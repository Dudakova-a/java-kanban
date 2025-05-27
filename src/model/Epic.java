package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private transient List<Subtask> subtasks;

    public Epic(int id, String name, String description, Status status,
                LocalDateTime startTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
    }

    public Epic(String name, String description) {
        super(1, name, description, Status.NEW, null, null); // 0 как маркер до присвоения ID
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId <= 0) {
            throw new IllegalArgumentException("ID подзадачи должно быть положительным");
        }
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public Duration getDuration() {
        if (subtasks == null || subtasks.isEmpty()) {
            return super.getDuration() != null ? super.getDuration() : Duration.ZERO;
        }
        return subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtasks == null || subtasks.isEmpty()) {
            return super.getStartTime();
        }
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtasks == null || subtasks.isEmpty()) {
            return super.getEndTime();
        }
        return subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() + "m" : "null") +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}