package com.pm.todoservice.service;

import com.pm.todoservice.dto.SubtaskDTO;
import com.pm.todoservice.dto.SubtaskPatchDTO;
import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.dto.TodoPatchDTO;
import com.pm.todoservice.dto.TodoShareDTO;
import com.pm.todoservice.dto.TodoShareRequestDTO;
import com.pm.todoservice.dto.TodoShareUpdateDTO;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.model.TodoShare;
import com.pm.todoservice.model.TodoSubtask;
import com.pm.todoservice.model.enums.TodoActivityAction;
import com.pm.todoservice.repository.TodoRepository;
import com.pm.todoservice.repository.TodoShareRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TodoCommandService {

    private final TodoRepository todoRepository;
    private final TodoAuthorizationService authorizationService;
    private final TodoValidationService validationService;
    private final TodoMapper todoMapper;
    private final TodoActivityService todoActivityService;
    private final TodoShareRepository todoShareRepository;

    public TodoCommandService(
            TodoRepository todoRepository,
            TodoAuthorizationService authorizationService,
            TodoValidationService validationService,
            TodoMapper todoMapper,
            TodoActivityService todoActivityService,
            TodoShareRepository todoShareRepository
    ) {
        this.todoRepository = todoRepository;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
        this.todoMapper = todoMapper;
        this.todoActivityService = todoActivityService;
        this.todoShareRepository = todoShareRepository;
    }

    @Transactional
    public TodoDTO createTodo(TodoDTO todoDTO, AuthContext authContext) {
        Todo todo = new Todo();
        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setCompleted(todoDTO.getCompleted());
        todo.setDueDate(todoDTO.getDueDate());
        todo.setRemindAt(todoDTO.getRemindAt());
        todo.setPriority(validationService.resolvePriority(todoDTO.getPriority()));
        todo.setCategory(validationService.normalizeCategory(todoDTO.getCategory()));
        todo.setTags(validationService.normalizeTags(todoDTO.getTags()));
        todo.setSubtasks(validationService.normalizeSubtasks(todoDTO.getSubtasks()));

        validationService.validateSchedule(todo.getDueDate(), todo.getRemindAt());

        if (authContext.isAdmin()) {
            todo.setUserId(todoDTO.getUserId() != null ? todoDTO.getUserId() : authContext.userId());
        } else {
            todo.setUserId(authContext.userId());
        }

        Todo savedTodo = todoRepository.save(todo);
        todoActivityService.log(savedTodo.getId(), TodoActivityAction.CREATED, authContext, "Todo created");
        return todoMapper.toDto(savedTodo);
    }

    @Transactional
    public TodoDTO updateTodo(UUID id, TodoDTO todoDTO, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateEditAccess(todo, authContext);

        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setCompleted(todoDTO.getCompleted());
        todo.setDueDate(todoDTO.getDueDate());
        todo.setRemindAt(todoDTO.getRemindAt());
        todo.setPriority(validationService.resolvePriority(todoDTO.getPriority()));
        todo.setCategory(validationService.normalizeCategory(todoDTO.getCategory()));
        todo.setTags(validationService.normalizeTags(todoDTO.getTags()));
        todo.setSubtasks(validationService.normalizeSubtasks(todoDTO.getSubtasks()));

        validationService.validateSchedule(todo.getDueDate(), todo.getRemindAt());

        if (authContext.isAdmin() && todoDTO.getUserId() != null) {
            todo.setUserId(todoDTO.getUserId());
        }

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.UPDATED, authContext, "Todo updated");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public TodoDTO patchTodo(UUID id, TodoPatchDTO patchDTO, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateEditAccess(todo, authContext);

        if (patchDTO.getTitle() != null) {
            todo.setTitle(validationService.normalizePatchTitle(patchDTO.getTitle()));
        }

        if (patchDTO.getDescription() != null) {
            validationService.validatePatchDescription(patchDTO.getDescription());
            todo.setDescription(patchDTO.getDescription());
        }

        if (patchDTO.getCompleted() != null) {
            todo.setCompleted(patchDTO.getCompleted());
        }

        if (patchDTO.getUserId() != null) {
            authorizationService.requireAdmin(authContext, "Only ADMIN can change todo owner");
            todo.setUserId(patchDTO.getUserId());
        }

        if (patchDTO.getDueDate() != null) {
            todo.setDueDate(patchDTO.getDueDate());
        }

        if (patchDTO.getRemindAt() != null) {
            todo.setRemindAt(patchDTO.getRemindAt());
        }

        if (patchDTO.getPriority() != null) {
            todo.setPriority(patchDTO.getPriority());
        }

        if (patchDTO.getCategory() != null) {
            todo.setCategory(validationService.normalizeCategory(patchDTO.getCategory()));
        }

        if (patchDTO.getTags() != null) {
            todo.setTags(validationService.normalizeTags(patchDTO.getTags()));
        }

        if (patchDTO.getSubtasks() != null) {
            todo.setSubtasks(validationService.normalizeSubtasks(patchDTO.getSubtasks()));
        }

        validationService.validateSchedule(todo.getDueDate(), todo.getRemindAt());

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.PATCHED, authContext, "Todo patched");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public void deleteTodo(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateOwnership(todo, authContext);
        todoRepository.deleteById(id);
        todoActivityService.log(id, TodoActivityAction.DELETED, authContext, "Todo deleted permanently");
    }

    @Transactional
    public long deleteAllTodosByUserId(UUID userId, AuthContext authContext) {
        authorizationService.validateUserScope(userId, authContext, "You can only delete your own todos");
        return todoRepository.deleteByUserId(userId);
    }

    @Transactional
    public TodoDTO addSubtask(UUID todoId, SubtaskDTO subtaskDTO, AuthContext authContext) {
        Todo todo = findByIdOrThrow(todoId);
        authorizationService.validateEditAccess(todo, authContext);

        String title = validationService.normalizeSubtaskTitle(subtaskDTO.getTitle());
        TodoSubtask newSubtask = new TodoSubtask(
                title,
                subtaskDTO.getCompleted() != null && subtaskDTO.getCompleted()
        );

        List<TodoSubtask> subtasks = todo.getSubtasks();
        subtasks.add(newSubtask);
        todo.setSubtasks(subtasks);

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.SUBTASK_ADDED, authContext, "Subtask added");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public TodoDTO patchSubtask(UUID todoId, UUID subtaskId, SubtaskPatchDTO patchDTO, AuthContext authContext) {
        Todo todo = findByIdOrThrow(todoId);
        authorizationService.validateEditAccess(todo, authContext);

        TodoSubtask subtask = findSubtaskOrThrow(todo, subtaskId);

        if (patchDTO.getTitle() != null) {
            subtask.setTitle(validationService.normalizeSubtaskTitle(patchDTO.getTitle()));
        }

        if (patchDTO.getCompleted() != null) {
            subtask.setCompleted(patchDTO.getCompleted());
        }

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.SUBTASK_UPDATED, authContext, "Subtask updated");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public TodoDTO deleteSubtask(UUID todoId, UUID subtaskId, AuthContext authContext) {
        Todo todo = findByIdOrThrow(todoId);
        authorizationService.validateEditAccess(todo, authContext);

        boolean removed = todo.getSubtasks().removeIf(subtask -> subtaskId.equals(subtask.getId()));
        if (!removed) {
            throw new RuntimeException("Subtask not found with id: " + subtaskId);
        }

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.SUBTASK_DELETED, authContext, "Subtask deleted");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public TodoDTO archiveTodo(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateEditAccess(todo, authContext);

        todo.setArchived(true);
        todo.setArchivedAt(java.time.LocalDateTime.now());
        todo.setArchivedBy(authContext.userId());

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.ARCHIVED, authContext, "Todo archived");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public TodoDTO restoreTodo(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateEditAccess(todo, authContext);

        todo.setArchived(false);
        todo.setArchivedAt(null);
        todo.setArchivedBy(null);

        Todo updatedTodo = todoRepository.save(todo);
        todoActivityService.log(updatedTodo.getId(), TodoActivityAction.RESTORED, authContext, "Todo restored");
        return todoMapper.toDto(updatedTodo);
    }

    @Transactional
    public TodoShareDTO addShare(UUID todoId, TodoShareRequestDTO requestDTO, AuthContext authContext) {
        Todo todo = findByIdOrThrow(todoId);
        authorizationService.validateOwnership(todo, authContext);

        if (todo.getUserId() != null && todo.getUserId().equals(requestDTO.getSharedWithUserId())) {
            throw new RuntimeException("Owner already has full access to this todo");
        }

        if (todoShareRepository.existsByTodoIdAndSharedWithUserId(todoId, requestDTO.getSharedWithUserId())) {
            throw new RuntimeException("Todo is already shared with this user");
        }

        TodoShare share = new TodoShare();
        share.setTodoId(todoId);
        share.setSharedWithUserId(requestDTO.getSharedWithUserId());
        share.setPermission(requestDTO.getPermission());
        share.setCreatedBy(authContext.userId());

        TodoShare savedShare = todoShareRepository.save(share);
        todoActivityService.log(
                todoId,
                TodoActivityAction.SHARE_ADDED,
                authContext,
                "Shared with user " + requestDTO.getSharedWithUserId() + " as " + requestDTO.getPermission()
        );
        return toShareDto(savedShare);
    }

    @Transactional
    public TodoShareDTO updateShare(UUID todoId, UUID sharedUserId, TodoShareUpdateDTO requestDTO, AuthContext authContext) {
        Todo todo = findByIdOrThrow(todoId);
        authorizationService.validateOwnership(todo, authContext);

        TodoShare share = todoShareRepository.findByTodoIdAndSharedWithUserId(todoId, sharedUserId)
                .orElseThrow(() -> new RuntimeException("Share not found for this user"));

        share.setPermission(requestDTO.getPermission());
        TodoShare savedShare = todoShareRepository.save(share);

        todoActivityService.log(
                todoId,
                TodoActivityAction.SHARE_UPDATED,
                authContext,
                "Updated share for user " + sharedUserId + " to " + requestDTO.getPermission()
        );
        return toShareDto(savedShare);
    }

    @Transactional
    public void removeShare(UUID todoId, UUID sharedUserId, AuthContext authContext) {
        Todo todo = findByIdOrThrow(todoId);
        authorizationService.validateOwnership(todo, authContext);

        TodoShare share = todoShareRepository.findByTodoIdAndSharedWithUserId(todoId, sharedUserId)
                .orElseThrow(() -> new RuntimeException("Share not found for this user"));

        todoShareRepository.delete(share);
        todoActivityService.log(
                todoId,
                TodoActivityAction.SHARE_REMOVED,
                authContext,
                "Removed share for user " + sharedUserId
        );
    }

    private Todo findByIdOrThrow(UUID id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
    }

    private TodoSubtask findSubtaskOrThrow(Todo todo, UUID subtaskId) {
        return todo.getSubtasks().stream()
                .filter(subtask -> subtaskId.equals(subtask.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Subtask not found with id: " + subtaskId));
    }

    private TodoShareDTO toShareDto(TodoShare share) {
        return new TodoShareDTO(
                share.getId(),
                share.getTodoId(),
                share.getSharedWithUserId(),
                share.getPermission(),
                share.getCreatedBy(),
                share.getCreatedAt()
        );
    }
}
