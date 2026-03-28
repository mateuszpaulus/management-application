package com.pm.todoservice.repository;

import com.pm.todoservice.model.BoardSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BoardSectionRepository extends JpaRepository<BoardSection, UUID> {

    List<BoardSection> findByBoardIdOrderByPositionAsc(UUID boardId);

    List<BoardSection> findByBoardIdIn(Collection<UUID> boardIds);

    @Query("select s.id from BoardSection s where s.boardId in :boardIds")
    Set<UUID> findIdsByBoardIdIn(@Param("boardIds") Collection<UUID> boardIds);
}
