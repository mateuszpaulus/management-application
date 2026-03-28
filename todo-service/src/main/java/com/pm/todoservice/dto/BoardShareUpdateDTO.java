package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.BoardSharePermission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardShareUpdateDTO {
    @NotNull(message = "permission is required")
    private BoardSharePermission permission;
}
