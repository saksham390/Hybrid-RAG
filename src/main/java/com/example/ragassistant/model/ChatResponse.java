package com.example.ragassistant.model;

import java.util.List;

public record ChatResponse(String answer, List<Citation> citations) {
}