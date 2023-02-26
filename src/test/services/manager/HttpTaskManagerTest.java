package test.services.manager;

import com.google.gson.Gson;
import http.HttpTaskServer;
import http.KVServer;
import http.KVTaskClient;
import models.task.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.manager.HttpTaskManager;
import services.manager.TaskManager;
import services.util.Managers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("HttpTaskManager default test.")
class HttpTaskManagerTest<T extends TaskManager> extends TaskManagerTest<T> {
    private static final String URL = "http://localhost:8078";
    private HttpTaskServer httpTaskServer;
    private KVServer kvServer;

    @BeforeEach
    public void beforeEach() throws IOException {
        taskManager = (T) Managers.getDefaultHttpTasksManager(URL);
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer(URL);
        httpTaskServer.start();
    }

    @Test
    public void whenHttpTaskManagerWorksSaveAndLoad() {
        Gson gson = new Gson();
        Task newTask = new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2023, 7, 1, 0, 0));
        Epic newEpic = new Epic(9, TaskType.EPIC, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2021, 12, 1, 0, 0));
        SubTask newSubTask = new SubTask(10, TaskType.SUBTASK, "test", TaskStatus.DONE,
                "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2022, 3, 20, 0, 0), 9);

        taskManager.addEpic(newEpic);
        taskManager.addSubTask(9,newSubTask);
        taskManager.addTask(newTask);

        KVTaskClient client = new KVTaskClient(URL);

        client.put(client.getApiToken(), gson.toJson(taskManager));

        HttpTaskManager load = HttpTaskManager.load(URL, client.getApiToken());

        assertEquals(1, load.getTaskRepository().size());
        assertEquals(1, load.getEpicRepository().size());
        assertEquals(1, load.getSubTaskRepository().size());
        assertEquals(0, load.getHistory().size());
    }

    @Test
    public void throwLoadException() {
        final RuntimeException ex = assertThrows(RuntimeException.class, () ->
                HttpTaskManager.load(URL, "1"));

        assertEquals("Ошибка обработки ключа: null", ex.getMessage());
    }

    @AfterEach
    public void tearDown() {
        kvServer.stop();
        httpTaskServer.stop();
    }
}