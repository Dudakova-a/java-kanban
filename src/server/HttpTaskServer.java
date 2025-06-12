package server;

import com.sun.net.httpserver.HttpServer;
import handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080; // Порт сервера
    private final HttpServer server; // Встроенный HTTP-сервер
    private final TaskManager taskManager; // Менеджер задач

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault()); // Используем дефолтный менеджер
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager; // Инициализируем переданным менеджером
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0); // Создаём сервер

        // Регистрируем обработчики для каждого типа запросов
        server.createContext("/tasks", new TasksHandler(taskManager)); // Обычные задачи
        server.createContext("/subtasks", new SubtasksHandler(taskManager)); // Подзадачи
        server.createContext("/epics", new EpicsHandler(taskManager)); // Эпики
        server.createContext("/history", new HistoryHandler(taskManager)); // История
        server.createContext("/prioritized", new PrioritizedHandler(taskManager)); // Приоритетные задачи
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }
}
