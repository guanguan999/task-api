package com.guanyiping.task.management.service;

import com.guanyiping.task.management.dto.TaskRequest;
import com.guanyiping.task.management.dto.TaskResponse;
import com.guanyiping.task.management.entity.Task;
import com.guanyiping.task.management.exception.ResourceNotFoundException;
import com.guanyiping.task.management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<TaskResponse> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority).stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(Long id) {
        return toResponse(taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id)));
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest updated) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        if (updated.getTitle() != null) {
            task.setTitle(updated.getTitle());
        }
        if (updated.getPriority() != null) {
            task.setPriority(updated.getPriority());
        }
        if (updated.getDescription() != null) {
            task.setDescription(updated.getDescription());
        }
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse completeTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        task.setCompleted(true);
        return toResponse(taskRepository.save(task));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getPriority(), task.isCompleted());
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}