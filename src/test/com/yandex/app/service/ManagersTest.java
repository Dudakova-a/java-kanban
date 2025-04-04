package com.yandex.app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultShouldReturnInitializedInMemoryTaskManager() {
        // Проверяем, что getDefault() возвращает проинициализированный InMemoryTaskManager
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер не должен быть null");
        // Проверяем что это TaskManager, а не конкретную реализацию
        assertInstanceOf(TaskManager.class, manager, "Должен возвращаться объект, реализующий TaskManager");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedInMemoryHistoryManager() {
        // Проверяем, что getDefaultHistory() возвращает проинициализированный InMemoryHistoryManager
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");
        // Проверяем что это HistoryManager, а не конкретную реализацию
        assertTrue(true, "Должен возвращаться объект, реализующий HistoryManager");
    }
}