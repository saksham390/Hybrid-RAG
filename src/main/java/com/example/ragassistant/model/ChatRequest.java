package com.example.ragassistant.model;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank(message = "question is required") String question) {
}