package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    private int nextId = 1;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean hasTimeOverlap(Task taskToCheck) {
        if (taskToCheck.getStartTime() == null) return false;
        return prioritizedTasks.stream()
                .filter(task -> task.getId() != taskToCheck.getId())
                .anyMatch(existing -> isTasksOverlap(existing, taskToCheck));
    }

    private boolean isTasksOverlap(Task a, Task b) {
        return a.getStartTime().isBefore(b.getEndTime()) &&
                b.getStartTime().isBefore(a.getEndTime());
    }

    private void updateEpicTimeFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            prioritizedTasks.remove(epic);
            if (epic.getStartTime() != null) {
                prioritizedTasks.add(epic);
            }
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        prioritizedTasks.removeIf(task -> tasks.containsKey(task.getId()));
        tasks.values().forEach(task -> historyManager.remove(task.getId()));
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        prioritizedTasks.removeIf(task -> subtasks.containsKey(task.getId()));
        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTimeFields(epic.getId());
        });
    }

    @Override
    public void deleteAllEpics() {
        prioritizedTasks.removeIf(task -> epics.containsKey(task.getId()));
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtaskIds().forEach(subtaskId -> {
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
            });
        });
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            epic.setSubtasks(getEpicSubtasks(id));
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Task createTask(Task task) throws TimeOverlapException {
        if (task == null) return task;
        if (hasTimeOverlap(task)) {
            throw new TimeOverlapException("Задача пересекается по времени");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) throws TimeOverlapException {
        if (subtask == null) throw new IllegalArgumentException("Подзадача не может быть null");
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик не существует");
        }
        if (hasTimeOverlap(subtask)) {
            throw new TimeOverlapException("Подзадача пересекается по времени");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
            updateEpicTimeFields(epic.getId());
        }
        updateEpicStatus(epic.getId());
        return subtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;
        if (epic.getId() != 0) { // Если ID уже задан (не временный)
            throw new IllegalArgumentException("Эпик не должен иметь заранее заданный ID");
        }
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateTask(Task task) throws TimeOverlapException {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task with id " + task.getId() + " does not exist");
        }
        if (hasTimeOverlap(task)) {
            throw new TimeOverlapException("Задача пересекается по времени");
        }
        Task oldTask = tasks.get(task.getId());
        if (oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TimeOverlapException {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        if (hasTimeOverlap(subtask)) {
            throw new TimeOverlapException("Подзадача пересекается по времени");
        }
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubtask);
        }
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        updateEpicStatus(subtask.getEpicId());
        updateEpicTimeFields(subtask.getEpicId());
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
                updateEpicTimeFields(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            if (epic.getStartTime() != null) {
                prioritizedTasks.remove(epic);
            }
            epic.getSubtaskIds().forEach(subtaskId -> {
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
                prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
            });
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();
        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) result.add(subtask);
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected int generateId() {
        return nextId++;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.NEW) allNew = false;
            if (subtask.getStatus() != Status.DONE) allDone = false;
        }

        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);
    }
}