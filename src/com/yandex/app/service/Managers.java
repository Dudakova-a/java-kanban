package com.yandex.app.service;

public class Managers {
    private Managers() {} // Приватный конструктор

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}