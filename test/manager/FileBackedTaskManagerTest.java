package manager;

import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;
    private LocalDateTime startTime;
    private Duration duration;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempFile);  // Исправлено - возвращаем реальный менеджер
    }

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test_tasks", ".csv");
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(30);
        super.setUp();  // Вызывает createTaskManager() и инициализирует taskManager
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    @DisplayName("Сохранение и загрузка пустого менеджера")
    void shouldSaveAndLoadEmptyManager() {
        taskManager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertTrue(loaded.getAllTasks().isEmpty()),
                () -> assertTrue(loaded.getAllEpics().isEmpty()),
                () -> assertTrue(loaded.getAllSubtasks().isEmpty()),
                () -> assertTrue(loaded.getHistory().isEmpty())
        );
    }

    @Test
    @DisplayName("Сохранение и загрузка задач с историей")
    void shouldSaveAndLoadTasksWithHistory() throws TimeOverlapException {
        Task task = new Task("Task", "Desc", Status.NEW, startTime, duration);
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());

        taskManager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertEquals(1, loaded.getAllTasks().size()),
                () -> assertEquals(1, loaded.getHistory().size()),
                () -> assertEquals(task, loaded.getTaskById(task.getId()))
        );
    }

}