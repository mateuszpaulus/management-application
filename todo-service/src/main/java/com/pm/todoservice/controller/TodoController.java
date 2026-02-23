package com.pm.todoservice.controller;

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
import com.pm.todoservice.service.TodoPageableFactory;
import com.pm.todoservice.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "todos", description = "API for managing Todos")
public class TodoController {

    private final TodoService todoService;
    private final TodoPageableFactory todoPageableFactory;

    public TodoController(TodoService todoService, TodoPageableFactory todoPageableFactory) {
        this.todoService = todoService;
        this.todoPageableFactory = todoPageableFactory;
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
    public ResponseEntity<Page<TodoDTO>> getAllTodos(
            AuthContext authContext,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = todoPageableFactory.buildPageable(page, size, sort);
        Page<TodoDTO> todos = todoService.getAllTodos(authContext, category, tag, completed, archived, search, pageable);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/list")
    @Operation(summary = "Get Todos as list (without pagination)")
    public ResponseEntity<List<TodoDTO>> getAllTodosAsList(
            AuthContext authContext,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort order = todoPageableFactory.buildSort(sort);
        List<TodoDTO> todos = todoService.getAllTodosList(authContext, category, tag, completed, archived, search, order);
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

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Delete all todos by userId")
    public ResponseEntity<Map<String, Long>> deleteAllTodosByUserId(
            @PathVariable UUID userId,
            AuthContext authContext
    ) {
        long deletedCount = todoService.deleteAllTodosByUserId(userId, authContext);
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount));
    }

    @PostMapping("/{id}/subtasks")
    @Operation(summary = "Add subtask to todo")
    public ResponseEntity<TodoDTO> addSubtask(
            @PathVariable UUID id,
            AuthContext authContext,
            @Valid @RequestBody SubtaskDTO subtaskDTO
    ) {
        TodoDTO updatedTodo = todoService.addSubtask(id, subtaskDTO, authContext);
        return ResponseEntity.ok(updatedTodo);
    }

    @PatchMapping("/{id}/subtasks/{subtaskId}")
    @Operation(summary = "Patch subtask")
    public ResponseEntity<TodoDTO> patchSubtask(
            @PathVariable UUID id,
            @PathVariable UUID subtaskId,
            AuthContext authContext,
            @RequestBody SubtaskPatchDTO patchDTO
    ) {
        TodoDTO updatedTodo = todoService.patchSubtask(id, subtaskId, patchDTO, authContext);
        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/{id}/subtasks/{subtaskId}")
    @Operation(summary = "Delete subtask")
    public ResponseEntity<TodoDTO> deleteSubtask(
            @PathVariable UUID id,
            @PathVariable UUID subtaskId,
            AuthContext authContext
    ) {
        TodoDTO updatedTodo = todoService.deleteSubtask(id, subtaskId, authContext);
        return ResponseEntity.ok(updatedTodo);
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive todo")
    public ResponseEntity<TodoDTO> archiveTodo(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        TodoDTO updatedTodo = todoService.archiveTodo(id, authContext);
        return ResponseEntity.ok(updatedTodo);
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore archived todo")
    public ResponseEntity<TodoDTO> restoreTodo(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        TodoDTO updatedTodo = todoService.restoreTodo(id, authContext);
        return ResponseEntity.ok(updatedTodo);
    }

    @GetMapping("/{id}/activity")
    @Operation(summary = "Get todo activity history")
    public ResponseEntity<List<TodoActivityDTO>> getTodoActivity(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        List<TodoActivityDTO> activity = todoService.getTodoActivity(id, authContext);
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/{id}/shares")
    @Operation(summary = "Get todo shares")
    public ResponseEntity<List<TodoShareDTO>> getTodoShares(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        List<TodoShareDTO> shares = todoService.getTodoShares(id, authContext);
        return ResponseEntity.ok(shares);
    }

    @PostMapping("/{id}/shares")
    @Operation(summary = "Share todo with user")
    public ResponseEntity<TodoShareDTO> addShare(
            @PathVariable UUID id,
            AuthContext authContext,
            @Valid @RequestBody TodoShareRequestDTO requestDTO
    ) {
        TodoShareDTO share = todoService.addShare(id, requestDTO, authContext);
        return new ResponseEntity<>(share, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/shares/{sharedUserId}")
    @Operation(summary = "Update todo share permission")
    public ResponseEntity<TodoShareDTO> updateShare(
            @PathVariable UUID id,
            @PathVariable UUID sharedUserId,
            AuthContext authContext,
            @Valid @RequestBody TodoShareUpdateDTO requestDTO
    ) {
        TodoShareDTO share = todoService.updateShare(id, sharedUserId, requestDTO, authContext);
        return ResponseEntity.ok(share);
    }

    @DeleteMapping("/{id}/shares/{sharedUserId}")
    @Operation(summary = "Remove todo share")
    public ResponseEntity<Void> removeShare(
            @PathVariable UUID id,
            @PathVariable UUID sharedUserId,
            AuthContext authContext
    ) {
        todoService.removeShare(id, sharedUserId, authContext);
        return ResponseEntity.noContent().build();
    }
}
