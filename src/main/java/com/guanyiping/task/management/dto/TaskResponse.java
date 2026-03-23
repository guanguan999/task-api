package com.guanyiping.task.management.dto;

public record TaskResponse(Long id, String title, String description, String priority, boolean completed) {}