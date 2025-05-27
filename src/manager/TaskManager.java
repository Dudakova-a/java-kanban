package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.Collection;
import java.util.List;

public interface TaskManager {
    // Получение списков всех задач
    Collection<Task> getAllTasks();

    Collection<Subtask> getAllSubtasks();

    Collection<Epic> getAllEpics();

    // Удаление всех задач
    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    // Получение задач по ID
    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    // Создание задач
    Task createTask(Task task) throws TimeOverlapException;

    Subtask createSubtask(Subtask subtask) throws TimeOverlapException;

    Epic createEpic(Epic epic);

    // Обновление задач
    void updateTask(Task task) throws TimeOverlapException;

    void updateSubtask(Subtask subtask) throws TimeOverlapException;

    void updateEpic(Epic epic);

    // Удаление задач по ID
    void deleteTaskById(int id);

    void deleteSubtaskById(int id);

    void deleteEpicById(int id);

    // Получение подзадач эпика
    List<Subtask> getEpicSubtasks(int epicId);

    // История просмотров
    List<Task> getHistory();

    // Приоритизированный список
    List<Task> getPrioritizedTasks();

    // Проверка пересечений по времени
    boolean hasTimeOverlap(Task taskToCheck);
}