package com.pm.todoservice.service;

import com.pm.todoservice.dto.TodoActivityDTO;
import com.pm.todoservice.model.TodoActivity;
import com.pm.todoservice.model.enums.TodoActivityAction;
import com.pm.todoservice.repository.TodoActivityRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TodoActivityService {

    private final TodoActivityRepository todoActivityRepository;

    public TodoActivityService(TodoActivityRepository todoActivityRepository) {
        this.todoActivityRepository = todoActivityRepository;
    }

    public void log(UUID todoId, TodoActivityAction action, AuthContext authContext, String details) {
        TodoActivity activity = new TodoActivity();
        activity.setTodoId(todoId);
        activity.setAction(action);
        activity.setActorUserId(authContext != null ? authContext.userId() : null);
        activity.setDetails(details);
        todoActivityRepository.save(activity);
    }

    public List<TodoActivityDTO> getTodoActivity(UUID todoId) {
        return todoActivityRepository.findByTodoIdOrderByCreatedAtDesc(todoId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TodoActivityDTO toDto(TodoActivity activity) {
        return new TodoActivityDTO(
                activity.getId(),
                activity.getTodoId(),
                activity.getAction(),
                activity.getActorUserId(),
                activity.getDetails(),
                activity.getCreatedAt()
        );
    }
}
