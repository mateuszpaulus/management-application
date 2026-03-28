package com.pm.todoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private UUID id;
    private String name;
    private UUID ownerUserId;
    private Boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean shared = false;
    private List<UUID> sharedWithUserIds = new ArrayList<>();
    private List<BoardSectionDTO> sections = new ArrayList<>();
}
