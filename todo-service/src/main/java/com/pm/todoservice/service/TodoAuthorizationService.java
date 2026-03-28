package com.pm.todoservice.service;

import com.pm.todoservice.exception.ForbiddenException;
import com.pm.todoservice.model.Board;
import com.pm.todoservice.model.BoardSection;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.model.enums.TodoSharePermission;
import com.pm.todoservice.repository.BoardRepository;
import com.pm.todoservice.repository.BoardSectionRepository;
import com.pm.todoservice.repository.TodoShareRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TodoAuthorizationService {

    private final TodoShareRepository todoShareRepository;
    private final BoardSectionRepository boardSectionRepository;
    private final BoardRepository boardRepository;
    private final BoardAccessService boardAccessService;

    public TodoAuthorizationService(
            TodoShareRepository todoShareRepository,
            BoardSectionRepository boardSectionRepository,
            BoardRepository boardRepository,
            BoardAccessService boardAccessService
    ) {
        this.todoShareRepository = todoShareRepository;
        this.boardSectionRepository = boardSectionRepository;
        this.boardRepository = boardRepository;
        this.boardAccessService = boardAccessService;
    }

    public void validateOwnership(Todo todo, AuthContext authContext) {
        if (isOwnerOrAdmin(todo, authContext)) {
            return;
        }
        if (isBoardOwnerOrAdmin(todo, authContext)) {
            return;
        }
        throw new ForbiddenException("You do not have access to this todo");
    }

    public void validateReadAccess(Todo todo, AuthContext authContext) {
        if (isOwnerOrAdmin(todo, authContext)) {
            return;
        }
        if (hasBoardReadAccess(todo, authContext)) {
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
        if (hasBoardEditAccess(todo, authContext)) {
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

    private boolean hasBoardReadAccess(Todo todo, AuthContext authContext) {
        return resolveBoard(todo)
                .map(board -> {
                    try {
                        boardAccessService.validateReadBoard(board, authContext);
                        return true;
                    } catch (ForbiddenException ignored) {
                        return false;
                    }
                })
                .orElse(false);
    }

    private boolean hasBoardEditAccess(Todo todo, AuthContext authContext) {
        return resolveBoard(todo)
                .map(board -> {
                    try {
                        boardAccessService.validateEditBoard(board, authContext);
                        return true;
                    } catch (ForbiddenException ignored) {
                        return false;
                    }
                })
                .orElse(false);
    }

    private boolean isBoardOwnerOrAdmin(Todo todo, AuthContext authContext) {
        return resolveBoard(todo)
                .map(board -> boardAccessService.isBoardOwnerOrAdmin(board, authContext))
                .orElse(false);
    }

    private java.util.Optional<Board> resolveBoard(Todo todo) {
        if (todo.getSectionId() == null) {
            return java.util.Optional.empty();
        }
        return boardSectionRepository.findById(todo.getSectionId())
                .map(BoardSection::getBoardId)
                .flatMap(boardRepository::findById);
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
