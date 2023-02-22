package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import models.task.*;
import services.manager.TaskManager;
import services.util.Managers;
import util.FileDataCreatorUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class HttpTaskServer {

    private static final int DEFAULT_PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final TaskManager backedTasksManager = Managers
            .getDefaultFileBackedTasksManager(FileDataCreatorUtil.getOrCreateFileAndDir());
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 10);
            httpServer.createContext("/tasks", new TaskServiceHandler());
            httpServer.start();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка на сервере: " + e.getMessage());
        }
    }

    static class TaskServiceHandler implements HttpHandler {
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
                    throw new IllegalArgumentException("Данный метод пока не поддерживается: " + method);
            }
        }

        private void handlePostRequest(HttpExchange exchange, String requestURI) {
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
                    throw new IllegalArgumentException(String.format("Запроса по адресу %s не существует.\n"
                            , requestURI));
            }
        }

        private void postHandleSubtask(HttpExchange exchange) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                SubTask task = gson.fromJson(body, SubTask.class);

                if (task == null) {
                    writeResponse(exchange,"Ошибка добавления таска.",400);
                    return;
                }else if (backedTasksManager.getAllSubTaskMap().containsKey(task.getId())) {
                    backedTasksManager.updateSubTask(task);
                    writeResponse(exchange,"Задача обновлена.",201);
                    return;
                }

                backedTasksManager.addSubTask(task.getIdEpic(), task);
                writeResponse(exchange,"Задача добавлена.",201);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void postHandleEpic(HttpExchange exchange) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Epic task = gson.fromJson(body, Epic.class);

                if (task == null) {
                    writeResponse(exchange,"Ошибка добавления таска.",400);
                    return;
                }else if (backedTasksManager.getAllEpicMap().containsKey(task.getId())) {
                    backedTasksManager.updateEpic(task);
                    writeResponse(exchange,"Задача обновлена.",201);
                    return;
                }

                backedTasksManager.addEpic(task);
                writeResponse(exchange,"Задача добавлена.",201);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void postHandleTask(HttpExchange exchange) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Task task = gson.fromJson(body, Task.class);

                if (task == null) {
                    writeResponse(exchange,"Ошибка добавления таска.",400);
                    return;
                }else if (backedTasksManager.getAllTaskMap().containsKey(task.getId())) {
                    backedTasksManager.updateTask(task);
                    writeResponse(exchange,"Задача обновлена.",201);
                    return;
                }

                backedTasksManager.addTask(task);
                writeResponse(exchange,"Задача добавлена.",201);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void handleDeleteRequest(HttpExchange exchange, String requestURI) {
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
                    throw new IllegalArgumentException(String.format("Запроса по адресу %s не существует.\n"
                            , requestURI));
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
                    throw new IllegalArgumentException(String.format("Запроса по адресу %s не существует.\n"
                            , requestURI));
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

                if (!backedTasksManager.getAllSubTaskMap().containsKey(id)) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                backedTasksManager.removeSubTask(id);
                writeResponse(exchange, "true", 204);
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

                if (!backedTasksManager.getAllEpicMap().containsKey(id)) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                backedTasksManager.removeEpicById(id);
                writeResponse(exchange, "true", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleSubTasks(HttpExchange exchange) {
            try {
                backedTasksManager.removeSubTaskMap();
                writeResponse(exchange, "true", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleEpic(HttpExchange exchange) {
            try {
                backedTasksManager.removeEpicMap();
                writeResponse(exchange, "true", 204);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleTask(HttpExchange exchange) {
            try {
                backedTasksManager.removeTaskMap();
                writeResponse(exchange, "true", 204);
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

                if (!backedTasksManager.getAllTaskMap().containsKey(id)) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }
                backedTasksManager.removeTaskById(id);
                writeResponse(exchange, "true", 204);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void deleteHandleAll(HttpExchange exchange) {
            try {
                backedTasksManager.removeAll();
                writeResponse(exchange, "true", 204);
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
                Epic epicById = backedTasksManager.getEpicById(idTask);

                if (epicById == null) {
                    writeResponse(exchange, "Введен неверный идентификатор задачи.", 400);
                    return;
                }

                List<SubTask> subTasksByEpic = backedTasksManager.getSubTasksByEpic(epicById);
                String response = gson.toJson(subTasksByEpic);
                writeResponse(exchange, response, 200);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void getHandlePrioritizedTasks(HttpExchange exchange) {
            List<Task> history = backedTasksManager.getPrioritizedTasks();
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
            List<Task> history = backedTasksManager.getHistory();
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
                SubTask taskById = backedTasksManager.getSubTaskById(idTask);
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
                Epic taskById = backedTasksManager.getEpicById(idTask);

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
                Task taskById = backedTasksManager.getTaskById(idTask);

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
            HashMap<Integer, SubTask> subTaskMap = backedTasksManager.getAllSubTaskMap();
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
            HashMap<Integer, Epic> epicMap = backedTasksManager.getAllEpicMap();
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
            HashMap<Integer, Task> taskMap = backedTasksManager.getAllTaskMap();
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
            HashMap<Integer, Task> allMap = backedTasksManager.getAllMap();
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


    static {
        Duration duration = Duration.of(1, ChronoUnit.DAYS);
        LocalDateTime localDateTime = LocalDateTime.of(2021, 1, 1, 0, 0);

        Epic firstEpic = new Epic(1, TaskType.EPIC, "Купить продукты", TaskStatus.NEW
                , "Сходить в магазин за продуктами",
                duration,
                localDateTime);
        Epic secondEpic = new Epic(2, TaskType.EPIC, "Спорт", TaskStatus.NEW,
                "Побегать в парке.",
                duration,
                localDateTime.plusDays(1));

        SubTask firstSubTask = new SubTask(3, TaskType.SUBTASK, "Купить помидоры"
                , TaskStatus.NEW, "Найти свежие помидоры в магазине.",
                duration.plusDays(1),
                localDateTime.plusDays(4),
                firstEpic.getId());

        SubTask secondSubTask = new SubTask(4, TaskType.SUBTASK, "Купить пиццу", TaskStatus.NEW,
                "Заказать пиццу в кафе",
                duration.plusDays(2),
                localDateTime.plusDays(6),
                firstEpic.getId());

        SubTask thirdSubTask = new SubTask(5, TaskType.SUBTASK, "Обман", TaskStatus.DONE,
                "Какой из тебя спортсмен.",
                duration.plusDays(1),
                localDateTime.plusDays(10),
                firstEpic.getId());

        Task simpleTask = new Task(99, TaskType.TASK, "Просто задание", TaskStatus.DONE,
                "Просто задание и никак не связано с эпиком или подзадачей эпика.",
                duration.plusDays(1),
                localDateTime.plusYears(200));

        Task simpleTaskWithoutTime = new Task(77, TaskType.TASK, "Просто задание", TaskStatus.DONE,
                "Просто задание и никак не связано с эпиком или подзадачей эпика.");

        backedTasksManager.addEpic(firstEpic);
        backedTasksManager.addEpic(secondEpic);
        backedTasksManager.addSubTask(firstEpic.getId(), firstSubTask);
        backedTasksManager.addSubTask(firstEpic.getId(), secondSubTask);
        backedTasksManager.addSubTask(firstEpic.getId(), thirdSubTask);
        backedTasksManager.addTask(simpleTask);
        backedTasksManager.addTask(simpleTaskWithoutTime);
    }
}
