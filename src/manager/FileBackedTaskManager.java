package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    protected void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");

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
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : ""
        };
        return String.join(",", fields);
    }

    private String historyToString(List<Task> history) {
        StringBuilder sb = new StringBuilder();
        for (Task task : history) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(task.getId());
        }
        return sb.toString();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (file.length() == 0) {
            return manager;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isHistorySection = false;

            reader.readLine(); // Пропускаем заголовок

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistorySection = true;
                    continue;
                }

                if (!isHistorySection) {
                    Task task = fromString(line);
                    if (task != null) {
                        if (task instanceof Epic) {
                            manager.epics.put(task.getId(), (Epic) task);
                        } else if (task instanceof Subtask) {
                            manager.subtasks.put(task.getId(), (Subtask) task);
                        } else {
                            manager.tasks.put(task.getId(), task);
                        }
                    }
                } else {
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

            switch (type) {
                case "TASK":
                    return new Task(id, name, description, status);
                case "EPIC":
                    Epic epic = new Epic(id, name, description);
                    epic.setStatus(status);
                    return epic;
                case "SUBTASK":
                    String epicIdString = parts[5].trim();
                    if (epicIdString.isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId: " + value);
                    }
                    int epicId = Integer.parseInt(epicIdString);
                    return new Subtask(id, name, description, status, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный числовой формат в строке: " + value, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректное значение статуса в строке: " + value, e);
        }
    }
}
