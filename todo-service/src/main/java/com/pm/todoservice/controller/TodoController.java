package com.pm.todoservice.controller;


import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.model.Todo;
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
    public ResponseEntity<TodoDTO> createTodo(@Valid @RequestBody TodoDTO todoDTO) {
        TodoDTO createdTodo = todoService.createTodo(todoDTO);
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Todos")
    public ResponseEntity<List<TodoDTO>> getAllTodos() {
        List<TodoDTO> todos = todoService.getAllTodos();
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Todo by Id")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable UUID id) {
        TodoDTO todo = todoService.getTodoById(id);
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/entire/{id}")
    @Operation(summary = "Get entire Todo by Id")
    public ResponseEntity<Todo> getEntireTodoById(@PathVariable UUID id) {
        Todo todo = todoService.getEntireTodoById(id);
        return ResponseEntity.ok(todo);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get Todo by UserId")
    public ResponseEntity<List<TodoDTO>> getTodosByUserId(@PathVariable UUID userId) {
        List<TodoDTO> todos = todoService.getTodosByUserId(userId);
        return ResponseEntity.ok(todos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a Todo")
    public ResponseEntity<TodoDTO> updateTodo(
            @PathVariable UUID id,
            @Valid @RequestBody TodoDTO todoDTO) {
        TodoDTO updatedTodo = todoService.updateTodo(id, todoDTO);
        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Todo")
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
}
