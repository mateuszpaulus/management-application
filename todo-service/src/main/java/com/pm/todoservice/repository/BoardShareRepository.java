package com.pm.todoservice.repository;

import com.pm.todoservice.model.BoardShare;
import com.pm.todoservice.model.enums.BoardSharePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BoardShareRepository extends JpaRepository<BoardShare, UUID> {

    List<BoardShare> findByBoardIdOrderByCreatedAtDesc(UUID boardId);

    Optional<BoardShare> findByBoardIdAndSharedWithUserId(UUID boardId, UUID sharedWithUserId);

    boolean existsByBoardIdAndSharedWithUserId(UUID boardId, UUID sharedWithUserId);

    @Query("select s.boardId from BoardShare s where s.sharedWithUserId = :userId and s.permission in :permissions")
    Set<UUID> findBoardIdsBySharedWithUserIdAndPermissionIn(
            @Param("userId") UUID userId,
            @Param("permissions") Collection<BoardSharePermission> permissions
    );
}
