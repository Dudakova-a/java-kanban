package model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    // Можно вынести общие данные в константы
    private static final int TEST_ID = 1;
    private static final LocalDateTime TIME_1 = LocalDateTime.of(2023, 1, 1, 10, 0);
    private static final LocalDateTime TIME_2 = LocalDateTime.of(2023, 2, 2, 15, 30);

    @Test
    void tasksEqualityDependsOnlyOnId() {
        Task task1 = new Task(TEST_ID, "Task 1", "Desc 1", Status.NEW, TIME_1, Duration.ofHours(2));
        Task task2 = new Task(TEST_ID, "Task 2", "Desc 2", Status.DONE, TIME_2, Duration.ofMinutes(15));

        assertAll(
                () -> assertEquals(task1, task2),
                () -> assertEquals(task1.hashCode(), task2.hashCode()),
                () -> assertNotEquals(task1.getDescription(), task2.getDescription())
        );
    }

    @Test
    void taskShouldNotBeEqualToNull() {
        Task task = new Task(
                1,
                "Task",
                "Description",
                Status.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0),
                Duration.ofHours(2)
        );

        assertNotEquals(null, task, "Задача не должна быть равна null");
    }

    @Test
    void taskShouldNotBeEqualToObjectOfDifferentClass() {
        Task task = new Task(
                1,
                "Task",
                "Description",
                Status.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0),
                Duration.ofHours(2)
        );

        Object differentObject = new Object();
        assertNotEquals(differentObject, task,
                "Задача не должна быть равна объекту другого класса");
    }

    @Test
    void tasksWithSameIdShouldHaveSameHashCode() {
        Task task1 = new Task(
                1,
                "Task 1",
                "Description",
                Status.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0),
                Duration.ofHours(2)
        );

        Task task2 = new Task(
                1,
                "Task 2",
                "Different description",
                Status.IN_PROGRESS,
                LocalDateTime.of(2023, 2, 1, 15, 30),
                Duration.ofMinutes(45)
        );

        assertEquals(task1.hashCode(), task2.hashCode(),
                "Хэш-коды задач с одинаковым id должны совпадать");
    }
}