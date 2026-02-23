package com.pm.todoservice.service;

import com.pm.todoservice.dto.SubtaskDTO;
import com.pm.todoservice.dto.SubtaskPatchDTO;
import com.pm.todoservice.dto.TodoActivityDTO;
import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.dto.TodoPatchDTO;
import com.pm.todoservice.dto.TodoShareDTO;
import com.pm.todoservice.dto.TodoShareRequestDTO;
import com.pm.todoservice.dto.TodoShareUpdateDTO;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.security.AuthContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TodoService {

    private final TodoCommandService commandService;
    private final TodoQueryService queryService;

    public TodoService(TodoCommandService commandService, TodoQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    public TodoDTO createTodo(TodoDTO todoDTO, AuthContext authContext) {
        return commandService.createTodo(todoDTO, authContext);
    }

    public Page<TodoDTO> getAllTodos(
            AuthContext authContext,
            String category,
            String tag,
            Boolean completed,
            Boolean archived,
            String search,
            Pageable pageable
    ) {
        return queryService.getAllTodos(authContext, category, tag, completed, archived, search, pageable);
    }

    public List<TodoDTO> getAllTodosList(
            AuthContext authContext,
            String category,
            String tag,
            Boolean completed,
            Boolean archived,
            String search,
            Sort sort
    ) {
        return queryService.getAllTodosList(authContext, category, tag, completed, archived, search, sort);
    }

    public TodoDTO getTodoById(UUID id, AuthContext authContext) {
        return queryService.getTodoById(id, authContext);
    }

    public Todo getEntireTodoById(UUID id, AuthContext authContext) {
        return queryService.getEntireTodoById(id, authContext);
    }

    public List<TodoDTO> getTodosByUserId(UUID userId, AuthContext authContext) {
        return queryService.getTodosByUserId(userId, authContext);
    }

    public TodoDTO updateTodo(UUID id, TodoDTO todoDTO, AuthContext authContext) {
        return commandService.updateTodo(id, todoDTO, authContext);
    }

    public TodoDTO patchTodo(UUID id, TodoPatchDTO patchDTO, AuthContext authContext) {
        return commandService.patchTodo(id, patchDTO, authContext);
    }

    public void deleteTodo(UUID id, AuthContext authContext) {
        commandService.deleteTodo(id, authContext);
    }

    public long deleteAllTodosByUserId(UUID userId, AuthContext authContext) {
        return commandService.deleteAllTodosByUserId(userId, authContext);
    }

    public TodoDTO addSubtask(UUID todoId, SubtaskDTO subtaskDTO, AuthContext authContext) {
        return commandService.addSubtask(todoId, subtaskDTO, authContext);
    }

    public TodoDTO patchSubtask(UUID todoId, UUID subtaskId, SubtaskPatchDTO patchDTO, AuthContext authContext) {
        return commandService.patchSubtask(todoId, subtaskId, patchDTO, authContext);
    }

    public TodoDTO deleteSubtask(UUID todoId, UUID subtaskId, AuthContext authContext) {
        return commandService.deleteSubtask(todoId, subtaskId, authContext);
    }

    public TodoDTO archiveTodo(UUID id, AuthContext authContext) {
        return commandService.archiveTodo(id, authContext);
    }

    public TodoDTO restoreTodo(UUID id, AuthContext authContext) {
        return commandService.restoreTodo(id, authContext);
    }

    public List<TodoActivityDTO> getTodoActivity(UUID id, AuthContext authContext) {
        return queryService.getTodoActivity(id, authContext);
    }

    public List<TodoShareDTO> getTodoShares(UUID id, AuthContext authContext) {
        return queryService.getTodoShares(id, authContext);
    }

    public TodoShareDTO addShare(UUID id, TodoShareRequestDTO requestDTO, AuthContext authContext) {
        return commandService.addShare(id, requestDTO, authContext);
    }

    public TodoShareDTO updateShare(UUID id, UUID sharedUserId, TodoShareUpdateDTO requestDTO, AuthContext authContext) {
        return commandService.updateShare(id, sharedUserId, requestDTO, authContext);
    }

    public void removeShare(UUID id, UUID sharedUserId, AuthContext authContext) {
        commandService.removeShare(id, sharedUserId, authContext);
    }
}
