package services.manager;

import models.task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node<Task>> historyReadStatistic = new HashMap<>();
    private Node<Task> first;
    private Node<Task> last;


    private void linkLast(Task task) {
        Node<Task> tempLast = last;
        Node<Task> newLast = new Node<>(task, null, tempLast);
        last = newLast;
        historyReadStatistic.put(task.getId(), newLast);

        if (tempLast == null) {
            first = newLast;
        } else {
            tempLast.next = newLast;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> currentNode = first;
        while (currentNode != null) {
            tasks.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    public void removeNode(Node<Task> task) {
        if (task != null) {

            Node<Task> next = task.next;
            Node<Task> prev = task.prev;
            task.data = null;

            if (first.equals(task) && last.equals(task)) {
                first = null;
                last = null;
            } else if (first.equals(task) && !(last.equals(task))) {
                first = next;
                first.prev = null;
            } else if (!(first.equals(task)) && last.equals(task)) {
                last = prev;
                last.next = null;
            } else {
                prev.next = next;
                next.prev = prev;
            }
        }
    }

    @Override
    public void add(Task task) {
        if (historyReadStatistic.containsKey(task.getId())) {
            remove(task.getId());
        }
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasks = getTasks();
        if (!tasks.isEmpty()) {
            Collections.reverse(tasks);
            return tasks;
        }
        return new ArrayList<>();
    }

    @Override
    public void remove(int id) {
        Node<Task> taskNode = historyReadStatistic.get(id);
        if (taskNode != null) {
            removeNode(taskNode);
        }
    }

    static class Node<T extends Task> {

        public T data;
        public Node<T> next;
        public Node<T> prev;

        public Node(T data, Node<T> next, Node<T> prev) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }


}
