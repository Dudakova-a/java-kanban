import manager.FileBackedTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

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

            if (manager == null) {
                throw new IllegalStateException("Не удалось инициализировать менеджер задач");
            }

            testTaskOperations(manager);
            testTimeFunctionality(manager);

        } catch (Exception e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testTaskOperations(TaskManager manager) {
        System.out.println("\n=== Тестирование базовых операций ===");

        // Создание задач с временными параметрами
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofHours(2));
        Task task2 = new Task("Task 2", "Description 2", Status.NEW,
                LocalDateTime.of(2023, 1, 1, 13, 0), Duration.ofHours(1));

        try {
            manager.createTask(task1);
            manager.createTask(task2);
            System.out.println("Задачи успешно созданы");
        } catch (Exception e) {
            System.out.println("Ошибка создания задач: " + e.getMessage());
        }

        // Создание эпика с подзадачами
        Epic epic1 = new Epic("Epic 1", "Description Epic 1");
        manager.createEpic(epic1);
        System.out.println("Создан эпик с ID: " + epic1.getId());
        // Убедимся, что ID эпика установлен правильно
        if (epic1.getId() <= 0) {
            throw new IllegalStateException("Эпик получил невалидный ID: " + epic1.getId());
        }

        Subtask subtask1 = new Subtask("Subtask 1", "Description Subtask 1", Status.NEW,
                LocalDateTime.of(2023, 1, 2, 9, 0), Duration.ofHours(3), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description Subtask 2", Status.IN_PROGRESS,
                LocalDateTime.of(2023, 1, 2, 13, 0), Duration.ofHours(2), epic1.getId());

        try {
            manager.createSubtask(subtask1);
            manager.createSubtask(subtask2);
            System.out.println("Подзадачи успешно созданы");
        } catch (Exception e) {
            System.out.println("Ошибка создания подзадач: " + e.getMessage());
        }

        // Тестирование пересечения времени
        System.out.println("\nПроверка пересечения времени:");
        Task overlappingTask = new Task("Overlapping Task", "Should fail", Status.NEW,
                LocalDateTime.of(2023, 1, 1, 11, 30), Duration.ofHours(1));

        try {
            manager.createTask(overlappingTask);
            System.out.println("ОШИБКА: Задача с пересекающимся временем была создана");
        } catch (Exception e) {
            System.out.println("УСПЕХ: " + e.getMessage());
        }
    }

    private static void testTimeFunctionality(TaskManager manager) {
        System.out.println("\n=== Тестирование временных параметров ===");

        // Вывод приоритетного списка задач
        System.out.println("Приоритетный список задач:");
        manager.getPrioritizedTasks().forEach(task -> {
            System.out.printf("%s: %s - %s (Длительность: %d мин)%n",
                    task.getClass().getSimpleName(),
                    task.getStartTime(),
                    task.getEndTime(),
                    task.getDuration().toMinutes());
        });

        // Проверка временных параметров эпика
        Epic epic = (Epic) manager.getEpicById(3); // Предполагая, что epic1 имеет ID=3
        System.out.println("\nВременные параметры эпика:");
        System.out.println("Начало: " + epic.getStartTime());
        System.out.println("Окончание: " + epic.getEndTime());
        System.out.println("Длительность: " + epic.getDuration().toMinutes() + " мин");
    }

    private static void testHistory(TaskManager manager) {
        System.out.println("\n=== Тестирование истории просмотров ===");

        // Получаем задачи для просмотра
        manager.getTaskById(1);
        manager.getEpicById(3);
        manager.getSubtaskById(4);

        System.out.println("История просмотров:");
        manager.getHistory().forEach(task -> {
            String type = task instanceof Epic ? "Epic" :
                    task instanceof Subtask ? "Subtask" : "Task";
            System.out.printf("[%s] %s (ID: %d)%n", type, task.getName(), task.getId());
        });

    }
}