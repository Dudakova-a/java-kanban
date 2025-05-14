package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;

import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;


class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        // Сохраняем пустой менеджер
        FileBackedTaskManager savedManager = new FileBackedTaskManager(tempFile);
        savedManager.save(); // Вызываем save() у конкретного экземпляра

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
        assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void testFileFormat() throws IOException {
        // Создаем тестовые данные
        Task task = new Task("Task", "Description", Status.NEW);
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Sub description", Status.DONE, epic.getId());
        manager.createTask(task);
        manager.createSubtask(subtask);

        // Получаем все строки файла
        List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);

        // Проверяем заголовок
        assertEquals("id,type,name,status,description,epic", lines.get(0),
                "Некорректный заголовок");

        // Проверяем формат задачи (вторая строка)
        assertTrue(lines.get(1).matches("\\d+,TASK,Task,NEW,Description,"),
                "Некорректный формат задачи: " + lines.get(1));

        // Проверяем формат эпика (третья строка)
        assertTrue(lines.get(2).matches("\\d+,EPIC,Epic,(NEW|IN_PROGRESS|DONE),Epic description,"),
                "Некорректный формат эпика: " + lines.get(2));

        // Проверяем формат подзадачи (четвертая строка)
        assertTrue(lines.get(3).matches("\\d+,SUBTASK,Subtask,DONE,Sub description,\\d+"),
                "Некорректный формат подзадачи: " + lines.get(3));

        // Проверяем разделитель (пятая строка)
        assertEquals("", lines.get(4), "Отсутствует разделитель перед историей");

        // Проверяем историю (шестая строка)
        if (lines.size() > 5) {
            assertTrue(lines.get(5).matches("\\d+(,\\d+)*"),
                    "Некорректный формат истории: " + lines.get(5));
        }
    }

    @Test
    void testLoadTasksWithAllFields() {
        // Создаем тестовые данные
        Task task = new Task("Task", "Description", Status.IN_PROGRESS);
        Epic epic = new Epic("Epic", "Epic desc");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Sub desc", Status.DONE, epic.getId());

        manager.createTask(task);
        manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем восстановленные задачи
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertAll(
                () -> assertEquals(task.getName(), loadedTask.getName(), "Не совпадает имя задачи"),
                () -> assertEquals(task.getDescription(), loadedTask.getDescription(), "Не совпадает описание"),
                () -> assertEquals(task.getStatus(), loadedTask.getStatus(), "Не совпадает статус")
        );

        // Проверяем подзадачи
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        assertAll(() -> assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(), "Не совпадает EpicId"),
                () -> assertEquals(epic.getId(), loadedSubtask.getEpicId(), "Неверная связь с эпиком")
        );
    }

    @Test
    void testHistoryPreservation() {
        // Создаем задачи
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        // Формируем историю в определенном порядке
        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        // Проверяем историю
        List<Task> history = loadedManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");

        // Дополнительные проверки порядка
        if (history.size() == 2) {
            assertEquals(task2.getId(), history.get(0).getId(), "Нарушен порядок истории");
            assertEquals(task1.getId(), history.get(1).getId(), "Нарушен порядок истории");
        }
    }

}


