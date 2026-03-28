package com.pm.todoservice.service;

import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.dto.TodoActivityDTO;
import com.pm.todoservice.dto.TodoShareDTO;
import com.pm.todoservice.model.Board;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.model.TodoShare;
import com.pm.todoservice.model.enums.TodoSharePermission;
import com.pm.todoservice.repository.BoardRepository;
import com.pm.todoservice.repository.BoardSectionRepository;
import com.pm.todoservice.repository.TodoRepository;
import com.pm.todoservice.repository.TodoShareRepository;
import com.pm.todoservice.security.AuthContext;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TodoQueryService {

    private final TodoRepository todoRepository;
    private final TodoAuthorizationService authorizationService;
    private final TodoValidationService validationService;
    private final TodoMapper todoMapper;
    private final TodoActivityService todoActivityService;
    private final TodoShareRepository todoShareRepository;
    private final BoardRepository boardRepository;
    private final BoardSectionRepository boardSectionRepository;
    private final BoardAccessService boardAccessService;

    public TodoQueryService(
            TodoRepository todoRepository,
            TodoAuthorizationService authorizationService,
            TodoValidationService validationService,
            TodoMapper todoMapper,
            TodoActivityService todoActivityService,
            TodoShareRepository todoShareRepository,
            BoardRepository boardRepository,
            BoardSectionRepository boardSectionRepository,
            BoardAccessService boardAccessService
    ) {
        this.todoRepository = todoRepository;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
        this.todoMapper = todoMapper;
        this.todoActivityService = todoActivityService;
        this.todoShareRepository = todoShareRepository;
        this.boardRepository = boardRepository;
        this.boardSectionRepository = boardSectionRepository;
        this.boardAccessService = boardAccessService;
    }

    public Page<TodoDTO> getAllTodos(
            AuthContext authContext,
            String category,
            String tag,
            Boolean completed,
            Boolean archived,
            String search,
            UUID boardId,
            UUID sectionId,
            Pageable pageable
    ) {
        String normalizedCategory = validationService.normalizeQueryFilter(category);
        String normalizedTag = validationService.normalizeQueryFilter(tag);
        String normalizedSearch = validationService.normalizeQueryFilter(search);

        Set<UUID> sharedTodoIds = authContext.isAdmin()
                ? Set.of()
                : todoShareRepository.findTodoIdsBySharedWithUserIdAndPermissionIn(
                authContext.userId(),
                List.of(TodoSharePermission.VIEW, TodoSharePermission.EDIT)
        );
        Set<UUID> readableSectionIds = resolveReadableSectionIds(authContext);

        Specification<Todo> specification = buildSpecification(
                authContext,
                normalizedCategory,
                normalizedTag,
                completed,
                archived,
                normalizedSearch,
                boardId,
                sectionId,
                sharedTodoIds,
                readableSectionIds
        );

        Page<TodoDTO> page = todoRepository.findAll(specification, pageable).map(todoMapper::toDto);
        enrichSharingMetadata(page.getContent());
        return page;
    }

    public List<TodoDTO> getAllTodosList(
            AuthContext authContext,
            String category,
            String tag,
            Boolean completed,
            Boolean archived,
            String search,
            UUID boardId,
            UUID sectionId,
            Sort sort
    ) {
        String normalizedCategory = validationService.normalizeQueryFilter(category);
        String normalizedTag = validationService.normalizeQueryFilter(tag);
        String normalizedSearch = validationService.normalizeQueryFilter(search);
        Set<UUID> sharedTodoIds = authContext.isAdmin()
                ? Set.of()
                : todoShareRepository.findTodoIdsBySharedWithUserIdAndPermissionIn(
                authContext.userId(),
                List.of(TodoSharePermission.VIEW, TodoSharePermission.EDIT)
        );
        Set<UUID> readableSectionIds = resolveReadableSectionIds(authContext);

        Specification<Todo> specification = buildSpecification(
                authContext,
                normalizedCategory,
                normalizedTag,
                completed,
                archived,
                normalizedSearch,
                boardId,
                sectionId,
                sharedTodoIds,
                readableSectionIds
        );

        List<TodoDTO> todos = todoRepository.findAll(specification, sort).stream().map(todoMapper::toDto).collect(Collectors.toList());
        enrichSharingMetadata(todos);
        return todos;
    }

    public TodoDTO getTodoById(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateReadAccess(todo, authContext);
        TodoDTO dto = todoMapper.toDto(todo);
        enrichSharingMetadata(List.of(dto));
        return dto;
    }

    public Todo getEntireTodoById(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateReadAccess(todo, authContext);
        return todo;
    }

    public List<TodoDTO> getTodosByUserId(UUID userId, AuthContext authContext) {
        authorizationService.validateUserScope(userId, authContext, "You can only access your own todos");
        return todoRepository.findByUserId(userId).stream().map(todoMapper::toDto).collect(Collectors.toList());
    }

    public List<TodoActivityDTO> getTodoActivity(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateReadAccess(todo, authContext);
        return todoActivityService.getTodoActivity(id);
    }

    public List<TodoShareDTO> getTodoShares(UUID id, AuthContext authContext) {
        Todo todo = findByIdOrThrow(id);
        authorizationService.validateOwnership(todo, authContext);

        return todoShareRepository.findByTodoIdOrderByCreatedAtDesc(id)
                .stream()
                .map(this::toShareDto)
                .collect(Collectors.toList());
    }

    private Todo findByIdOrThrow(UUID id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
    }

    private Specification<Todo> buildSpecification(
            AuthContext authContext,
            String category,
            String tag,
            Boolean completed,
            Boolean archived,
            String search,
            UUID boardId,
            UUID sectionId,
            Set<UUID> sharedTodoIds,
            Set<UUID> readableSectionIds
    ) {
        Specification<Todo> specification = (root, query, cb) -> cb.conjunction();

        if (!authContext.isAdmin()) {
            specification = specification.and((root, query, cb) -> {
                boolean hasSharedTodos = sharedTodoIds != null && !sharedTodoIds.isEmpty();
                boolean hasSharedSections = readableSectionIds != null && !readableSectionIds.isEmpty();

                if (!hasSharedTodos && !hasSharedSections) {
                    return cb.equal(root.get("userId"), authContext.userId());
                }

                if (hasSharedTodos && hasSharedSections) {
                    return cb.or(
                            cb.equal(root.get("userId"), authContext.userId()),
                            root.get("id").in(sharedTodoIds),
                            root.get("sectionId").in(readableSectionIds)
                    );
                }

                if (hasSharedTodos) {
                    return cb.or(
                            cb.equal(root.get("userId"), authContext.userId()),
                            root.get("id").in(sharedTodoIds)
                    );
                }

                return cb.or(
                        cb.equal(root.get("userId"), authContext.userId()),
                        root.get("sectionId").in(readableSectionIds)
                );
            });
        }

        if (boardId != null) {
            Set<UUID> sectionIdsForBoard = boardSectionRepository.findIdsByBoardIdIn(Set.of(boardId));
            if (sectionIdsForBoard.isEmpty()) {
                specification = specification.and((root, query, cb) -> cb.disjunction());
            } else {
                specification = specification.and((root, query, cb) -> root.get("sectionId").in(sectionIdsForBoard));
            }
        }

        if (sectionId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("sectionId"), sectionId));
        }

        if (category != null) {
            specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
        }

        if (tag != null) {
            specification = specification.and((root, query, cb) -> {
                Join<Todo, String> tagsJoin = root.join("tags", JoinType.INNER);
                assert query != null;
                query.distinct(true);
                return cb.equal(cb.lower(tagsJoin), tag.toLowerCase());
            });
        }

        if (completed != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("completed"), completed));
        }

        if (archived != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("archived"), archived));
        } else {
            specification = specification.and((root, query, cb) -> cb.isFalse(root.get("archived")));
        }

        if (search != null) {
            String pattern = "%" + search.toLowerCase() + "%";
            specification = specification.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(cb.lower(root.get("description")), pattern)
                    )
            );
        }

        return specification;
    }

    private Set<UUID> resolveReadableSectionIds(AuthContext authContext) {
        if (authContext.isAdmin()) {
            return Set.of();
        }

        Set<UUID> ownedBoardIds = boardRepository.findByOwnerUserId(authContext.userId()).stream()
                .map(Board::getId)
                .collect(Collectors.toSet());
        Set<UUID> sharedBoardIds = boardAccessService.getReadableBoardIds(authContext);

        Set<UUID> allBoardIds = new java.util.HashSet<>(ownedBoardIds);
        allBoardIds.addAll(sharedBoardIds);
        if (allBoardIds.isEmpty()) {
            return Set.of();
        }
        return boardSectionRepository.findIdsByBoardIdIn(allBoardIds);
    }

    private TodoShareDTO toShareDto(TodoShare share) {
        return new TodoShareDTO(
                share.getId(),
                share.getTodoId(),
                share.getSharedWithUserId(),
                share.getPermission(),
                share.getCreatedBy(),
                share.getCreatedAt()
        );
    }

    private void enrichSharingMetadata(List<TodoDTO> todos) {
        if (todos == null || todos.isEmpty()) {
            return;
        }

        Set<UUID> todoIds = todos.stream()
                .map(TodoDTO::getId)
                .collect(Collectors.toSet());

        Map<UUID, List<UUID>> sharesByTodoId = todoShareRepository.findByTodoIdIn(todoIds).stream()
                .collect(Collectors.groupingBy(
                        TodoShare::getTodoId,
                        Collectors.mapping(TodoShare::getSharedWithUserId, Collectors.toList())
                ));

        for (TodoDTO todo : todos) {
            List<UUID> sharedWith = sharesByTodoId.getOrDefault(todo.getId(), List.of());
            todo.setShared(!sharedWith.isEmpty());
            todo.setSharedWithUserIds(sharedWith);
        }
    }
}
