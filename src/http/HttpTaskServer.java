package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import services.manager.HttpTaskManager;
import services.util.Managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class HttpTaskServer {

    private static final int DEFAULT_PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpTaskManager httpTaskManager;
    private final HttpServer httpServer;
    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public HttpTaskServer(String url) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
            httpServer.createContext("/tasks", new TaskServiceHandler());
            httpTaskManager = (HttpTaskManager) Managers.getDefaultHttpTasksManager(url);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка на сервере: " + e.getMessage());
        }
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    protected class TaskServiceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String requestURI = exchange.getRequestURI().getPath().toLowerCase();

            switch (method) {
                case "GET":
                    handleGetRequest(exchange, requestURI);
                    break;
                case "POST":
                    handlePostRequest(exchange, requestURI);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, requestURI);
                    break;
                default:
                    writeResponse(exchange, "Данный метод пока не поддерживается: " + method,
                            400);
            }
        }

        private void handlePostRequest(HttpExchange exchange, String requestURI) throws IOException {
            switch (requestURI) {
                case "/tasks/task":
                    postHandleTask(exchange);
                    break;
                case "/tasks/epic":
                    postHandleEpic(exchange);
                    break;
                case "/tasks/subtask":
                    postHandleSubtask(exchange);
                    break;
                default:
                    writeResponse(exchange, String.format("Запроса по адресу %s не существует.\n"
                            , requestURI), 404);
            }
        }

        private void handleDeleteRequest(HttpExchange exchange, String requestURI) throws IOException {
            switch (requestURI) {
                case "/tasks":
                    deleteHandleAll(exchange);
                    return;
                case "/tasks/task":
                    deleteHandleTask(exchange);
                    break;
                case "/tasks/epic":
                    deleteHandleEpic(exchange);
                    break;
                case "/tasks/subtask":
                    deleteHandleSubTasks(exchange);
                    break;
                case "/tasks/task/":
                    deleteHandleTaskById(exchange);
                    break;
                case "/tasks/epic/":
                    deleteHandleEpicById(exchange);
                    break;
                case "/tasks/subtask/":
                    deleteHandleSubTasksById(exchange);
                    break;
                default:
                    writeResponse(exchange, String.format("Запроса по адресу %s не существует.\n"
                            , requestURI), 404);
            }
        }

        private void handleGetRequest(HttpExchange exchange, String requestURI) throws IOException {
            switch (requestURI) {
                case "/tasks":
                    getHandleAllMap(exchange);
                    break;
                case "/tasks/task":
                    getHandleTasks(exchange);
                    break;
                case "/tasks/task/":
                    getHandleTaskById(exchange);
                    break;
                case "/tasks/epic":
                    getHandleEpicTasks(exchange);
                    break;
                case "/tasks/epic/":
                    getHandleEpicById(exchange);
                    break;
                case "/tasks/subtask":
                    getHandleSubTasks(exchange);
                    break;
                case "/tasks/subtask/":
                    getHandleSubTasksById(exchange);
                    break;
                case "/tasks/history":
                    getHandleHistory(exchange);
                    break;
                case "/tasks/":
                    getHandlePrioritizedTasks(exchange);
                    break;
                case "/tasks/subtask/epic/":
                    getHandleSubtaskByEpic(exchange);
                    break;
                default:
                    writeResponse(exchange, String.format("Запроса по адресу %s не существует.\n"
                            , requestURI), 404);
            }
        }

        private void postHandleSubtask(HttpExchange exchange) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                SubTask task = gson.fromJson(body, SubTask.class);

                if (task == null) {
                    writeResponse(exchange, "Ошибка добавления таска.", 400);
                    return;
                } else if (httpTaskManager.getAllSubTaskMap().containsKey(task.getId())) {
                    httpTaskManager.updateSubTask(task);
                    writeResponse(exchange, "Задача обновлена.", 201);
                    return;
                }

                httpTaskManager.addSubTask(task.getIdEpic(), task);
                writeResponse(exchange, "Задача добавлена.", 201);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void postHandleEpic(HttpExchange exchange) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Epic task = gson.fromJson(body, Epic.class);

                if (task == null) {
                    writeResponse(exchange, "Ошибка добавления таска.", 400);
                    return;
                } else if (httpTaskManager.getAllEpicMap().containsKey(task.getId())) {
                    httpTaskManager.updateEpic(task);
                    writeResponse(exchange, "Задача обновлена.", 201);
                    return;
                }

                httpTaskManager.addEpic(task);
                writeResponse(exchange, "Задача добавлена.", 201);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void postHandleTask(HttpExchange exchange) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Task task = gson.fromJson(body, Task.class);

                if (task == null) {
                    writeResponse(exchange, "Ошибка добавления таска.", 400);
                    return;
                } else if (httpTaskManager.getTaskRepository().containsKey(task.getId())) {
                    httpTaskManager.updateTask(task);
                    writeResponse(exchange, "Задача обновлена.", 201);
                    return;
                }

                httpTaskManager.addTask(task);
                writeResponse(exchange, "Задача добавлена.", 201);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        private void deleteHandleSubTasksById(HttpExchange exchange) {
            Optional<Integer> idFromParam = getIdFromParam(exchange);

            try {
                if (!idFromParam.isPresent()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer id = idFromParam.get();

                if (!httpTaskManager.getAllSubTaskMap().containsKey(id)) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                httpTaskManager.removeSubTask(id);
                writeResponse(exchange, "", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleEpicById(HttpExchange exchange) {
            Optional<Integer> idFromParam = getIdFromParam(exchange);

            try {
                if (!idFromParam.isPresent()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer id = idFromParam.get();

                if (!httpTaskManager.getAllEpicMap().containsKey(id)) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                httpTaskManager.removeEpicById(id);
                writeResponse(exchange, "", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleSubTasks(HttpExchange exchange) {
            try {
                httpTaskManager.removeSubTaskMap();
                writeResponse(exchange, "", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleEpic(HttpExchange exchange) {
            try {
                httpTaskManager.removeEpicMap();
                writeResponse(exchange, "", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleTask(HttpExchange exchange) {
            try {
                httpTaskManager.removeTaskMap();
                writeResponse(exchange, "", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleTaskById(HttpExchange exchange) {
            Optional<Integer> idFromParam = getIdFromParam(exchange);

            try {
                if (!idFromParam.isPresent()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer id = idFromParam.get();

                if (!httpTaskManager.getAllTaskMap().containsKey(id)) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }
                httpTaskManager.removeTaskById(id);
                writeResponse(exchange, "", 204);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleAll(HttpExchange exchange) {
            try {
                httpTaskManager.removeAll();
                writeResponse(exchange, "", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void getHandleSubtaskByEpic(HttpExchange exchange) {
            Optional<Integer> idFromParam = getIdFromParam(exchange);

            try {
                if (!idFromParam.isPresent()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer idTask = idFromParam.get();
                Epic epicById = httpTaskManager.getEpicById(idTask);

                if (epicById == null) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                List<SubTask> subTasksByEpic = httpTaskManager.getSubTasksByEpic(epicById);
                String response = gson.toJson(subTasksByEpic);
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void getHandlePrioritizedTasks(HttpExchange exchange) {
            List<Task> history = httpTaskManager.getPrioritizedTasks();
            writeResponseForListTasks(exchange, history);
        }

        private void writeResponseForListTasks(HttpExchange exchange, List<Task> history) {
            try {
                if (history.isEmpty()) {
                    writeResponse(exchange, "История пуста", 200);
                    return;
                }
                String response = gson.toJson(history);
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void getHandleHistory(HttpExchange exchange) {
            List<Task> history = httpTaskManager.getHistory();
            writeResponseForListTasks(exchange, history);
        }

        private void getHandleSubTasksById(HttpExchange exchange) {
            Optional<Integer> taskId = getIdFromParam(exchange);
            try {
                if (taskId.isEmpty()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer idTask = taskId.get();
                SubTask taskById = httpTaskManager.getSubTaskById(idTask);
                if (taskById == null) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                String response = gson.toJson(taskById);
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Optional<Integer> getIdFromParam(HttpExchange exchange) {
            String param = exchange.getRequestURI().getQuery();
            Optional<Integer> taskId = getTaskId(param);
            return taskId;
        }

        private void getHandleEpicById(HttpExchange exchange) {
            Optional<Integer> taskId = getIdFromParam(exchange);

            try {
                if (taskId.isEmpty()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer idTask = taskId.get();
                Epic taskById = httpTaskManager.getEpicById(idTask);

                if (taskById == null) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                String response = gson.toJson(taskById);
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void getHandleTaskById(HttpExchange exchange) {
            Optional<Integer> taskId = getIdFromParam(exchange);

            try {
                if (taskId.isEmpty()) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                Integer idTask = taskId.get();
                Task taskById = httpTaskManager.getTaskById(idTask);

                if (taskById == null) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                String response = gson.toJson(taskById);
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void getHandleSubTasks(HttpExchange exchange) {
            HashMap<Integer, SubTask> subTaskMap = httpTaskManager.getAllSubTaskMap();
            String response = gson.toJson(subTaskMap);

            try {
                if (response.isBlank()) {
                    writeResponse(exchange, "Список сабтасков пуст", 200);
                    return;
                }
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка получения всех сабтасков: " + e.getMessage());
            }
        }

        private void getHandleEpicTasks(HttpExchange exchange) {
            HashMap<Integer, Epic> epicMap = httpTaskManager.getAllEpicMap();
            String response = gson.toJson(epicMap);

            try {
                if (response.isBlank()) {
                    writeResponse(exchange, "Список эпиков пуст", 200);
                    return;
                }
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка получения всех эпиков: " + e.getMessage());
            }
        }

        private void getHandleTasks(HttpExchange exchange) {
            HashMap<Integer, Task> taskMap = httpTaskManager.getAllTaskMap();
            String response = gson.toJson(taskMap);

            try {
                if (response.isBlank()) {
                    writeResponse(exchange, "Список тасков пуст", 200);
                }
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка получения всех тасков: " + e.getMessage());
            }
        }

        private void getHandleAllMap(HttpExchange exchange) {
            HashMap<Integer, Task> allMap = httpTaskManager.getAllMap();
            String response = gson.toJson(allMap);

            try {
                if (response.isBlank()) {
                    writeResponse(exchange, "Список задач пуст", 200);
                }
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка получения всех задач: " + e.getMessage());
            }
        }

        private Optional<Integer> getTaskId(String param) {
            String[] paramParts = param.split("=");
            try {
                return Optional.of(Integer.parseInt(paramParts[1]));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }

        private void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {

            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(responseCode, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            exchange.close();
        }
    }
}
