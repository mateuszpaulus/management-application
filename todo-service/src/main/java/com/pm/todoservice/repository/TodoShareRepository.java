package com.pm.todoservice.repository;

import com.pm.todoservice.model.TodoShare;
import com.pm.todoservice.model.enums.TodoSharePermission;
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
public interface TodoShareRepository extends JpaRepository<TodoShare, UUID> {

    List<TodoShare> findByTodoIdOrderByCreatedAtDesc(UUID todoId);

    List<TodoShare> findByTodoIdIn(Collection<UUID> todoIds);

    Optional<TodoShare> findByTodoIdAndSharedWithUserId(UUID todoId, UUID sharedWithUserId);

    boolean existsByTodoIdAndSharedWithUserId(UUID todoId, UUID sharedWithUserId);
    
    @Query("select s.todoId from TodoShare s where s.sharedWithUserId = :userId and s.permission in :permissions")
    Set<UUID> findTodoIdsBySharedWithUserIdAndPermissionIn(
            @Param("userId") UUID userId,
            @Param("permissions") Collection<TodoSharePermission> permissions
    );
}
