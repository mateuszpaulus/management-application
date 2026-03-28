package com.pm.todoservice.service;

import com.pm.todoservice.dto.*;
import com.pm.todoservice.model.Board;
import com.pm.todoservice.model.BoardSection;
import com.pm.todoservice.model.BoardShare;
import com.pm.todoservice.repository.BoardRepository;
import com.pm.todoservice.repository.BoardSectionRepository;
import com.pm.todoservice.repository.BoardShareRepository;
import com.pm.todoservice.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardSectionRepository boardSectionRepository;
    private final BoardShareRepository boardShareRepository;
    private final BoardAccessService boardAccessService;

    public BoardService(
            BoardRepository boardRepository,
            BoardSectionRepository boardSectionRepository,
            BoardShareRepository boardShareRepository,
            BoardAccessService boardAccessService
    ) {
        this.boardRepository = boardRepository;
        this.boardSectionRepository = boardSectionRepository;
        this.boardShareRepository = boardShareRepository;
        this.boardAccessService = boardAccessService;
    }

    @Transactional
    public BoardDTO createBoard(BoardCreateDTO dto, AuthContext authContext) {
        Board board = new Board();
        board.setName(dto.getName().trim());
        board.setOwnerUserId(authContext.userId());
        board.setArchived(false);

        Board saved = boardRepository.save(board);
        return toBoardDto(saved, List.of(), List.of());
    }

    public List<BoardDTO> getBoards(AuthContext authContext) {
        List<Board> boards = authContext.isAdmin()
                ? boardRepository.findAll()
                : getBoardsForUser(authContext.userId());

        return mapBoardsWithSectionsAndShares(boards);
    }

    public BoardDTO getBoard(UUID boardId, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateReadBoard(board, authContext);

        List<BoardSection> sections = boardSectionRepository.findByBoardIdOrderByPositionAsc(boardId);
        List<BoardShare> shares = boardShareRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
        return toBoardDto(board, sections, shares);
    }

    @Transactional
    public BoardDTO updateBoard(UUID boardId, BoardUpdateDTO dto, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateEditBoard(board, authContext);

        if (dto.getName() != null) {
            String normalized = dto.getName().trim();
            if (normalized.isEmpty() || normalized.length() > 150) {
                throw new RuntimeException("Board name must be between 1 and 150 characters");
            }
            board.setName(normalized);
        }

        if (dto.getArchived() != null) {
            board.setArchived(dto.getArchived());
        }

        Board saved = boardRepository.save(board);
        List<BoardSection> sections = boardSectionRepository.findByBoardIdOrderByPositionAsc(boardId);
        List<BoardShare> shares = boardShareRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
        return toBoardDto(saved, sections, shares);
    }

    @Transactional
    public BoardSectionDTO addSection(UUID boardId, BoardSectionCreateDTO dto, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateEditBoard(board, authContext);

        BoardSection section = new BoardSection();
        section.setBoardId(boardId);
        section.setName(dto.getName().trim());
        section.setPosition(dto.getPosition() != null ? dto.getPosition() : 0);
        BoardSection saved = boardSectionRepository.save(section);
        return toSectionDto(saved);
    }

    @Transactional
    public BoardSectionDTO updateSection(UUID boardId, UUID sectionId, BoardSectionUpdateDTO dto, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateEditBoard(board, authContext);

        BoardSection section = findSectionOrThrow(sectionId);
        if (!boardId.equals(section.getBoardId())) {
            throw new RuntimeException("Section does not belong to this board");
        }

        if (dto.getName() != null) {
            String normalized = dto.getName().trim();
            if (normalized.isEmpty() || normalized.length() > 120) {
                throw new RuntimeException("Section name must be between 1 and 120 characters");
            }
            section.setName(normalized);
        }
        if (dto.getPosition() != null) {
            section.setPosition(dto.getPosition());
        }

        return toSectionDto(boardSectionRepository.save(section));
    }

    @Transactional
    public void deleteSection(UUID boardId, UUID sectionId, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateEditBoard(board, authContext);

        BoardSection section = findSectionOrThrow(sectionId);
        if (!boardId.equals(section.getBoardId())) {
            throw new RuntimeException("Section does not belong to this board");
        }

        boardSectionRepository.delete(section);
    }

    public List<BoardShareDTO> getBoardShares(UUID boardId, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateManageBoard(board, authContext);

        return boardShareRepository.findByBoardIdOrderByCreatedAtDesc(boardId)
                .stream()
                .map(this::toShareDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardShareDTO addBoardShare(UUID boardId, BoardShareRequestDTO dto, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateManageBoard(board, authContext);

        if (board.getOwnerUserId().equals(dto.getSharedWithUserId())) {
            throw new RuntimeException("Owner already has full access to this board");
        }
        if (boardShareRepository.existsByBoardIdAndSharedWithUserId(boardId, dto.getSharedWithUserId())) {
            throw new RuntimeException("Board is already shared with this user");
        }

        BoardShare share = new BoardShare();
        share.setBoardId(boardId);
        share.setSharedWithUserId(dto.getSharedWithUserId());
        share.setPermission(dto.getPermission());
        share.setCreatedBy(authContext.userId());

        return toShareDto(boardShareRepository.save(share));
    }

    @Transactional
    public BoardShareDTO updateBoardShare(UUID boardId, UUID sharedUserId, BoardShareUpdateDTO dto, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateManageBoard(board, authContext);

        BoardShare share = boardShareRepository.findByBoardIdAndSharedWithUserId(boardId, sharedUserId)
                .orElseThrow(() -> new RuntimeException("Board share not found for this user"));
        share.setPermission(dto.getPermission());
        return toShareDto(boardShareRepository.save(share));
    }

    @Transactional
    public void removeBoardShare(UUID boardId, UUID sharedUserId, AuthContext authContext) {
        Board board = findBoardOrThrow(boardId);
        boardAccessService.validateManageBoard(board, authContext);

        BoardShare share = boardShareRepository.findByBoardIdAndSharedWithUserId(boardId, sharedUserId)
                .orElseThrow(() -> new RuntimeException("Board share not found for this user"));
        boardShareRepository.delete(share);
    }

    public Optional<BoardSection> findSectionById(UUID sectionId) {
        return boardSectionRepository.findById(sectionId);
    }

    public Set<UUID> findSectionIdsByBoardIds(Collection<UUID> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Set.of();
        }
        return boardSectionRepository.findIdsByBoardIdIn(boardIds);
    }

    public Set<UUID> getReadableBoardIds(AuthContext authContext) {
        if (authContext.isAdmin()) {
            return Set.of();
        }

        Set<UUID> shared = boardAccessService.getReadableBoardIds(authContext);
        Set<UUID> owned = boardRepository.findByOwnerUserId(authContext.userId()).stream()
                .map(Board::getId)
                .collect(Collectors.toSet());

        Set<UUID> all = new HashSet<>(shared);
        all.addAll(owned);
        return all;
    }

    public Set<UUID> getEditableBoardIds(AuthContext authContext) {
        if (authContext.isAdmin()) {
            return Set.of();
        }

        Set<UUID> shared = boardAccessService.getEditableBoardIds(authContext);
        Set<UUID> owned = boardRepository.findByOwnerUserId(authContext.userId()).stream()
                .map(Board::getId)
                .collect(Collectors.toSet());

        Set<UUID> all = new HashSet<>(shared);
        all.addAll(owned);
        return all;
    }

    private List<Board> getBoardsForUser(UUID userId) {
        Set<UUID> sharedBoardIds = boardShareRepository.findBoardIdsBySharedWithUserIdAndPermissionIn(
                userId,
                List.of(com.pm.todoservice.model.enums.BoardSharePermission.VIEW, com.pm.todoservice.model.enums.BoardSharePermission.EDIT)
        );

        List<Board> ownedBoards = boardRepository.findByOwnerUserId(userId);
        List<Board> sharedBoards = sharedBoardIds.isEmpty()
                ? List.of()
                : boardRepository.findAllById(sharedBoardIds);

        Map<UUID, Board> merged = new LinkedHashMap<>();
        ownedBoards.forEach(board -> merged.put(board.getId(), board));
        sharedBoards.forEach(board -> merged.put(board.getId(), board));
        return new ArrayList<>(merged.values());
    }

    private List<BoardDTO> mapBoardsWithSectionsAndShares(List<Board> boards) {
        if (boards.isEmpty()) {
            return List.of();
        }

        Set<UUID> boardIds = boards.stream().map(Board::getId).collect(Collectors.toSet());
        Map<UUID, List<BoardSection>> sectionsByBoardId = boardSectionRepository.findByBoardIdIn(boardIds).stream()
                .collect(Collectors.groupingBy(BoardSection::getBoardId));
        Map<UUID, List<BoardShare>> sharesByBoardId = boardShareRepository.findAll().stream()
                .filter(share -> boardIds.contains(share.getBoardId()))
                .collect(Collectors.groupingBy(BoardShare::getBoardId));

        return boards.stream()
                .map(board -> toBoardDto(
                        board,
                        sectionsByBoardId.getOrDefault(board.getId(), List.of()),
                        sharesByBoardId.getOrDefault(board.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    private Board findBoardOrThrow(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));
    }

    private BoardSection findSectionOrThrow(UUID sectionId) {
        return boardSectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
    }

    private BoardDTO toBoardDto(Board board, List<BoardSection> sections, List<BoardShare> shares) {
        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId());
        dto.setName(board.getName());
        dto.setOwnerUserId(board.getOwnerUserId());
        dto.setArchived(board.getArchived());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setUpdatedAt(board.getUpdatedAt());
        dto.setShared(!shares.isEmpty());
        dto.setSharedWithUserIds(shares.stream().map(BoardShare::getSharedWithUserId).collect(Collectors.toList()));
        dto.setSections(sections.stream().map(this::toSectionDto).collect(Collectors.toList()));
        return dto;
    }

    private BoardSectionDTO toSectionDto(BoardSection section) {
        return new BoardSectionDTO(
                section.getId(),
                section.getBoardId(),
                section.getName(),
                section.getPosition()
        );
    }

    private BoardShareDTO toShareDto(BoardShare share) {
        return new BoardShareDTO(
                share.getId(),
                share.getBoardId(),
                share.getSharedWithUserId(),
                share.getPermission(),
                share.getCreatedBy(),
                share.getCreatedAt()
        );
    }
}
