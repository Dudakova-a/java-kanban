package manager;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;
    private Task task4;

    // Тестовые временные параметры
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2023, 1, 1, 10, 0);
    private static final Duration DURATION_1H = Duration.ofHours(1);
    private static final Duration DURATION_2H = Duration.ofHours(2);

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();

        task1 = new Task(
                1,
                "Task 1",
                "Description 1",
                Status.NEW,
                BASE_TIME,
                DURATION_1H
        );

        task2 = new Task(
                2,
                "Task 2",
                "Description 2",
                Status.IN_PROGRESS,
                BASE_TIME.plusHours(2),
                DURATION_2H
        );

        task3 = new Task(
                3,
                "Task 3",
                "Description 3",
                Status.DONE,
                BASE_TIME.plusHours(5),
                Duration.ofMinutes(30)
        );

        task4 = new Task(
                4,
                "Task 4",
                "Description 4",
                Status.NEW,
                BASE_TIME.plusDays(1),
                DURATION_1H
        );
    }

    @Test
    void shouldAddTasksToHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertIterableEquals(List.of(task1, task2), history, "Порядок задач нарушен");
    }

    @Test
    void shouldRemoveDuplicatesWhenAddingSameTask() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Дубликат

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Дубликат не был удалён");
        assertEquals(task2, history.get(0), "Первая задача не совпадает");
        assertEquals(task1, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не была удалена");
        assertEquals(task2, history.get(0), "Оставшаяся задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromMiddleOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromBeginningOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления");
        assertEquals(task2, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromEndOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void shouldMaintainOrderAfterMultipleOperations() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        historyManager.add(task4);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(4, history.size(), "Неверное количество задач в истории");
        assertIterableEquals(List.of(task1, task3, task4, task2), history, "Порядок задач нарушен");
    }

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой");

        historyManager.remove(999); // Несуществующий ID
        assertTrue(historyManager.getHistory().isEmpty(), "История должна остаться пустой");

        historyManager.add(null); // Null задача
        assertTrue(historyManager.getHistory().isEmpty(), "История должна игнорировать null");
    }


}