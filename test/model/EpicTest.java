package model;

import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private final LocalDateTime testTime = LocalDateTime.now();
    private final Duration testDuration = Duration.ofMinutes(30);

    @Test
    void shouldCreateEpicWithRequiredFields() {
        Epic epic = new Epic("Test Epic", "Test Description");

        assertEquals(1, epic.getId());
        assertEquals("Test Epic", epic.getName());
        assertEquals("Test Description", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertNull(epic.getStartTime());
        assertEquals(Duration.ZERO, epic.getDuration());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldCreateEpicWithAllFields() {
        Epic epic = new Epic(1, "Test Epic", "Test Description",
                Status.NEW, testTime, testDuration);

        assertEquals(1, epic.getId());
        assertEquals(testTime, epic.getStartTime());
        assertEquals(testDuration, epic.getDuration());
    }

    @Test
    void shouldAddAndRemoveSubtaskIds() {
        Epic epic = new Epic("Test Epic", "Test Description");

        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        assertEquals(List.of(2, 3), epic.getSubtaskIds());

        epic.removeSubtaskId(2);
        assertEquals(List.of(3), epic.getSubtaskIds());
    }

    @Test
    void shouldNotAddInvalidSubtaskId() {
        Epic epic = new Epic("Test Epic", "Test Description");

        assertThrows(IllegalArgumentException.class, () -> epic.addSubtaskId(0));
        assertThrows(IllegalArgumentException.class, () -> epic.addSubtaskId(-1));
    }

    @Test
    void shouldCalculateDurationWithoutSubtasks() {
        Epic epic = new Epic(1, "Test Epic", "Test Description",
                Status.NEW, null, null);

        assertEquals(Duration.ZERO, epic.getDuration());
    }

    @Test
    void shouldCalculateDurationWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Desc 1", Status.NEW,
                testTime, Duration.ofMinutes(15), 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Desc 2", Status.NEW,
                testTime.plusHours(1), Duration.ofMinutes(20), 1);

        epic.setSubtasks(List.of(subtask1, subtask2));

        assertEquals(Duration.ofMinutes(35), epic.getDuration());
    }

    @Test
    void shouldCalculateStartTimeWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Desc 1", Status.NEW,
                testTime.plusHours(1), Duration.ofMinutes(15), 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Desc 2", Status.NEW,
                testTime, Duration.ofMinutes(20), 1);

        epic.setSubtasks(List.of(subtask1, subtask2));

        assertEquals(testTime, epic.getStartTime());
    }

    @Test
    void shouldReturnNullStartTimeWhenNoSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");

        assertNull(epic.getStartTime());
    }

    @Test
    void shouldCalculateEndTimeWithSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Desc 1", Status.NEW,
                testTime, Duration.ofMinutes(15), 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Desc 2", Status.NEW,
                testTime.plusHours(1), Duration.ofMinutes(20), 1);

        epic.setSubtasks(List.of(subtask1, subtask2));

        assertEquals(testTime.plusHours(1).plusMinutes(20), epic.getEndTime());
    }

    @Test
    void shouldReturnNullEndTimeWhenNoSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");

        assertNull(epic.getEndTime());
    }

    @Test
    void shouldHandleNullSubtasksInCalculations() {
        Epic epic = new Epic("Test Epic", "Test Description");
        epic.setSubtasks(null);

        assertEquals(Duration.ZERO, epic.getDuration());
        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldReturnCorrectToString() {
        Epic epic = new Epic(1, "Test Epic", "Test Description",
                Status.NEW, testTime, testDuration);
        epic.addSubtaskId(2);

        String expected = "Epic{id=1, name='Test Epic', description='Test Description', " +
                "status=NEW, startTime=" + testTime + ", duration=30m, subtaskIds=[2]}";
        assertEquals(expected, epic.toString());
    }
}