package com.example.ragassistant.service;

import com.example.ragassistant.model.DocumentResponse;
import com.example.ragassistant.model.UploadResponse;
import com.example.ragassistant.repository.DocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DocumentService {

    private static final int WORDS_PER_CHUNK = 700;
    private static final int OVERLAP_WORDS = 100;

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public UploadResponse process(MultipartFile file) {
        try {
            byte[] pdf = file.getBytes();
            long documentId = documentRepository.saveDocument(file.getOriginalFilename());
            int chunkCount = 0;

            try (PDDocument document = Loader.loadPDF(pdf)) {
                PDFTextStripper stripper = new PDFTextStripper();
                for (int pageNumber = 1; pageNumber <= document.getNumberOfPages(); pageNumber++) {
                    stripper.setStartPage(pageNumber);
                    stripper.setEndPage(pageNumber);
                    String pageText = stripper.getText(document).trim();
                    List<String> chunks = chunk(pageText);
                    for (int chunkNumber = 0; chunkNumber < chunks.size(); chunkNumber++) {
                        documentRepository.saveChunk(documentId, pageNumber, chunkNumber + 1,
                                chunks.get(chunkNumber));
                        chunkCount++;
                    }
                }
                return new UploadResponse(documentId, file.getOriginalFilename(),
                        document.getNumberOfPages(), chunkCount);
            }
        }
        catch (IOException exception) {
            throw new IllegalArgumentException("The uploaded file is not a readable PDF", exception);
        }
    }

    public List<DocumentResponse> list() {
        return documentRepository.findAllDocuments();
    }

    private List<String> chunk(String text) {
        if (text.isBlank()) {
            return List.of();
        }

        List<String> words = new ArrayList<>(Arrays.asList(text.split("\\s+")));
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < words.size()) {
            int end = Math.min(start + WORDS_PER_CHUNK, words.size());
            chunks.add(String.join(" ", words.subList(start, end)));
            if (end == words.size()) {
                break;
            }
            start = end - OVERLAP_WORDS;
        }
        return chunks;
    }
}