package com.pm.todoservice.service;

import com.pm.todoservice.dto.SubtaskDTO;
import com.pm.todoservice.dto.TodoDTO;
import com.pm.todoservice.model.Todo;
import com.pm.todoservice.model.TodoSubtask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TodoMapper {

    public TodoDTO toDto(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setCompleted(todo.getCompleted());
        dto.setUserId(todo.getUserId());
        dto.setDueDate(todo.getDueDate());
        dto.setRemindAt(todo.getRemindAt());
        dto.setPriority(todo.getPriority());
        dto.setCategory(todo.getCategory());
        dto.setTags(new HashSet<>(todo.getTags()));
        dto.setSubtasks(mapSubtasks(todo.getSubtasks()));
        dto.setCompletedSubtasks(countCompletedSubtasks(todo.getSubtasks()));
        dto.setTotalSubtasks(todo.getSubtasks() != null ? todo.getSubtasks().size() : 0);
        dto.setProgressPercent(calculateProgressPercent(todo.getSubtasks()));
        dto.setArchived(todo.getArchived());
        dto.setArchivedAt(todo.getArchivedAt());
        dto.setArchivedBy(todo.getArchivedBy());
        dto.setShared(false);
        dto.setSharedWithUserIds(new ArrayList<>());
        return dto;
    }

    private List<SubtaskDTO> mapSubtasks(List<TodoSubtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        return subtasks.stream()
                .map(subtask -> new SubtaskDTO(
                        subtask.getId(),
                        subtask.getTitle(),
                        subtask.getCompleted()
                ))
                .collect(Collectors.toList());
    }

    private int countCompletedSubtasks(List<TodoSubtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return 0;
        }
        return (int) subtasks.stream().filter(subtask -> Boolean.TRUE.equals(subtask.getCompleted())).count();
    }

    private int calculateProgressPercent(List<TodoSubtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return 0;
        }

        int total = subtasks.size();
        int completed = countCompletedSubtasks(subtasks);
        return (int) Math.round((completed * 100.0) / total);
    }
}
