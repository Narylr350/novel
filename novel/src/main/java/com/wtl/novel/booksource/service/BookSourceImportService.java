package com.wtl.novel.booksource.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.booksource.entity.BookSourceEntity;
import com.wtl.novel.booksource.model.BookSource;
import com.wtl.novel.booksource.repository.BookSourceRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookSourceImportService {
    private final BookSourceRepository repository;
    private final BookSourceRuleValidator validator;
    private final ObjectMapper objectMapper;

    public BookSourceImportService(BookSourceRepository repository, BookSourceRuleValidator validator) {
        this(repository, validator, new ObjectMapper());
    }

    BookSourceImportService(BookSourceRepository repository, BookSourceRuleValidator validator, ObjectMapper objectMapper) {
        this.repository = repository;
        this.validator = validator;
        this.objectMapper = objectMapper;
    }

    public ImportResult importSources(String sourceJson) {
        List<JsonNode> nodes = parseSourceNodes(sourceJson);
        List<SourceSummary> summaries = new ArrayList<>();
        List<BookSourceRuleValidator.RuleIssue> issues = new ArrayList<>();
        for (JsonNode node : nodes) {
            BookSource source = toSource(node);
            List<BookSourceRuleValidator.RuleIssue> sourceIssues = validator.validate(source);
            issues.addAll(sourceIssues);
            if (sourceIssues.stream().anyMatch(issue -> "error".equals(issue.severity()))) {
                continue;
            }
            BookSourceEntity saved = repository.save(toEntity(source, node));
            summaries.add(toSummary(saved));
        }
        return new ImportResult(true, summaries.size(), summaries, issues);
    }

    public List<SourceSummary> listSources() {
        return repository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public List<BookSourceRuleValidator.RuleIssue> validateSourceJson(String sourceJson) {
        return parseSourceNodes(sourceJson).stream()
                .map(this::toSource)
                .flatMap(source -> validator.validate(source).stream())
                .toList();
    }

    private List<JsonNode> parseSourceNodes(String sourceJson) {
        if (sourceJson == null || sourceJson.isBlank()) {
            throw new IllegalArgumentException("sourceJson is required");
        }
        try {
            JsonNode root = objectMapper.readTree(sourceJson);
            if (root.isArray()) {
                List<JsonNode> nodes = new ArrayList<>();
                root.forEach(nodes::add);
                return nodes;
            }
            if (root.isObject()) {
                return List.of(root);
            }
            throw new IllegalArgumentException("sourceJson must be a JSON object or array");
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("sourceJson is not valid JSON", e);
        }
    }

    private BookSource toSource(JsonNode node) {
        try {
            return objectMapper.treeToValue(node, BookSource.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("sourceJson cannot be parsed as BookSource", e);
        }
    }

    private BookSourceEntity toEntity(BookSource source, JsonNode node) {
        BookSourceEntity entity = repository.findByBookSourceUrl(source.getBookSourceUrl())
                .orElseGet(BookSourceEntity::new);
        entity.setSourceId(stableSourceId(source.getBookSourceUrl()));
        entity.setBookSourceUrl(source.getBookSourceUrl());
        entity.setBookSourceName(source.getBookSourceName());
        entity.setBookSourceGroup(source.getBookSourceGroup());
        entity.setEnabled(source.isEnabled());
        entity.setRawJson(node.toString());
        return entity;
    }

    private SourceSummary toSummary(BookSourceEntity entity) {
        return new SourceSummary(
                entity.getSourceId(),
                entity.getBookSourceUrl(),
                entity.getBookSourceName(),
                entity.isEnabled()
        );
    }

    private String stableSourceId(String bookSourceUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bookSourceUrl.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                builder.append(String.format("%02x", hash[i]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public record SourceSummary(String sourceId, String bookSourceUrl, String bookSourceName, boolean enabled) {
    }

    public record ImportResult(
            boolean ok,
            int count,
            List<SourceSummary> sources,
            List<BookSourceRuleValidator.RuleIssue> issues
    ) {
    }
}
