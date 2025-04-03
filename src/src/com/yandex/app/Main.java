package com.yandex.app;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Status;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.service.TaskManager;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создание задач
        Task task1 = new Task( "com.yandex.app.model.Task 1", "Description 1", Status.NEW);
        Task task2 = new Task( "com.yandex.app.model.Task 2", "Description 2", Status.NEW);

        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпика с подзадачами
        Epic epic1 = new Epic(0, "com.yandex.app.model.Epic 1", "Description com.yandex.app.model.Epic 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "com.yandex.app.model.Subtask 1", "Description com.yandex.app.model.Subtask 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "com.yandex.app.model.Subtask 2", "Description com.yandex.app.model.Subtask 2", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Вывод всех задач
        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());
        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());

        // Изменение статусов
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        // Вывод обновлённых данных
        System.out.println("После изменения статуса подзадачи 1:");
        System.out.println(manager.getAllSubtasks());
        System.out.println("Статус эпика 1:");
        System.out.println(manager.getEpicById(epic1.getId()).getStatus());

        // Удаление задачи и эпика
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());

        // Вывод после удаления
        System.out.println("После удаления задачи 1 и эпика 1:");
        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());
        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());


    }
    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getSubtasksByEpicId(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

}
