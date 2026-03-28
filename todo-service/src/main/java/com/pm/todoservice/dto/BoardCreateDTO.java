package com.pm.todoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateDTO {
    @NotBlank(message = "Board name is required")
    @Size(min = 1, max = 150, message = "Board name must be between 1 and 150 characters")
    private String name;
}
