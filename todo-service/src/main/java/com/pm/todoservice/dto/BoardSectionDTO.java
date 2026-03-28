package com.pm.todoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardSectionDTO {
    private UUID id;
    private UUID boardId;
    private String name;
    private Integer position;
}
