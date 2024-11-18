package com.innovatelu;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> documentStorage;


    public DocumentManager(Map<String, Document> storage) {
        this.documentStorage = storage;
    }

    public DocumentManager() {
        this(new HashMap<>());
    }

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */

    public Document save(Document document) {
        Objects.requireNonNull(document);

        assignIdIfAbsent(document);

        assignCreatedTimestampIfAbsent(document);

        storeDocument(document);
        return document;
    }


    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        Objects.requireNonNull(request);

        return documentStorage.values().stream()
                .filter(document -> matchesSearchRequest(document, request))
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStorage.get(id));
    }

    private void assignIdIfAbsent(Document document) {
        if (Objects.isNull(document.getId()) || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }
    }

    private void assignCreatedTimestampIfAbsent(Document document) {
        if (Objects.isNull(document.getCreated())) {
            document.setCreated(Instant.now());
        }
    }

    private void storeDocument(Document document) {
        documentStorage.put(document.id, document);
    }

    private boolean matchesSearchRequest(Document document, SearchRequest request) {
        return Stream.of(
                matchesTitlePrefixes(document, request),
                matchesContent(document, request),
                matchesAuthorId(document, request),
                matchesCreatedFrom(document, request),
                matchesCreatedTo(document, request)
        ).anyMatch(Boolean::booleanValue);
    }

    private boolean matchesTitlePrefixes(Document document, SearchRequest request) {
        return isNonEmpty(request.getTitlePrefixes())
                && request.getTitlePrefixes().stream()
                .anyMatch(prefix -> Objects.nonNull(document.getTitle())
                        && document.getTitle().toLowerCase().startsWith(prefix.toLowerCase()));
    }

    private boolean matchesContent(Document document, SearchRequest request) {
        return isNonEmpty(request.getContainsContents())
                && request.getContainsContents().stream()
                .anyMatch(content -> Objects.nonNull(document.getContent())
                        && document.getContent().toLowerCase().contains(content.toLowerCase()));
    }

    private boolean matchesAuthorId(Document document, SearchRequest request) {
        return isNonEmpty(request.getAuthorIds())
                && request.getAuthorIds().contains(document.getAuthor().getId());
    }

    private boolean matchesCreatedFrom(Document document, SearchRequest request) {
        return Objects.nonNull(request.getCreatedFrom())
                && document.getCreated().isAfter(request.getCreatedFrom());
    }

    private boolean matchesCreatedTo(Document document, SearchRequest request) {
        return Objects.nonNull(request.getCreatedTo())
                && document.getCreated().isBefore(request.getCreatedTo());
    }


    private <T> boolean isNonEmpty(List<T> list) {
        return Objects.nonNull(list) && !list.isEmpty();
    }


    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}