package com.pm.todoservice.service;

import com.pm.todoservice.exception.ForbiddenException;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.model.enums.TodoSharePermission;
import com.pm.todoservice.repository.TodoShareRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TodoAuthorizationService {

    private final TodoShareRepository todoShareRepository;

    public TodoAuthorizationService(TodoShareRepository todoShareRepository) {
        this.todoShareRepository = todoShareRepository;
    }

    public void validateOwnership(Todo todo, AuthContext authContext) {
        if (isOwnerOrAdmin(todo, authContext)) {
            return;
        }
        throw new ForbiddenException("You do not have access to this todo");
    }

    public void validateReadAccess(Todo todo, AuthContext authContext) {
        if (isOwnerOrAdmin(todo, authContext)) {
            return;
        }

        boolean hasShareAccess = hasPermission(todo.getId(), authContext.userId(), List.of(TodoSharePermission.VIEW, TodoSharePermission.EDIT));
        if (!hasShareAccess) {
            throw new ForbiddenException("You do not have access to this todo");
        }
    }

    public void validateEditAccess(Todo todo, AuthContext authContext) {
        if (isOwnerOrAdmin(todo, authContext)) {
            return;
        }

        boolean hasEditAccess = hasPermission(todo.getId(), authContext.userId(), List.of(TodoSharePermission.EDIT));
        if (!hasEditAccess) {
            throw new ForbiddenException("You do not have edit access to this todo");
        }
    }

    private boolean isOwnerOrAdmin(Todo todo, AuthContext authContext) {
        if (authContext.isAdmin()) {
            return true;
        }

        return todo.getUserId() != null && todo.getUserId().equals(authContext.userId());
    }

    private boolean hasPermission(UUID todoId, UUID userId, List<TodoSharePermission> permissions) {
        return todoShareRepository.findByTodoIdAndSharedWithUserId(todoId, userId)
                .map(share -> permissions.contains(share.getPermission()))
                .orElse(false);
    }

    public void validateUserScope(UUID userId, AuthContext authContext, String message) {
        if (!authContext.isAdmin() && !userId.equals(authContext.userId())) {
            throw new ForbiddenException(message);
        }
    }

    public void requireAdmin(AuthContext authContext, String message) {
        if (!authContext.isAdmin()) {
            throw new ForbiddenException(message);
        }
    }
}
