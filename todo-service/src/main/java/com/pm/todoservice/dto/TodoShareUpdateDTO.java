package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.TodoSharePermission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoShareUpdateDTO {
    @NotNull(message = "permission is required")
    private TodoSharePermission permission;
}
