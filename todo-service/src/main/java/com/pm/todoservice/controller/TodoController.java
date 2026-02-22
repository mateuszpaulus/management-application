package com.pm.todoservice.controller;


import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.service.TodoService;
import com.pm.todoservice.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "todos", description = "API for managing Todos")
public class TodoController {
    private static final String USER_ID_HEADER = "X-Auth-User-Id";
    private static final String USER_ROLE_HEADER = "X-Auth-User-Role";

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    @Operation(summary = "Create a new Todo")
    public ResponseEntity<TodoDTO> createTodo(
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole,
            @Valid @RequestBody TodoDTO todoDTO
    ) {
        TodoDTO createdTodo = todoService.createTodo(todoDTO, requireUserId(requesterUserId), requireRole(requesterRole));
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Todos")
    public ResponseEntity<List<TodoDTO>> getAllTodos(
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        List<TodoDTO> todos = todoService.getAllTodos(requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Todo by Id")
    public ResponseEntity<TodoDTO> getTodoById(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        TodoDTO todo = todoService.getTodoById(id, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/entire/{id}")
    @Operation(summary = "Get entire Todo by Id")
    public ResponseEntity<Todo> getEntireTodoById(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        Todo todo = todoService.getEntireTodoById(id, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Todo by UserId")
    public ResponseEntity<List<TodoDTO>> getTodosByUserId(
            @PathVariable UUID userId,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        List<TodoDTO> todos = todoService.getTodosByUserId(userId, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(todos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a Todo")
    public ResponseEntity<TodoDTO> updateTodo(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole,
            @Valid @RequestBody TodoDTO todoDTO) {
        TodoDTO updatedTodo = todoService.updateTodo(id, todoDTO, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Todo")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        todoService.deleteTodo(id, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.noContent().build();
    }

    private UUID requireUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication header: " + USER_ID_HEADER);
        }

        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid authentication header: " + USER_ID_HEADER);
        }
    }

    private String requireRole(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication header: " + USER_ROLE_HEADER);
        }
        return roleHeader;
    }
}
