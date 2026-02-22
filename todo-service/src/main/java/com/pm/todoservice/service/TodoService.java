package com.pm.todoservice.service;

import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.exception.ForbiddenException;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.repository.TodoRepository;
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
    public TodoDTO createTodo(TodoDTO todoDTO, UUID requesterUserId, String requesterRole) {
        Todo todo = new Todo();
        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setCompleted(todoDTO.getCompleted());

        if (isAdmin(requesterRole)) {
            todo.setUserId(todoDTO.getUserId());
        } else {
            todo.setUserId(requesterUserId);
        }

        Todo savedTodo = todoRepository.save(todo);
        return convertToDTO(savedTodo);
    }

    public List<TodoDTO> getAllTodos(UUID requesterUserId, String requesterRole) {
        List<Todo> todos = isAdmin(requesterRole)
                ? todoRepository.findAll()
                : todoRepository.findByUserId(requesterUserId);

        return todos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TodoDTO getTodoById(UUID id, UUID requesterUserId, String requesterRole) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, requesterUserId, requesterRole);
        return convertToDTO(todo);
    }

    public Todo getEntireTodoById(UUID id, UUID requesterUserId, String requesterRole) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, requesterUserId, requesterRole);
        return todo;
    }

    public List<TodoDTO> getTodosByUserId(UUID userId, UUID requesterUserId, String requesterRole) {
        if (!isAdmin(requesterRole) && !userId.equals(requesterUserId)) {
            throw new ForbiddenException("You can only access your own todos");
        }

        return todoRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TodoDTO updateTodo(UUID id, TodoDTO todoDTO, UUID requesterUserId, String requesterRole) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, requesterUserId, requesterRole);

        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setCompleted(todoDTO.getCompleted());
        if (isAdmin(requesterRole) && todoDTO.getUserId() != null) {
            todo.setUserId(todoDTO.getUserId());
        }

        Todo updatedTodo = todoRepository.save(todo);
        return convertToDTO(updatedTodo);
    }

    @Transactional
    public void deleteTodo(UUID id, UUID requesterUserId, String requesterRole) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
        validateOwnership(todo, requesterUserId, requesterRole);
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

    private void validateOwnership(Todo todo, UUID requesterUserId, String requesterRole) {
        if (isAdmin(requesterRole)) {
            return;
        }

        if (todo.getUserId() == null || !todo.getUserId().equals(requesterUserId)) {
            throw new ForbiddenException("You do not have access to this todo");
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
