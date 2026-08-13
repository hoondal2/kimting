package com.kimting.kimting.memory.infrastructure.ai;

import com.kimting.kimting.memory.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;

    @Override
    public void add(List<Document> documents) {
        vectorStore.add(documents);
    }

    @Override
    public List<Document> search(SearchRequest request) {
        return vectorStore.similaritySearch(request);
    }

    @Override
    public void delete(List<String> ids) {
        vectorStore.delete(ids);
    }
}
