package com.example.ragassistant.model;

import java.time.Instant;

public record DocumentResponse(long documentId, String filename, Instant uploadedAt) {
}