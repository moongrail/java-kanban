package test.services.manager;

import exceptions.ManagerSaveException;
import models.task.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.manager.FileBackedTasksManager;
import services.manager.TaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileBackedTasksManager default test.")
class FileBackedTasksManagerTest<T extends TaskManager> extends TaskManagerTest<T> {
    private static final Path RESOURCES = Path.of("resources/test", "history-test.csv");

    private static final File FILE = getOrCreateFileAndDir(RESOURCES);

    @BeforeEach
    public void beforeEach() {
        taskManager = (T) new FileBackedTasksManager(FILE);
    }


    @Test
    public void checkSaveMethodIfFileIsEmpty() {
        assertTrue(taskManager.getAllMap().isEmpty());
    }

    @Test
    public void throwManagersSaveExceptionWhenCreatedFileManager() {
        ManagerSaveException exception = assertThrows(ManagerSaveException.class,
                () -> new FileBackedTasksManager(new File("FILE")));
        assertEquals("Ошибка чтения файла: FILE", exception.getMessage());
    }

    @Test
    public void checkWorkFromStringFileLine() {
        Epic testEpic = new Epic(3, TaskType.EPIC, "test", TaskStatus.NEW, "test");
        Task testTask = new Task(4, TaskType.TASK, "test", TaskStatus.NEW, "test");
        SubTask testSubtask = new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.NEW, "test", 3);

        Epic epic = (Epic) FileBackedTasksManager.fromString("3,EPIC,test,NEW,test,");
        Task task = FileBackedTasksManager.fromString("4,TASK,test,NEW,test,");
        SubTask subTask = (SubTask) FileBackedTasksManager.fromString("5,SUBTASK,test,NEW,test,3");

        assertEquals(testEpic, epic);
        assertEquals(testSubtask, subTask);
        assertEquals(testTask, task);
    }

    @Test
    public void checkWorkToStringFileLine() {
        String s = FileBackedTasksManager.toString(new Epic(3, TaskType.EPIC, "test", TaskStatus.NEW,
                "test"));
        assertEquals("3,EPIC,test,NEW,test,", s);
    }

    @Test
    public void checkSaveMethodWriteCorrectly() {
        try {
            super.addDataInRepositoriesSize10();
            //Я не совсем понимаю что именно то хотят от меня? Что мне проверить. В общем, тестами я покрыл больше 80%.
            assertEquals(13, Files.readAllLines(FILE.toPath()).size());
            assertEquals(10, taskManager.getAllMap().size());
            assertTrue(taskManager.getHistory().isEmpty());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла в проверке файла на то что он не пустой: " + e);
        }
    }

    @Test
    public void checkSaveMethodIfInFileOnlyEpic() {
        try {
            taskManager.addEpic(new Epic(3, TaskType.EPIC, "test", TaskStatus.NEW, "test"));
            taskManager.addEpic(new Epic(4, TaskType.EPIC, "test", TaskStatus.NEW, "test"));

            assertEquals(5, Files.readAllLines(FILE.toPath()).size());
            assertEquals(2, taskManager.getAllEpicMap().size());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла в проверке файла на то что он не пустой: " + e);
        }
    }

    @Test
    public void checkLoadFromFileWork() {
        super.addDataInRepositoriesSize10();
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);

        FileBackedTasksManager recoverFileManager = FileBackedTasksManager.loadFromFile(FILE);
        assertEquals(10, recoverFileManager.getAllMap().size());
        assertEquals(3, recoverFileManager.getAllEpicMap().size());
        assertEquals(5, recoverFileManager.getAllSubTaskMap().size());
        assertEquals(2, recoverFileManager.getAllTaskMap().size());
        assertEquals(2, recoverFileManager.getHistory().size());
    }

    @Test
    public void throwsInLoadFromFileManagerSaveException() {

        ManagerSaveException exception = assertThrows(ManagerSaveException.class,
                () -> FileBackedTasksManager.loadFromFile(new File("FILE")));

        assertEquals("Ошибка чтения файла: FILE", exception.getMessage());
    }

    private static File getOrCreateFileAndDir(Path resources) {
        File dir = Path.of("resources/test").toFile();
        if (!dir.exists()) {
            dir.mkdir();
        }
        Path path = null;
        File file = null;

        if (!resources.toFile().exists()) {
            try {
                path = Files.createFile(resources);
            } catch (IOException e) {
                throw new ManagerSaveException(e.getMessage());
            }
            file = path.toFile();
        } else {
            file = resources.toFile();
        }
        return file;
    }
}