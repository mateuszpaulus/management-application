package com.pm.todoservice.model;

import com.pm.todoservice.model.enums.TodoActivityAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "todo_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "todo_id", nullable = false)
    private UUID todoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private TodoActivityAction action;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
