package com.pm.todoservice.service;

import com.pm.todoservice.dto.SubtaskDTO;
import com.pm.todoservice.model.TodoSubtask;
import com.pm.todoservice.model.enums.TodoPriority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TodoValidationService {

    public void validateSchedule(LocalDateTime dueDate, LocalDateTime remindAt) {
        if (dueDate != null && remindAt != null && remindAt.isAfter(dueDate)) {
            throw new RuntimeException("Reminder time cannot be after due date");
        }
    }

    public TodoPriority resolvePriority(TodoPriority priority) {
        return priority != null ? priority : TodoPriority.MEDIUM;
    }

    public String normalizeQueryFilter(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String normalized = category.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 100) {
            throw new RuntimeException("Category cannot exceed 100 characters");
        }
        return normalized;
    }

    public Set<String> normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalizedTags = new HashSet<>();
        for (String tag : tags) {
            if (tag == null) {
                continue;
            }
            String normalizedTag = tag.trim();
            if (normalizedTag.isEmpty()) {
                continue;
            }
            if (normalizedTag.length() > 50) {
                throw new RuntimeException("Each tag must be between 1 and 50 characters");
            }
            normalizedTags.add(normalizedTag);
        }
        return normalizedTags;
    }

    public String normalizePatchTitle(String title) {
        return normalizeTitle(title, "Title must be between 1 and 255 characters");
    }

    public String normalizeSubtaskTitle(String title) {
        return normalizeTitle(title, "Subtask title must be between 1 and 255 characters");
    }

    public List<TodoSubtask> normalizeSubtasks(List<SubtaskDTO> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        List<TodoSubtask> normalizedSubtasks = new ArrayList<>();
        for (SubtaskDTO subtaskDTO : subtasks) {
            if (subtaskDTO == null) {
                continue;
            }

            String title = normalizeSubtaskTitle(subtaskDTO.getTitle());
            boolean completed = subtaskDTO.getCompleted() != null && subtaskDTO.getCompleted();
            TodoSubtask subtask = new TodoSubtask(title, completed);
            if (subtaskDTO.getId() != null) {
                subtask.setId(subtaskDTO.getId());
            }
            normalizedSubtasks.add(subtask);
        }

        return normalizedSubtasks;
    }

    private String normalizeTitle(String title, String message) {
        if (title == null) {
            throw new RuntimeException(message);
        }
        String normalized = title.trim();
        if (normalized.isEmpty() || normalized.length() > 255) {
            throw new RuntimeException(message);
        }
        return normalized;
    }

    public void validatePatchDescription(String description) {
        if (description.length() > 1000) {
            throw new RuntimeException("Description cannot exceed 1000 characters");
        }
    }
}
