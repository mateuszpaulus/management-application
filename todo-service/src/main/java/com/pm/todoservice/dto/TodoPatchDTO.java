package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.TodoPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoPatchDTO {
    private String title;
    private String description;
    private Boolean completed;
    private UUID userId;
    private LocalDateTime dueDate;
    private LocalDateTime remindAt;
    private TodoPriority priority;
    private String category;
    private Set<String> tags;
    private List<SubtaskDTO> subtasks;
}
