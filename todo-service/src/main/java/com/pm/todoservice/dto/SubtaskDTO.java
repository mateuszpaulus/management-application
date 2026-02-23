package com.pm.todoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskDTO {

    private UUID id;

    @NotBlank(message = "Subtask title is required")
    @Size(min = 1, max = 255, message = "Subtask title must be between 1 and 255 characters")
    private String title;

    private Boolean completed = false;
}
