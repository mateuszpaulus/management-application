package com.pm.todoservice.dto;

import com.pm.todoservice.model.enums.TodoActivityAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoActivityDTO {
    private UUID id;
    private UUID todoId;
    private TodoActivityAction action;
    private UUID actorUserId;
    private String details;
    private LocalDateTime createdAt;
}
