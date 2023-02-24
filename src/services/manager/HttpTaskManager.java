package services.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import http.KVTaskClient;
import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import services.manager.history.InMemoryHistoryManager;
import util.FileDataCreatorUtil;

import java.util.HashMap;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private final String url;

    public HttpTaskManager(String url) {
        super(FileDataCreatorUtil.getOrCreateFileAndDir());
        this.url = url;
        this.client = new KVTaskClient(url);
    }

    @Override
    protected void save() {
        client.put(client.getApiToken(), gson.toJson(this));
    }

    public static HttpTaskManager load(String url, String key) {
        HttpTaskManager manager = new HttpTaskManager(url);
        String json = manager.client.load(key);

        ResponseTaskRepository responseTaskRepository = manager.gson.fromJson(json, ResponseTaskRepository.class);
        ResponseEpicRepository responseEpicRepository = manager.gson.fromJson(json, ResponseEpicRepository.class);
        ResponseSubTaskRepository responseSubTaskRepository = manager.gson
                .fromJson(json, ResponseSubTaskRepository.class);
        ResponseHistory responseHistory = manager.gson.fromJson(json, ResponseHistory.class);

        manager.taskRepository.putAll(responseTaskRepository.getTaskRepository());
        manager.epicRepository.putAll(responseEpicRepository.getEpicRepository());
        manager.subTaskRepository.putAll(responseSubTaskRepository.getSubTaskRepository());
        manager.historyManager = responseHistory.getHistoryManager();

        return manager;
    }

    static class ResponseTaskRepository {
        @SerializedName("taskRepository")
        @Expose
        private HashMap<Integer, Task> taskRepository;

        public HashMap<Integer, Task> getTaskRepository() {
            return taskRepository;
        }
    }

    static class ResponseEpicRepository {
        @SerializedName("epicRepository")
        @Expose
        private HashMap<Integer, Epic> epicRepository;

        public HashMap<Integer, Epic> getEpicRepository() {
            return epicRepository;
        }
    }

    static class ResponseSubTaskRepository {
        @SerializedName("subTaskRepository")
        @Expose
        private HashMap<Integer, SubTask> subTaskRepository;

        public HashMap<Integer, SubTask> getSubTaskRepository() {
            return subTaskRepository;
        }
    }

    static class ResponseHistory {
        @SerializedName("historyManager")
        @Expose
        private InMemoryHistoryManager historyManager;

        public InMemoryHistoryManager getHistoryManager() {
            return historyManager;
        }
    }
}
