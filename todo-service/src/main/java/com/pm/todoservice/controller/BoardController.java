package com.pm.todoservice.controller;

import com.pm.todoservice.dto.*;
import com.pm.todoservice.security.AuthContext;
import com.pm.todoservice.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "boards", description = "API for managing Boards and Sections")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    @Operation(summary = "Create board")
    public ResponseEntity<BoardDTO> createBoard(
            AuthContext authContext,
            @Valid @RequestBody BoardCreateDTO dto
    ) {
        return new ResponseEntity<>(boardService.createBoard(dto, authContext), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get boards")
    public ResponseEntity<List<BoardDTO>> getBoards(AuthContext authContext) {
        return ResponseEntity.ok(boardService.getBoards(authContext));
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "Get board by id")
    public ResponseEntity<BoardDTO> getBoard(
            @PathVariable UUID boardId,
            AuthContext authContext
    ) {
        return ResponseEntity.ok(boardService.getBoard(boardId, authContext));
    }

    @PatchMapping("/{boardId}")
    @Operation(summary = "Update board")
    public ResponseEntity<BoardDTO> updateBoard(
            @PathVariable UUID boardId,
            AuthContext authContext,
            @RequestBody BoardUpdateDTO dto
    ) {
        return ResponseEntity.ok(boardService.updateBoard(boardId, dto, authContext));
    }

    @PostMapping("/{boardId}/sections")
    @Operation(summary = "Add section to board")
    public ResponseEntity<BoardSectionDTO> addSection(
            @PathVariable UUID boardId,
            AuthContext authContext,
            @Valid @RequestBody BoardSectionCreateDTO dto
    ) {
        return new ResponseEntity<>(boardService.addSection(boardId, dto, authContext), HttpStatus.CREATED);
    }

    @PatchMapping("/{boardId}/sections/{sectionId}")
    @Operation(summary = "Update board section")
    public ResponseEntity<BoardSectionDTO> updateSection(
            @PathVariable UUID boardId,
            @PathVariable UUID sectionId,
            AuthContext authContext,
            @RequestBody BoardSectionUpdateDTO dto
    ) {
        return ResponseEntity.ok(boardService.updateSection(boardId, sectionId, dto, authContext));
    }

    @DeleteMapping("/{boardId}/sections/{sectionId}")
    @Operation(summary = "Delete board section")
    public ResponseEntity<Void> deleteSection(
            @PathVariable UUID boardId,
            @PathVariable UUID sectionId,
            AuthContext authContext
    ) {
        boardService.deleteSection(boardId, sectionId, authContext);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}/shares")
    @Operation(summary = "Get board shares")
    public ResponseEntity<List<BoardShareDTO>> getBoardShares(
            @PathVariable UUID boardId,
            AuthContext authContext
    ) {
        return ResponseEntity.ok(boardService.getBoardShares(boardId, authContext));
    }

    @PostMapping("/{boardId}/shares")
    @Operation(summary = "Share board")
    public ResponseEntity<BoardShareDTO> addBoardShare(
            @PathVariable UUID boardId,
            AuthContext authContext,
            @Valid @RequestBody BoardShareRequestDTO dto
    ) {
        return new ResponseEntity<>(boardService.addBoardShare(boardId, dto, authContext), HttpStatus.CREATED);
    }

    @PatchMapping("/{boardId}/shares/{sharedUserId}")
    @Operation(summary = "Update board share")
    public ResponseEntity<BoardShareDTO> updateBoardShare(
            @PathVariable UUID boardId,
            @PathVariable UUID sharedUserId,
            AuthContext authContext,
            @Valid @RequestBody BoardShareUpdateDTO dto
    ) {
        return ResponseEntity.ok(boardService.updateBoardShare(boardId, sharedUserId, dto, authContext));
    }

    @DeleteMapping("/{boardId}/shares/{sharedUserId}")
    @Operation(summary = "Remove board share")
    public ResponseEntity<Void> removeBoardShare(
            @PathVariable UUID boardId,
            @PathVariable UUID sharedUserId,
            AuthContext authContext
    ) {
        boardService.removeBoardShare(boardId, sharedUserId, authContext);
        return ResponseEntity.noContent().build();
    }
}
