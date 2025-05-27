package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            return null;
        }

        int newId = generateId();
        Epic newEpic = new Epic(newId, epic.getName(), epic.getDescription(),
                epic.getStatus(), epic.getStartTime(), epic.getDuration());

        // Копируем подзадачи, если они есть
        if (!epic.getSubtaskIds().isEmpty()) {
            newEpic.getSubtaskIds().addAll(epic.getSubtaskIds());
        }

        epics.put(newId, newEpic);
        save();
        return newEpic;
    }


    protected void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic,startTime,duration\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

            writer.write("\n" + historyToString(getHistory()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String toString(Task task) {
        String[] fields = {
                String.valueOf(task.getId()),
                task instanceof Epic ? "EPIC" : (task instanceof Subtask ? "SUBTASK" : "TASK"),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "",
                task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : "",
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : ""
        };
        return String.join(",", fields);
    }

    private String historyToString(List<Task> history) {
        return history.stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (file.length() == 0) {
            return manager;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isHistorySection = false;
            int maxId = 0;
            reader.readLine(); // Пропускаем заголовок

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistorySection = true;
                    continue;
                }

                if (!isHistorySection) {
                    Task task = fromString(line);
                    if (task != null) {
                        // Обновляем максимальный ID
                        if (task.getId() > maxId) {
                            maxId = task.getId();
                        }
                        if (task instanceof Epic) {
                            manager.epics.put(task.getId(), (Epic) task);
                        } else if (task instanceof Subtask) {
                            manager.subtasks.put(task.getId(), (Subtask) task);
                            // Добавляем ID подзадачи в эпик
                            Epic epic = manager.epics.get(((Subtask) task).getEpicId());
                            if (epic != null) {
                                epic.addSubtaskId(task.getId());
                            }
                        } else {
                            manager.tasks.put(task.getId(), task);
                        }
                    }
                } else {
                    // Восстановление истории
                    for (String id : line.split(",")) {
                        int taskId = Integer.parseInt(id.trim());
                        if (manager.tasks.containsKey(taskId)) {
                            manager.historyManager.add(manager.tasks.get(taskId));
                        } else if (manager.subtasks.containsKey(taskId)) {
                            manager.historyManager.add(manager.subtasks.get(taskId));
                        } else if (manager.epics.containsKey(taskId)) {
                            manager.historyManager.add(manager.epics.get(taskId));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split(",", -1);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Некорректная строка задачи: " + value);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String type = parts[1].trim();
            String name = parts[2].trim();
            Status status = Status.valueOf(parts[3].trim());
            String description = parts[4].trim();

            // Обработка временных параметров
            LocalDateTime startTime = parts.length > 6 && !parts[6].isEmpty() ?
                    LocalDateTime.parse(parts[6], DATE_TIME_FORMATTER) : null;
            Duration duration = parts.length > 7 && !parts[7].isEmpty() ?
                    Duration.ofMinutes(Long.parseLong(parts[7])) : null;

            switch (type) {
                case "TASK":
                    return new Task(id, name, description, status, startTime, duration);
                case "EPIC":
                    Epic epic = new Epic(id, name, description, status, startTime, duration);
                    return epic;
                case "SUBTASK":
                    if (parts.length < 6 || parts[5].isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId: " + value);
                    }
                    int epicId = Integer.parseInt(parts[5].trim());
                    return new Subtask(id, name, description, status, startTime, duration, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга строки: " + value, e);
        }
    }
}