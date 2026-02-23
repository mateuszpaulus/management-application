package com.pm.todoservice.model;

import com.pm.todoservice.model.enums.TodoSharePermission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "todo_shares",
        uniqueConstraints = @UniqueConstraint(name = "uk_todo_share_todo_user", columnNames = {"todo_id", "shared_with_user_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoShare {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "todo_id", nullable = false)
    private UUID todoId;

    @Column(name = "shared_with_user_id", nullable = false)
    private UUID sharedWithUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    private TodoSharePermission permission;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
