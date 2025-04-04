package com.yandex.app.service;

import com.yandex.app.model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager{
    private final LinkedList<Task> history = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        history.addLast(task);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }

    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}