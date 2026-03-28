package com.pm.todoservice.service;

import com.pm.todoservice.exception.ForbiddenException;
import com.pm.todoservice.model.Board;
import com.pm.todoservice.model.enums.BoardSharePermission;
import com.pm.todoservice.repository.BoardShareRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BoardAccessService {

    private final BoardShareRepository boardShareRepository;

    public BoardAccessService(BoardShareRepository boardShareRepository) {
        this.boardShareRepository = boardShareRepository;
    }

    public void validateReadBoard(Board board, AuthContext authContext) {
        if (isBoardOwnerOrAdmin(board, authContext)) {
            return;
        }

        boolean hasShareAccess = hasBoardPermission(
                board.getId(),
                authContext.userId(),
                List.of(BoardSharePermission.VIEW, BoardSharePermission.EDIT)
        );
        if (!hasShareAccess) {
            throw new ForbiddenException("You do not have access to this board");
        }
    }

    public void validateEditBoard(Board board, AuthContext authContext) {
        if (isBoardOwnerOrAdmin(board, authContext)) {
            return;
        }

        boolean hasEditAccess = hasBoardPermission(
                board.getId(),
                authContext.userId(),
                List.of(BoardSharePermission.EDIT)
        );
        if (!hasEditAccess) {
            throw new ForbiddenException("You do not have edit access to this board");
        }
    }

    public void validateManageBoard(Board board, AuthContext authContext) {
        if (!isBoardOwnerOrAdmin(board, authContext)) {
            throw new ForbiddenException("Only owner or admin can manage board sharing");
        }
    }

    public Set<UUID> getReadableBoardIds(AuthContext authContext) {
        if (authContext.isAdmin()) {
            return Set.of();
        }
        return boardShareRepository.findBoardIdsBySharedWithUserIdAndPermissionIn(
                authContext.userId(),
                List.of(BoardSharePermission.VIEW, BoardSharePermission.EDIT)
        );
    }

    public Set<UUID> getEditableBoardIds(AuthContext authContext) {
        if (authContext.isAdmin()) {
            return Set.of();
        }
        return boardShareRepository.findBoardIdsBySharedWithUserIdAndPermissionIn(
                authContext.userId(),
                List.of(BoardSharePermission.EDIT)
        );
    }

    public boolean isBoardOwnerOrAdmin(Board board, AuthContext authContext) {
        if (authContext.isAdmin()) {
            return true;
        }
        return board.getOwnerUserId() != null && board.getOwnerUserId().equals(authContext.userId());
    }

    private boolean hasBoardPermission(UUID boardId, UUID userId, List<BoardSharePermission> permissions) {
        return boardShareRepository.findByBoardIdAndSharedWithUserId(boardId, userId)
                .map(share -> permissions.contains(share.getPermission()))
                .orElse(false);
    }
}
