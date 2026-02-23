package com.pm.todoservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "todo_subtasks")
@Getter
@Setter
@NoArgsConstructor
public class TodoSubtask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "subtask_title", nullable = false)
    private String title;

    @Column(name = "subtask_completed", nullable = false)
    private Boolean completed = false;

    public TodoSubtask(String title, Boolean completed) {
        this.title = title;
        this.completed = completed;
    }
}
