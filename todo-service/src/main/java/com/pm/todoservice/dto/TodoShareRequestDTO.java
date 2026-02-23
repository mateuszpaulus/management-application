package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.TodoSharePermission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoShareRequestDTO {
    @NotNull(message = "sharedWithUserId is required")
    private UUID sharedWithUserId;

    @NotNull(message = "permission is required")
    private TodoSharePermission permission;
}
