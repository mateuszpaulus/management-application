package com.pm.todoservice.service;

import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.dto.TodoPatchDTO;
import com.pm.todoservice.exception.ForbiddenException;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.repository.TodoRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional
    public TodoDTO createTodo(TodoDTO todoDTO, AuthContext authContext) {
        Todo todo = new Todo();
        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setCompleted(todoDTO.getCompleted());

        if (authContext.isAdmin()) {
            todo.setUserId(todoDTO.getUserId() != null ? todoDTO.getUserId() : authContext.userId());
        } else {
            todo.setUserId(authContext.userId());
        }

        Todo savedTodo = todoRepository.save(todo);
        return convertToDTO(savedTodo);
    }

    public List<TodoDTO> getAllTodos(AuthContext authContext) {
        List<Todo> todos = authContext.isAdmin()
                ? todoRepository.findAll()
                : todoRepository.findByUserId(authContext.userId());

        return todos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TodoDTO getTodoById(UUID id, AuthContext authContext) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, authContext);
        return convertToDTO(todo);
    }

    public Todo getEntireTodoById(UUID id, AuthContext authContext) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, authContext);
        return todo;
    }

    public List<TodoDTO> getTodosByUserId(UUID userId, AuthContext authContext) {
        if (!authContext.isAdmin() && !userId.equals(authContext.userId())) {
            throw new ForbiddenException("You can only access your own todos");
        }

        return todoRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TodoDTO updateTodo(UUID id, TodoDTO todoDTO, AuthContext authContext) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, authContext);

        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setCompleted(todoDTO.getCompleted());
        if (authContext.isAdmin() && todoDTO.getUserId() != null) {
            todo.setUserId(todoDTO.getUserId());
        }

        Todo updatedTodo = todoRepository.save(todo);
        return convertToDTO(updatedTodo);
    }

    @Transactional
    public TodoDTO patchTodo(UUID id, TodoPatchDTO patchDTO, AuthContext authContext) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, authContext);

        if (patchDTO.getTitle() != null) {
            String title = patchDTO.getTitle().trim();
            if (title.isEmpty() || title.length() > 255) {
                throw new RuntimeException("Title must be between 1 and 255 characters");
            }
            todo.setTitle(title);
        }

        if (patchDTO.getDescription() != null) {
            if (patchDTO.getDescription().length() > 1000) {
                throw new RuntimeException("Description cannot exceed 1000 characters");
            }
            todo.setDescription(patchDTO.getDescription());
        }

        if (patchDTO.getCompleted() != null) {
            todo.setCompleted(patchDTO.getCompleted());
        }

        if (patchDTO.getUserId() != null) {
            if (!authContext.isAdmin()) {
                throw new ForbiddenException("Only ADMIN can change todo owner");
            }
            todo.setUserId(patchDTO.getUserId());
        }

        Todo updatedTodo = todoRepository.save(todo);
        return convertToDTO(updatedTodo);
    }

    @Transactional
    public void deleteTodo(UUID id, AuthContext authContext) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, authContext);
        todoRepository.deleteById(id);
    }

    private TodoDTO convertToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setCompleted(todo.getCompleted());
        dto.setUserId(todo.getUserId());
        return dto;
    }

    private void validateOwnership(Todo todo, AuthContext authContext) {
        if (authContext.isAdmin()) {
            return;
        }

        if (todo.getUserId() == null || !todo.getUserId().equals(authContext.userId())) {
            throw new ForbiddenException("You do not have access to this todo");
        }
    }
}
