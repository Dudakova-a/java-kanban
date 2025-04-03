package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    // Методы для задач (Task)
    ArrayList<Task> getAllTasks();
    void deleteAllTasks();
    Task getTaskById(int id);
    void createTask(Task task);
    void updateTask(Task task);
    void deleteTaskById(int id);

    // Методы для подзадач (Subtask)
    ArrayList<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtaskById(int id);
    void createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtaskById(int id);

    // Методы для эпиков (Epic)
    ArrayList<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpicById(int id);
    void createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpicById(int id);

    // Дополнительные методы
    List<Subtask> getSubtasksByEpicId(int epicId);
    List<Task> getHistory();
}
