package com.guanyiping.task.management.service;

import com.guanyiping.task.management.entity.Task;
import com.guanyiping.task.management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task updated) {
        Task task = getTaskById(id);
        if (updated.getTitle() != null) {
            task.setTitle(updated.getTitle());
        }
        if (updated.getPriority() != null) {
            task.setPriority(updated.getPriority());
        }
        if (updated.getDescription() != null) {
            task.setDescription(updated.getDescription());
        }
        task.setCompleted(updated.isCompleted());
        return taskRepository.save(task);
    }

    public Task completeTask(Long id) {
        Task task = getTaskById(id);
        task.setCompleted(true);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}