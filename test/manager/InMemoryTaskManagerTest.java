package manager;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import manager.TimeOverlapException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateTask() throws TimeOverlapException {
        Task task = new Task("Task", "Test", Status.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofHours(1));
        Task created = manager.createTask(task);

        assertNotNull(created);
        assertEquals(1, created.getId());
        assertEquals(1, manager.getAllTasks().size());
    }

    @Test
    void shouldThrowWhenTasksOverlap() throws TimeOverlapException {
        Task task1 = new Task("Task1", "Test1", Status.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofHours(2));
        Task task2 = new Task("Task2", "Test2", Status.NEW,
                LocalDateTime.of(2023, 1, 1, 11, 0), Duration.ofHours(1));

        manager.createTask(task1);
        assertThrows(TimeOverlapException.class, () -> manager.createTask(task2));
    }

}