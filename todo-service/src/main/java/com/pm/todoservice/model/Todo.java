package com.pm.todoservice.model;

import com.pm.todoservice.model.enums.TodoPriority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "todos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String title;

    @Setter
    @Getter
    @Column(length = 1000)
    private String description;

    @NotNull
    private Boolean completed = false;

    @NotNull
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Setter
    @Getter
    @Column(name = "user_id")
    private UUID userId;

    @Setter
    @Getter
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Setter
    @Getter
    @Column(name = "remind_at")
    private LocalDateTime remindAt;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TodoPriority priority;

    @Setter
    @Getter
    @Column(name = "category", length = 100)
    private String category;

    @Setter
    @Getter
    @ElementCollection
    @CollectionTable(name = "todo_tags", joinColumns = @JoinColumn(name = "todo_id"))
    @Column(name = "tag", length = 50)
    private Set<String> tags = new HashSet<>();

    @Setter
    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "todo_id")
    @OrderColumn(name = "position")
    private List<TodoSubtask> subtasks = new ArrayList<>();

    @Setter
    @Getter
    @Column(name = "archived")
    private Boolean archived = false;

    @Setter
    @Getter
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Setter
    @Getter
    @Column(name = "archived_by")
    private UUID archivedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public @NotNull String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public @NotNull Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(@NotNull Boolean completed) {
        this.completed = completed;
    }
}
