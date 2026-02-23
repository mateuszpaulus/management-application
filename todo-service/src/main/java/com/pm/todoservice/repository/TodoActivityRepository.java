package com.pm.todoservice.repository;

import com.pm.todoservice.model.TodoActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoActivityRepository extends JpaRepository<TodoActivity, UUID> {
    List<TodoActivity> findByTodoIdOrderByCreatedAtDesc(UUID todoId);
}
