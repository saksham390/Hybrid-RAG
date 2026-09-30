package com.example.ragassistant.controller;

import com.example.ragassistant.model.DocumentResponse;
import com.example.ragassistant.model.UploadResponse;
import com.example.ragassistant.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        return documentService.process(file);
    }

    @GetMapping
    public List<DocumentResponse> list() {
        return documentService.list();
    }
}