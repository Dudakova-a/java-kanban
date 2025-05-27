package manager;

import manager.TaskManager;
import model.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    protected LocalDateTime startTime;  // Общее время начала для тестов
    protected Duration duration;  // Общая продолжительность для тестов

    protected Task task;
    protected Epic epic;
    protected Subtask subtask1;
    protected Subtask subtask2;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();

        task = new Task("Task", "Description", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));

        epic = new Epic("Epic", "Description");

        subtask1 = new Subtask("Subtask 1", "Description", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15), epic.getId());

        subtask2 = new Subtask("Subtask 2", "Description", Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(45), epic.getId());
    }

    /* Тесты для Task */

    @Test
    void shouldCreateAndGetTask() throws TimeOverlapException {
        // Создаем задачу и получаем её объект
        Task createdTask = taskManager.createTask(task);

        // Получаем ID созданной задачи
        final int taskId = createdTask.getId();

        // Получаем задачу для проверки
        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldUpdateTask() throws TimeOverlapException {
        final int taskId = taskManager.createTask(task).getId();
        Task updatedTask = new Task("Updated", "Updated", Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(3), Duration.ofMinutes(20));
        updatedTask.setId(taskId);

        taskManager.updateTask(updatedTask);

        assertEquals(updatedTask, taskManager.getTaskById(taskId), "Задача не обновилась");
    }

    @Test
    void shouldDeleteTask() throws TimeOverlapException {
        // Создаем задачу (метод createTask возвращает void)
        taskManager.createTask(task);

        // Получаем ID созданной задачи (предполагая, что она получает первый доступный ID)
        final int taskId = task.getId();

        // Удаляем задачу
        taskManager.deleteTaskById(taskId);

        // Проверяем что задача удалилась
        assertNull(taskManager.getTaskById(taskId), "Задача не удалилась");
    }

    @Test
    void shouldCreateAndGetEpic() throws TimeOverlapException {
        // Создаем эпик и получаем его ID
        Epic createdEpic = taskManager.createEpic(epic);  // предполагаем, что epic уже проинициализирован
        final int epicId = createdEpic.getId();

        // Проверяем, что эпик сохранился
        final Epic savedEpic = taskManager.getEpicById(epicId);
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
    }

    @Test
    void shouldUpdateEpic() {
        // Создаем эпик
        Epic createdEpic = taskManager.createEpic(epic);
        final int epicId = createdEpic.getId();

        // Обновляем его
        Epic updatedEpic = new Epic("Updated", "Updated");
        updatedEpic.setId(epicId);  // важно сохранить тот же ID

        taskManager.updateEpic(updatedEpic);

        // Проверяем, что обновление прошло
        assertEquals(updatedEpic, taskManager.getEpicById(epicId), "Эпик не обновился");
    }

    @Test
    void shouldCreateAndGetSubtask() throws TimeOverlapException {
        // Создаем эпик (подзадача не может существовать без эпика)
        Epic createdEpic = taskManager.createEpic(epic);

        // Создаем подзадачу и получаем её ID
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        final int subtaskId = createdSubtask.getId();

        // Проверяем, что подзадача сохранилась
        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask1, savedSubtask, "Подзадачи не совпадают");
    }

    @Test
    void shouldUpdateSubtask() throws TimeOverlapException {
        // Создаем эпик и подзадачу
        Epic createdEpic = taskManager.createEpic(epic);
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        final int subtaskId = createdSubtask.getId();

        // Обновляем подзадачу
        Subtask updatedSubtask = new Subtask("Updated", "Updated", Status.DONE,
                LocalDateTime.now().plusHours(4), Duration.ofMinutes(10), epic.getId());
        updatedSubtask.setId(subtaskId);  // важно сохранить тот же ID

        taskManager.updateSubtask(updatedSubtask);

        // Проверяем, что обновление прошло
        assertEquals(updatedSubtask, taskManager.getSubtaskById(subtaskId), "Подзадача не обновилась");
    }

    /* Тесты для получения списков */

    @Test
    void shouldGetAllTasks() throws TimeOverlapException {
        taskManager.createTask(task);
        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
    }

    @Test
    void shouldGetAllSubtasks() throws TimeOverlapException {
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        assertEquals(1, taskManager.getAllSubtasks().size(), "Неверное количество подзадач");
    }

    @Test
    void shouldGetAllEpics() {
        taskManager.createEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size(), "Неверное количество эпиков");
    }

    /* Тесты для удаления */

    @Test
    void shouldDeleteAllTasks() throws TimeOverlapException {
        taskManager.createTask(task);
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty(), "Задачи не удалились");
    }

    @Test
    void shouldDeleteAllSubtasks() throws TimeOverlapException {
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадачи не удалились");
    }

    /* Тесты для истории */

    @Test
    void shouldAddToHistory() throws TimeOverlapException {
        final int taskId = taskManager.createTask(task).getId();
        taskManager.getTaskById(taskId);

        assertEquals(1, taskManager.getHistory().size(), "Задача не добавилась в историю");
    }

    /* Граничные случаи */


    @Test
    void shouldNotUpdateNonExistentTask() {
        int nonExistentTaskId = 999;
        Task fakeTask = new Task(
                "Fake",
                "Fake",
                Status.NEW,
                LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(30)
        );
        fakeTask.setId(nonExistentTaskId);

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateTask(fakeTask)
        );

        assertEquals("Task with id 999 does not exist", exception.getMessage());
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask subtaskWithoutEpic = new Subtask("Subtask", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30), 999);

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.createSubtask(subtaskWithoutEpic)
        );

        assertEquals("Эпик не существует", exception.getMessage());
    }

    /* Тесты для временных параметров */

    @Test
    void shouldDetectTimeOverlap() throws TimeOverlapException {
        taskManager.createTask(task);
        Task overlappingTask = new Task("Overlap", "Overlap", Status.NEW,
                task.getStartTime().plusMinutes(10), Duration.ofMinutes(20));

        assertThrows(TimeOverlapException.class, () -> taskManager.createTask(overlappingTask));
    }

    @Test
    void shouldGetPrioritizedTasks() throws TimeOverlapException {
        // 1. Создаем тестовые данные
        LocalDateTime now = LocalDateTime.now();

        // 2. Сначала создаем эпик
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));

        // 3. Создаем задачи с разным временем начала
        Task task1 = taskManager.createTask(
                new Task("Task 1", "Description", Status.NEW, now.plusHours(2), Duration.ofMinutes(30)));

        Task task2 = taskManager.createTask(
                new Task("Task 2", "Description", Status.NEW, now.plusHours(1), Duration.ofMinutes(15)));

        // 4. Создаем подзадачу (если нужно)
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Description", Status.NEW, now.plusHours(3), Duration.ofMinutes(20), epic.getId()));

        // 5. Получаем приоритетный список
        List<Task> prioritized = taskManager.getPrioritizedTasks();

        // 6. Проверяем
        assertEquals(3, prioritized.size(), "Неверное количество задач");

        // Проверяем порядок: task2 (раньше) -> task1 -> subtask
        assertEquals(task2.getId(), prioritized.get(0).getId(), "Первая задача неверная");
        assertEquals(task1.getId(), prioritized.get(1).getId(), "Вторая задача неверная");
        assertEquals(subtask.getId(), prioritized.get(2).getId(), "Третья задача неверная");
    }
}