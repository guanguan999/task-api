package com.guanyiping.task.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanyiping.task.management.dto.TaskRequest;
import com.guanyiping.task.management.dto.TaskResponse;
import com.guanyiping.task.management.exception.ResourceNotFoundException;
import com.guanyiping.task.management.security.JwtUtil;
import com.guanyiping.task.management.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskController 業務ロジックテスト
 * Security フィルターを無効化し、コントローラーの入力検証・レスポンス・例外処理に集中します。
 */
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false) // Security フィルターを無効化
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    // JwtFilter のコンテキスト生成に必要
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        taskResponse = new TaskResponse(1L, "Test Task", "Test description", "High", false);
    }

    // --- GET /tasks ---

    @Test
    void getAllTasks_returnsList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].priority").value("High"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void getAllTasks_returnsEmptyList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /tasks/{id} ---

    @Test
    void getTask_found_returns200() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getTask_notFound_returns404() throws Exception {
        when(taskService.getTaskById(99L))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 99"));

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    // --- GET /tasks/priority/{priority} ---

    @Test
    void getTasksByPriority_validPriority_returns200() throws Exception {
        when(taskService.getTasksByPriority("High")).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/tasks/priority/High"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("High"));
    }

    @Test
    void getTasksByPriority_invalidPriority_returns400() throws Exception {
        mockMvc.perform(get("/tasks/priority/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- POST /tasks ---

    @Test
    void createTask_success_returns201() throws Exception {
        TaskRequest request = buildRequest("New Task", "New description here", "Medium");
        TaskResponse created = new TaskResponse(2L, "New Task", "New description here", "Medium", false);
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(created);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.priority").value("Medium"));
    }

    @Test
    void createTask_missingTitle_returns400() throws Exception {
        TaskRequest request = buildRequest(null, "Some description text", "High");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTask_shortDescription_returns400() throws Exception {
        TaskRequest request = buildRequest("Title", "short", "High");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_invalidPriority_returns400() throws Exception {
        TaskRequest request = buildRequest("Title", "Some description text", "INVALID");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /tasks/{id} ---

    @Test
    void updateTask_success_returns200() throws Exception {
        TaskRequest request = buildRequest("Updated Title", "Updated description text", "Low");
        TaskResponse updated = new TaskResponse(1L, "Updated Title", "Updated description text", "Low", false);
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.priority").value("Low"));
    }

    @Test
    void updateTask_notFound_returns404() throws Exception {
        TaskRequest request = buildRequest("Title", "Some description text", "High");
        when(taskService.updateTask(eq(99L), any(TaskRequest.class)))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 99"));

        mockMvc.perform(put("/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- PATCH /tasks/{id}/complete ---

    @Test
    void completeTask_success_returns200AndCompleted() throws Exception {
        TaskResponse completed = new TaskResponse(1L, "Test Task", "Test description", "High", true);
        when(taskService.completeTask(1L)).thenReturn(completed);

        mockMvc.perform(patch("/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void completeTask_notFound_returns404() throws Exception {
        when(taskService.completeTask(99L))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 99"));

        mockMvc.perform(patch("/tasks/99/complete"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /tasks/{id} ---

    @Test
    void deleteTask_success_returns204() throws Exception {
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteTask_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found with id: 99"))
                .when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/tasks/99"))
                .andExpect(status().isNotFound());
    }

    private TaskRequest buildRequest(String title, String description, String priority) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPriority(priority);
        return request;
    }
}