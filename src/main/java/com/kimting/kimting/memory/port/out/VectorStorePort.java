package com.kimting.kimting.memory.port.out;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;

public interface VectorStorePort {

    void add(List<Document> documents);

    List<Document> search(SearchRequest request);

    void delete(List<String> ids);
}
