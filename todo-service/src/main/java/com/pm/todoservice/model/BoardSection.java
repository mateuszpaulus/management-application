package com.pm.todoservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "board_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardSection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "position", nullable = false)
    private Integer position = 0;
}
