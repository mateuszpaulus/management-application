package com.pm.todoservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardSectionUpdateDTO {
    @Size(min = 1, max = 120, message = "Section name must be between 1 and 120 characters")
    private String name;
    private Integer position;
}
