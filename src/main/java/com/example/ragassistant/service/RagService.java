package com.example.ragassistant.service;

import com.example.ragassistant.model.ChatResponse;
import com.example.ragassistant.model.Citation;
import com.example.ragassistant.repository.DocumentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private static final String NOT_FOUND =
            "I could not find this information in the uploaded documents.";

    private final DocumentRepository documentRepository;
    private final ChatClient chatClient;

    public RagService(DocumentRepository documentRepository, ChatClient.Builder chatClientBuilder) {
        this.documentRepository = documentRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse answer(String question) {
        List<DocumentRepository.StoredChunk> chunks = documentRepository.search(question, 5);
        if (chunks.isEmpty()) {
            return new ChatResponse(NOT_FOUND, List.of());
        }

        String context = chunks.stream()
                .map(chunk -> "[" + chunk.filename() + ", page " + chunk.pageNumber()
                        + ", chunk " + chunk.id() + "]\n" + chunk.content())
                .reduce("", (left, right) -> left + "\n\n" + right);

        String answer = chatClient.prompt()
                .system("""
                        You are an enterprise document assistant.
                        Answer using only the provided document context.
                        If the context does not support an answer, respond exactly with:
                        I could not find this information in the uploaded documents.
                        Do not use general knowledge. Do not invent facts or sources.
                        Do not add a Sources section; the application supplies citations separately.
                        """)
                .user("Question: " + question + "\n\nDocument context:\n" + context)
                .call()
                .content();

        List<Citation> citations = chunks.stream()
                .map(chunk -> new Citation(chunk.filename(), chunk.pageNumber(),
                        "document-" + chunk.documentId() + "-chunk-" + chunk.chunkNumber()))
                .toList();
        return new ChatResponse(answer, citations);
    }
}
