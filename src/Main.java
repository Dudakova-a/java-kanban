import manager.FileBackedTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File storageFile = new File("tasks.csv");
        TaskManager manager = null;

        try {
            if (storageFile.exists()) {
                System.out.println("Загружаем задачи из файла...");
                manager = FileBackedTaskManager.loadFromFile(storageFile);
            } else {
                System.out.println("Создаем новый файл хранилища...");
                manager = new FileBackedTaskManager(storageFile);
            }

            // Проверяем, что менеджер был успешно создан
            if (manager == null) {
                throw new IllegalStateException("Не удалось инициализировать менеджер задач");
            }

            // Создание и тестирование задач
            testTaskOperations(manager);

        } catch (Exception e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testTaskOperations(TaskManager manager) {
        // Создание задач
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпика с подзадачами
        Epic epic1 = new Epic(0, "Epic 1", "Description Epic 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description Subtask 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description Subtask 2", Status.IN_PROGRESS, epic1.getId());
        Subtask subtask3 = new Subtask("Subtask 3", "Description Subtask 3", Status.DONE, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);

        // Создание эпика без подзадач
        Epic epic2 = new Epic("Epic 2", "Description Epic 2");
        manager.createEpic(epic2);

        // Тестирование истории просмотров
        testHistory(manager);
    }

    private static void testHistory(TaskManager manager) {
        System.out.println("\nТестирование истории просмотров:");

        // Получаем реальные ID созданных задач
        int task1Id = 1;  // Первая задача обычно получает ID 1
        int epic1Id = 3;   // Эпик после двух задач
        int subtask2Id = 5; // Вторая подзадача

        System.out.println("Запрос задачи 1");
        manager.getTaskById(task1Id);
        printHistory(manager);

        System.out.println("Запрос эпика 1");
        manager.getEpicById(epic1Id);
        printHistory(manager);

        System.out.println("Запрос подзадачи 2");
        manager.getSubtaskById(subtask2Id);
        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            // Чёткое разделение типов задач при выводе
            if (task instanceof Epic) {
                System.out.println("[Epic] " + task);
            } else if (task instanceof Subtask) {
                System.out.println("[Subtask] " + task);
            } else {
                System.out.println("[Task] " + task);
            }
        }
    }
}