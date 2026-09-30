package com.example.ragassistant.repository;

import com.example.ragassistant.model.DocumentResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long saveDocument(String filename) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO documents (filename) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, filename);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("The database did not return a document ID");
        }
        return key.longValue();
    }

    public void saveChunk(long documentId, int pageNumber, int chunkNumber, String content) {
        jdbcTemplate.update(
                "INSERT INTO document_chunks (document_id, page_number, chunk_number, content) VALUES (?, ?, ?, ?)",
                documentId, pageNumber, chunkNumber, content);
    }

    public List<DocumentResponse> findAllDocuments() {
        return jdbcTemplate.query(
                "SELECT id, filename, uploaded_at FROM documents ORDER BY uploaded_at DESC",
                (resultSet, rowNumber) -> new DocumentResponse(
                        resultSet.getLong("id"),
                        resultSet.getString("filename"),
                        resultSet.getTimestamp("uploaded_at").toInstant()));
    }

    public List<StoredChunk> search(String question, int limit) {
        return jdbcTemplate.query(
                """
                SELECT c.id, c.document_id, d.filename, c.page_number, c.chunk_number, c.content
                FROM document_chunks c
                JOIN documents d ON d.id = c.document_id
                WHERE c.search_vector @@ plainto_tsquery('english', ?)
                ORDER BY ts_rank_cd(c.search_vector, plainto_tsquery('english', ?)) DESC
                LIMIT ?
                """,
                (resultSet, rowNumber) -> new StoredChunk(
                        resultSet.getLong("id"),
                        resultSet.getLong("document_id"),
                        resultSet.getString("filename"),
                        resultSet.getInt("page_number"),
                        resultSet.getInt("chunk_number"),
                        resultSet.getString("content")),
                question, question, limit);
    }

    public record StoredChunk(
            long id,
            long documentId,
            String filename,
            int pageNumber,
            int chunkNumber,
            String content) {
    }
}