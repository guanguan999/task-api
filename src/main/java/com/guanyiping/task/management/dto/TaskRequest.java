package com.guanyiping.task.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @Size(min=10, message = "Description must be at least 10 characters")
    private String description;
    @NotNull(message = "Priority is required")
    @Pattern(regexp = "Low|Medium|High", message = "Priority must be Low, Medium, or High")
    private String priority;
}
