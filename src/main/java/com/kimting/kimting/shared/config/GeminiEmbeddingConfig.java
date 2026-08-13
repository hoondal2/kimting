package com.kimting.kimting.shared.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class GeminiEmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel geminiEmbeddingModel(
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        return new GeminiEmbeddingModel(apiKey);
    }

    static class GeminiEmbeddingModel implements EmbeddingModel {

        private static final String EMBED_URL =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

        private final String apiKey;
        private final RestClient restClient;

        GeminiEmbeddingModel(String apiKey) {
            this.apiKey = apiKey;
            this.restClient = RestClient.create();
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<String> inputs = request.getInstructions();
            List<Embedding> embeddings = new ArrayList<>(inputs.size());
            for (int i = 0; i < inputs.size(); i++) {
                embeddings.add(new Embedding(embedText(inputs.get(i)), i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return embedText(document.getText());
        }

        @Override
        public int dimensions() {
            return 768;
        }

        private float[] embedText(String text) {
            var body = Map.of(
                    "model", "models/gemini-embedding-001",
                    "content", Map.of("parts", List.of(Map.of("text", text))),
                    "outputDimensionality", 768
            );

            EmbedResponse resp = restClient.post()
                    .uri(EMBED_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(EmbedResponse.class);

            double[] values = resp.embedding().values();
            float[] result = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (float) values[i];
            }
            return result;
        }

        record EmbedResponse(@JsonProperty("embedding") EmbedData embedding) {}
        record EmbedData(@JsonProperty("values") double[] values) {}
    }
}
