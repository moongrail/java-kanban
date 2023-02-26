package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.task.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpRequest.newBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("HttpTaskServer default test.")
class HttpTaskServerTest {
    private static final URI TEST_URL = URI.create("http://localhost:8080/tasks");
    private static final String TEST_URL_SERVER = "http://localhost:8078";
    private HttpClient client;
    private HttpTaskServer server;
    private KVServer kvServer;

    private Gson gson = new Gson();

    @BeforeEach
    public void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        server = new HttpTaskServer(TEST_URL_SERVER);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @Test
    public void whenWrongMethodRequest() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(TEST_URL)
                .PUT(BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Данный метод пока не поддерживается: PUT", response.body());
    }

    @Test
    public void whenWrongGetRequest() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/magazine"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Запроса по адресу /tasks/magazine не существует.\n", response.body());
    }

    @Test
    public void whenWrongPostRequest() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/magazine"))
                .POST(BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Запроса по адресу /tasks/magazine не существует.\n", response.body());
    }

    @Test
    public void whenWrongDeleteRequest() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/magazine"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Запроса по адресу /tasks/magazine не существует.\n", response.body());
    }

    @Test
    public void whenPostTasksCorrect() throws IOException, InterruptedException, ExecutionException {
        Task newTask = new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2023, 7, 1, 0, 0));

        String json = gson.toJson(newTask);

        HttpRequest request = newBuilder()
                .uri(URI.create(TEST_URL + "/task"))
                .method("POST", BodyPublishers.ofString(json))
                .setHeader("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("Задача добавлена.", response.body());
    }

    @Test
    public void whenPostEpicCorrect() throws IOException, InterruptedException, ExecutionException {
        Epic newEpic = new Epic(9, TaskType.EPIC, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2021, 12, 1, 0, 0));

        String json = gson.toJson(newEpic);

        HttpRequest request = newBuilder()
                .uri(URI.create(TEST_URL + "/epic"))
                .method("POST", BodyPublishers.ofString(json))
                .setHeader("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("Задача добавлена.", response.body());
    }


    @Test
    public void whenPostSubTaskCorrect() throws IOException, InterruptedException, ExecutionException {
        SubTask newSubTask = new SubTask(10, TaskType.SUBTASK, "test", TaskStatus.DONE,
                "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2022, 3, 20, 0, 0), 9);

        String json = gson.toJson(newSubTask);

        HttpRequest request = newBuilder()
                .uri(URI.create(TEST_URL + "/subtask"))
                .method("POST", BodyPublishers.ofString(json))
                .setHeader("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("Задача добавлена.", response.body());
    }

    @Test
    public void whenPostSameTasksCorrect() throws IOException, InterruptedException {
        Task newTask = new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2023, 7, 1, 0, 0));

        String json = gson.toJson(newTask);

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response2 = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response2.statusCode());
        assertEquals("Задача обновлена.", response2.body());
    }

    @Test
    public void whenPostTasksRequestInCorrect() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertEquals("Ошибка добавления таска.", response.body());
    }

    @Test
    public void testGetTasksCorrect() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=1"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task task = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode());
        assertNotNull(task);
    }

    @Test
    public void testGetEpicCorrect() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/?id=9"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic task = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertNotNull(task);
    }

    @Test
    public void testGetSubTaskCorrect() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/?id=10"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask task = gson.fromJson(response.body(), SubTask.class);

        assertEquals(200, response.statusCode());
        assertNotNull(task);
    }

    @Test
    public void testGetTasksIncorrect() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=600"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertEquals("Введен неверный идентификатор задачи.", response.body());
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Type newType = new TypeToken<HashMap<Integer, Task>>() {
        }.getType();
        HashMap<Integer, Task> allMap = gson.fromJson(response.body(), newType);

        assertEquals(200, response.statusCode());
        assertEquals(3, allMap.size());
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Type newType = new TypeToken<HashMap<Integer, Task>>() {
        }.getType();
        HashMap<Integer, Task> allTask = gson.fromJson(response.body(), newType);

        assertEquals(200, response.statusCode());
        assertEquals(1, allTask.size());
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Type newType = new TypeToken<HashMap<Integer, Epic>>() {
        }.getType();
        HashMap<Integer, Epic> allEpic = gson.fromJson(response.body(), newType);

        assertEquals(200, response.statusCode());
        assertEquals(1, allEpic.size());
    }

    @Test
    public void testGetSubTasks() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Type newType = new TypeToken<HashMap<Integer, SubTask>>() {
        }.getType();
        HashMap<Integer, SubTask> allSubTasks = gson.fromJson(response.body(), newType);

        assertEquals(200, response.statusCode());
        assertEquals(1, allSubTasks.size());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest task = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=1"))
                .GET()
                .build();

        HttpResponse<String> responseTask = client.send(task, HttpResponse.BodyHandlers.ofString());

        Task task1 = gson.fromJson(responseTask.body(), Task.class);

        assertEquals(200, responseTask.statusCode());
        assertEquals(1, task1.getId());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest epic = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/?id=9"))
                .GET()
                .build();

        HttpResponse<String> responseEpic = client.send(epic, HttpResponse.BodyHandlers.ofString());

        Epic epic2 = gson.fromJson(responseEpic.body(), Epic.class);

        assertEquals(200, responseEpic.statusCode());
        assertEquals(9, epic2.getId());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest subtask = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/?id=10"))
                .GET()
                .build();

        HttpResponse<String> responseSubtask = client.send(subtask, HttpResponse.BodyHandlers.ofString());

        SubTask subtask2 = gson.fromJson(responseSubtask.body(), SubTask.class);

        assertEquals(200, responseSubtask.statusCode());
        assertEquals(10, subtask2.getId());
    }

    @Test
    public void testGetHistoryEmpty() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("История пуста", response.body());
    }

    @Test
    public void testGetHistoryCorrect() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/history"))
                .GET()
                .build();

        HttpRequest getHistory = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/?id=1"))
                .GET()
                .build();

        client.send(getHistory, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Type newType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> historyList = gson.fromJson(response.body(), newType);

        assertEquals(200, response.statusCode());
        assertEquals(1, historyList.size());
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/"))
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Type newType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> prioritizedList = gson.fromJson(response.body(), newType);

        assertEquals(200, response.statusCode());
        assertEquals(9, prioritizedList.get(0).getId());
        assertEquals(10, prioritizedList.get(1).getId());
        assertEquals(1, prioritizedList.get(2).getId());
    }

    @Test
    public void testDeleteAll() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteTasks() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteEpics() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteSubtasks() throws IOException, InterruptedException {
        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task?id=1"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/?id=9"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        sendDataToServer();

        HttpRequest request = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/?id=10"))
                .DELETE()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
    }


    private void sendDataToServer() throws IOException, InterruptedException {
        Task newTask = new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2023, 7, 1, 0, 0));

        String json = gson.toJson(newTask);

        HttpRequest post = newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(BodyPublishers.ofString(json))
                .build();

        Epic newEpic = new Epic(9, TaskType.EPIC, "test", TaskStatus.NEW, "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2021, 12, 1, 0, 0));

        String json2 = gson.toJson(newEpic);

        HttpRequest request = newBuilder()
                .uri(URI.create(TEST_URL + "/epic"))
                .method("POST", BodyPublishers.ofString(json2))
                .setHeader("content-type", "application/json")
                .build();

        SubTask newSubTask = new SubTask(10, TaskType.SUBTASK, "test", TaskStatus.DONE,
                "test",
                Duration.of(2, ChronoUnit.DAYS),
                LocalDateTime.of(2022, 3, 20, 0, 0), 9);

        String json3 = gson.toJson(newSubTask);

        HttpRequest request3 = newBuilder()
                .uri(URI.create(TEST_URL + "/subtask"))
                .method("POST", BodyPublishers.ofString(json3))
                .setHeader("content-type", "application/json")
                .build();

        client.send(request3, HttpResponse.BodyHandlers.ofString());
        client.send(request, HttpResponse.BodyHandlers.ofString());
        client.send(post, HttpResponse.BodyHandlers.ofString());
    }

    @AfterEach
    public void tearDown() throws IOException, InterruptedException {
        server.stop();
        kvServer.stop();
    }
}