package com.aichatbot.backend.service;

import com.aichatbot.backend.model.DocumentChunk;
import com.aichatbot.backend.model.DocumentChunkRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentVectorStoreRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DocumentChunk> rowMapper = this::mapRow;

    public DocumentVectorStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<DocumentChunkRequest> chunks) {
        String sql = """
                INSERT INTO document_chunks (document_name, section, page_number, content, embedding)
                VALUES (?, ?, ?, ?, CAST(? AS vector))
                """;
        jdbcTemplate.batchUpdate(sql, chunks, chunks.size(), (ps, chunk) -> {
            ps.setString(1, chunk.documentName());
            ps.setString(2, chunk.section());
            if (chunk.pageNumber() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            else {
                ps.setInt(3, chunk.pageNumber());
            }
            ps.setString(4, chunk.content());
            ps.setString(5, toVectorLiteral(chunk.embedding()));
        });
    }

    public List<DocumentChunk> similaritySearch(float[] embedding, int topK) {
        String sql = """
                SELECT id,
                       document_name,
                       section,
                       page_number,
                       content,
                       1 - (embedding <=> CAST(? AS vector)) AS similarity_score
                FROM document_chunks
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """;
        String vectorLiteral = toVectorLiteral(embedding);
        return jdbcTemplate.query(sql, rowMapper, vectorLiteral, vectorLiteral, topK);
    }

    private DocumentChunk mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DocumentChunk(
                rs.getLong("id"),
                rs.getString("document_name"),
                rs.getString("section"),
                rs.getObject("page_number", Integer.class),
                rs.getString("content"),
                rs.getDouble("similarity_score"));
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(embedding[i]);
        }
        return builder.append(']').toString();
    }
}
