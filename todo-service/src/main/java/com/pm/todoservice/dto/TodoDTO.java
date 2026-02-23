package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.TodoPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoDTO {

    @Getter
    @Setter
    private UUID id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Setter
    @Getter
    private Boolean completed = false;

    @Setter
    @Getter
    private UUID userId;

    @Setter
    @Getter
    private LocalDateTime dueDate;

    @Setter
    @Getter
    private LocalDateTime remindAt;

    @Setter
    @Getter
    private TodoPriority priority = TodoPriority.MEDIUM;

    @Setter
    @Getter
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Setter
    @Getter
    private Set<@Size(min = 1, max = 50, message = "Each tag must be between 1 and 50 characters") String> tags = new HashSet<>();

    @Setter
    @Getter
    private List<SubtaskDTO> subtasks = new ArrayList<>();

    @Setter
    @Getter
    private Integer completedSubtasks = 0;

    @Setter
    @Getter
    private Integer totalSubtasks = 0;

    @Setter
    @Getter
    private Integer progressPercent = 0;

    @Setter
    @Getter
    private Boolean archived = false;

    @Setter
    @Getter
    private LocalDateTime archivedAt;

    @Setter
    @Getter
    private UUID archivedBy;

    @Setter
    @Getter
    private Boolean shared = false;

    @Setter
    @Getter
    private List<UUID> sharedWithUserIds = new ArrayList<>();
    
    public @NotBlank(message = "Title is required") @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "Title is required") @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters") String title) {
        this.title = title;
    }

    public @Size(max = 1000, message = "Description cannot exceed 1000 characters") String getDescription() {
        return description;
    }

    public void setDescription(@Size(max = 1000, message = "Description cannot exceed 1000 characters") String description) {
        this.description = description;
    }

}
