package com.guanyiping.task.management.repository;

import com.guanyiping.task.management.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"owner", "category"})
    List<Task> findAll();

    @EntityGraph(attributePaths = {"owner", "category"})
    List<Task> findByPriority(String priority);
}