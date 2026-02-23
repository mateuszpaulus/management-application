package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.TodoSharePermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoShareDTO {
    private UUID id;
    private UUID todoId;
    private UUID sharedWithUserId;
    private TodoSharePermission permission;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
