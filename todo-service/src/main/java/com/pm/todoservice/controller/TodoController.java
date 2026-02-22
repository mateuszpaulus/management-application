package com.pm.todoservice.controller;

import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.dto.TodoPatchDTO;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.security.AuthContext;
import com.pm.todoservice.service.TodoService;
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

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    @Operation(summary = "Create a new Todo")
    public ResponseEntity<TodoDTO> createTodo(
            AuthContext authContext,
            @Valid @RequestBody TodoDTO todoDTO
    ) {
        TodoDTO createdTodo = todoService.createTodo(todoDTO, authContext);
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Todos")
    public ResponseEntity<List<TodoDTO>> getAllTodos(AuthContext authContext) {
        List<TodoDTO> todos = todoService.getAllTodos(authContext);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Todo by Id")
    public ResponseEntity<TodoDTO> getTodoById(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        TodoDTO todo = todoService.getTodoById(id, authContext);
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/entire/{id}")
    @Operation(summary = "Get entire Todo by Id")
    public ResponseEntity<Todo> getEntireTodoById(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        Todo todo = todoService.getEntireTodoById(id, authContext);
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Todo by UserId")
    public ResponseEntity<List<TodoDTO>> getTodosByUserId(
            @PathVariable UUID userId,
            AuthContext authContext
    ) {
        List<TodoDTO> todos = todoService.getTodosByUserId(userId, authContext);
        return ResponseEntity.ok(todos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a Todo")
    public ResponseEntity<TodoDTO> updateTodo(
            @PathVariable UUID id,
            AuthContext authContext,
            @Valid @RequestBody TodoDTO todoDTO
    ) {
        TodoDTO updatedTodo = todoService.updateTodo(id, todoDTO, authContext);
        return ResponseEntity.ok(updatedTodo);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch Todo fields")
    public ResponseEntity<TodoDTO> patchTodo(
            @PathVariable UUID id,
            AuthContext authContext,
            @RequestBody TodoPatchDTO patchDTO
    ) {
        TodoDTO patchedTodo = todoService.patchTodo(id, patchDTO, authContext);
        return ResponseEntity.ok(patchedTodo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Todo")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        todoService.deleteTodo(id, authContext);
        return ResponseEntity.noContent().build();
    }
}
