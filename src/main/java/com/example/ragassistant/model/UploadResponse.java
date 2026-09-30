package com.example.ragassistant.model;

public record UploadResponse(long documentId, String filename, int pages, int chunks) {
}