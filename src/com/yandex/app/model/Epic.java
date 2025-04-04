package com.yandex.app.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds; // Список ids для подзадач входящих в эпик

    // Конструктор для создания эпика
    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW); // Эпик всегда создается со статусом NEW
        this.subtaskIds = new ArrayList<>();// Инициализируем список подзадач
    }
    public Epic(String name, String description) {
        super(name, description, Status.NEW); // Эпик всегда создается со статусом NEW
        this.subtaskIds = new ArrayList<>();// Инициализируем список подзадач
    }
    // Геттер для списка идентификаторов подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Метод для добавления индентификатора подзадачи в эпик
    public void addSubtaskId(int subtaskID) {
        subtaskIds.add(subtaskID);
    }

    // Метод для удаления индентификатора подзадачи из эпика
    public void removeSubtaskId(int subtaskID) {
        subtaskIds.remove((Integer) subtaskID); // Удаляем по значению
    }
}