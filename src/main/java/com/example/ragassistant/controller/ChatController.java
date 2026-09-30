package com.example.ragassistant.controller;

import com.example.ragassistant.model.ChatRequest;
import com.example.ragassistant.model.ChatResponse;
import com.example.ragassistant.service.RagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/rag")
    public ChatResponse answer(@Valid @RequestBody ChatRequest request) {
        return ragService.answer(request.question());
    }
}