package com.pm.todoservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardUpdateDTO {
    @Size(min = 1, max = 150, message = "Board name must be between 1 and 150 characters")
    private String name;
    private Boolean archived;
}
