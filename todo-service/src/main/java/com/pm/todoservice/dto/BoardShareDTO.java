package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.BoardSharePermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardShareDTO {
    private UUID id;
    private UUID boardId;
    private UUID sharedWithUserId;
    private BoardSharePermission permission;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
