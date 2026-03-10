package com.guanyiping.task.management.repository;

import com.guanyiping.task.management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}