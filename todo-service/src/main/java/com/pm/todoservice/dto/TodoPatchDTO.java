package com.pm.todoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoPatchDTO {
    private String title;
    private String description;
    private Boolean completed;
    private UUID userId;
}
