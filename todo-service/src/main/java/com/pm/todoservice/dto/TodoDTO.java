package com.pm.todoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

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