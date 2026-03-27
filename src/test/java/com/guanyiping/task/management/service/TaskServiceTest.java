package com.guanyiping.task.management.service;

import com.guanyiping.task.management.dto.TaskRequest;
import com.guanyiping.task.management.dto.TaskResponse;
import com.guanyiping.task.management.entity.Task;
import com.guanyiping.task.management.exception.ResourceNotFoundException;
import com.guanyiping.task.management.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test description");
        task.setPriority("High");
        task.setCompleted(false);
    }

    // --- getAllTasks ---

    @Test
    void getAllTasks_returnsEmptyList_whenNoTasks() {
        when(taskRepository.findAll()).thenReturn(List.of());

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTasks_returnsMappedResponses() {
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).title()).isEqualTo("Test Task");
        assertThat(result.get(0).priority()).isEqualTo("High");
        assertThat(result.get(0).completed()).isFalse();
    }

    // --- getTasksByPriority ---

    @Test
    void getTasksByPriority_returnsMatchingTasks() {
        when(taskRepository.findByPriority("High")).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getTasksByPriority("High");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).priority()).isEqualTo("High");
    }

    @Test
    void getTasksByPriority_returnsEmptyList_whenNoneMatch() {
        when(taskRepository.findByPriority("Low")).thenReturn(List.of());

        List<TaskResponse> result = taskService.getTasksByPriority("Low");

        assertThat(result).isEmpty();
    }

    // --- getTaskById ---

    @Test
    void getTaskById_returnsTask_whenFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getTaskById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Task");
    }

    @Test
    void getTaskById_throwsResourceNotFoundException_whenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- createTask ---

    @Test
    void createTask_savesAndReturnsMappedResponse() {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("New description");
        request.setPriority("Medium");

        Task saved = new Task();
        saved.setId(2L);
        saved.setTitle("New Task");
        saved.setDescription("New description");
        saved.setPriority("Medium");
        saved.setCompleted(false);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse result = taskService.createTask(request);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.title()).isEqualTo("New Task");
        assertThat(result.priority()).isEqualTo("Medium");
        assertThat(result.completed()).isFalse();
        verify(taskRepository).save(any(Task.class));
    }

    // --- updateTask ---

    @Test
    void updateTask_updatesOnlyNonNullFields() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");
        // description と priority は null のまま

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.updateTask(1L, request);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.description()).isEqualTo("Test description"); // 変更されない
        assertThat(result.priority()).isEqualTo("High");               // 変更されない
    }

    @Test
    void updateTask_updatesAllFields_whenAllProvided() {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Title");
        request.setDescription("New description text");
        request.setPriority("Low");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.updateTask(1L, request);

        assertThat(result.title()).isEqualTo("New Title");
        assertThat(result.description()).isEqualTo("New description text");
        assertThat(result.priority()).isEqualTo("Low");
    }

    @Test
    void updateTask_throwsResourceNotFoundException_whenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, new TaskRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- completeTask ---

    @Test
    void completeTask_setsCompletedToTrue() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.completeTask(1L);

        assertThat(result.completed()).isTrue();
        verify(taskRepository).save(task);
    }

    @Test
    void completeTask_throwsResourceNotFoundException_whenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.completeTask(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- deleteTask ---

    @Test
    void deleteTask_callsDeleteById() {
        taskService.deleteTask(1L);
        verify(taskRepository).deleteById(1L);
    }
}