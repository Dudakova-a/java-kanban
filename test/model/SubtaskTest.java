package model;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    // Тестовые данные
    private static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2023, 1, 1, 10, 0);
    private static final Duration TEST_DURATION = Duration.ofHours(2);
    private static final LocalDateTime DIFFERENT_START_TIME = LocalDateTime.of(2023, 2, 1, 15, 30);
    private static final Duration DIFFERENT_DURATION = Duration.ofMinutes(45);

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(
                1,                      // id
                "Subtask 1",            // name
                "Description",           // description
                Status.NEW,             // status
                TEST_START_TIME,         // startTime
                TEST_DURATION,           // duration
                2                       // epicId
        );

        Subtask subtask2 = new Subtask(
                1,                      // тот же id
                "Subtask 2",            // другое имя
                "Different description", // другое описание
                Status.DONE,            // другой статус
                DIFFERENT_START_TIME,    // другое время начала
                DIFFERENT_DURATION,     // другая продолжительность
                3                       // другой epicId
        );

        assertEquals(subtask1, subtask2,
                "Подзадачи с одинаковым id должны быть равны, несмотря на различия в других полях");
    }

    @Test
    void subtaskShouldInheritFromTask() {
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                Status.NEW,
                TEST_START_TIME,
                TEST_DURATION,
                2
        );

        assertInstanceOf(Task.class, subtask,
                "Subtask должен наследоваться от Task");
    }

    @Test
    void shouldReturnCorrectEpicId() {
        int epicId = 5;
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                Status.NEW,
                TEST_START_TIME,
                TEST_DURATION,
                epicId
        );

        assertEquals(epicId, subtask.getEpicId(),
                "Неверно возвращается epicId");
    }

    @Test
    void subtaskShouldNotBeEqualToNull() {
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                Status.NEW,
                TEST_START_TIME,
                TEST_DURATION,
                2
        );

        assertNotEquals(null, subtask,
                "Подзадача не должна быть равна null");
    }

    @Test
    void subtaskShouldNotBeEqualToObjectOfDifferentClass() {
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                Status.NEW,
                TEST_START_TIME,
                TEST_DURATION,
                2
        );

        Object differentObject = new Object();
        assertNotEquals(differentObject, subtask,
                "Подзадача не должна быть равна объекту другого класса");
    }

    @Test
    void subtasksWithSameIdShouldHaveSameHashCode() {
        Subtask subtask1 = new Subtask(
                1,
                "Subtask 1",
                "Description",
                Status.NEW,
                TEST_START_TIME,
                TEST_DURATION,
                2
        );

        Subtask subtask2 = new Subtask(
                1,
                "Subtask 2",
                "Different description",
                Status.DONE,
                DIFFERENT_START_TIME,
                DIFFERENT_DURATION,
                3
        );

        assertEquals(subtask1.hashCode(), subtask2.hashCode(),
                "Хэш-коды подзадач с одинаковым id должны совпадать");
    }
}