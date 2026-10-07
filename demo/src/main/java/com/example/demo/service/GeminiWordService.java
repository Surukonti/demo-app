package com.example.demo.service;

import com.example.demo.model.AiWordRequest;
import com.example.demo.model.AiWordResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiWordService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "English", "Turkish", "Ukrainian", "Arabic", "Dari"
    );

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final int MAX_ATTEMPTS = 3;

    // Avoid paying for the same word/language combination repeatedly.
    private final Map<String, AiWordResponse> cache = new ConcurrentHashMap<>();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiWordService(
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-3.5-flash-lite}") String model) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AiWordResponse getWord(AiWordRequest request) {
        validate(request);

        String cacheKey = request.germanWord().trim().toLowerCase()
                + "|" + request.targetLanguage();

        AiWordResponse cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String prompt = buildPrompt(request);
        String requestBody = buildRequestBody(prompt);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL.formatted(model)))
                        .header("x-goog-api-key", apiKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        httpRequest,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() / 100 == 2) {
                    AiWordResponse result = parseResponse(response.body());
                    cache.put(cacheKey, result);
                    return result;
                }

                if (response.statusCode() != 503 || attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException(
                            "Gemini API returned HTTP " + response.statusCode()
                    );
                }

                Thread.sleep(1000L * attempt);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Gemini request was interrupted", e
                );

            } catch (IOException e) {
                throw new IllegalStateException(
                        "Could not call Gemini API", e
                );
            }
        }

        throw new IllegalStateException("Gemini request failed");
    }

    private void validate(AiWordRequest request) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        if (request == null ||
                request.germanWord() == null ||
                request.germanWord().isBlank()) {

            throw new IllegalArgumentException("germanWord is required");
        }

        if (request.targetLanguage() == null ||
                !SUPPORTED_LANGUAGES.contains(request.targetLanguage())) {

            throw new IllegalArgumentException(
                    "targetLanguage must be one of: " + SUPPORTED_LANGUAGES
            );
        }
    }

    private String buildPrompt(AiWordRequest request) {

        return """
            You are a German language teacher and German vocabulary expert.
            Analyze the German word below and return ONLY the requested JSON structure.

            German word: %s
            Target translation language: %s

            Important:
            - This is a WORD SEARCH, not a learner-level exercise.
            - Do NOT assume B1, B2, A1, A2, or any other learner level.
            - Determine the German word's actual CEFR level independently.
            - The returned "level" must describe the word itself, not the learner.

            Rules:
            - Preserve the searched German word.
            - Give the most useful primary translation in the target language.
            - Give 3 to 6 useful, distinct meanings in the target language when applicable.
            - Identify the word type (for example: VERB, NOUN, ADJECTIVE, ADVERB).
            - For nouns, provide the German article and common plural when applicable.
            - Provide exactly 3 natural example sentences suitable for a German learner.
            - Every example must contain both German and its translation into the target language.
            - Keep the German example sentence unchanged; translate only the translation field.
            - If the word is a verb, provide infinitive, Präteritum and Perfekt, with both German forms and their meanings.
            - If it is not a verb, return empty strings for all verbForms fields.
            - Do not invent a verb form for a non-verb.
            - Keep translations natural.
            - Do not include Markdown, explanations, comments, or additional fields.
            """.formatted(
                request.germanWord().trim(),
                request.targetLanguage()
        );
    }

    private String buildRequestBody(String prompt) {
        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "germanWord", Map.of("type", "STRING"),
                        "translation", Map.of("type", "STRING"),
                        "meanings", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING")
                        ),
                        "targetLanguage", Map.of("type", "STRING"),
                        "wordType", Map.of("type", "STRING"),
                        "level", Map.of("type", "STRING"),
                        "article", Map.of("type", "STRING"),
                        "plural", Map.of("type", "STRING"),
                        "examples", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "german", Map.of("type", "STRING"),
                                                "translation", Map.of("type", "STRING")
                                        ),
                                        "required", List.of("german", "translation")
                                )
                        ),
                        "verbForms", Map.of(
                                "type", "OBJECT",
                                "properties", Map.of(
                                        "infinitiveGerman", Map.of("type", "STRING"),
                                        "infinitiveTranslation", Map.of("type", "STRING"),
                                        "praeteritumGerman", Map.of("type", "STRING"),
                                        "praeteritumTranslation", Map.of("type", "STRING"),
                                        "perfektGerman", Map.of("type", "STRING"),
                                        "perfektTranslation", Map.of("type", "STRING")
                                ),
                                "required", List.of(
                                        "infinitiveGerman",
                                        "infinitiveTranslation",
                                        "praeteritumGerman",
                                        "praeteritumTranslation",
                                        "perfektGerman",
                                        "perfektTranslation"
                                )
                        )
                ),
                "required", List.of(
                        "germanWord",
                        "translation",
                        "targetLanguage",
                        "wordType",
                        "level",
                        "article",
                        "plural",
                        "examples",
                        "verbForms"
                )
        );

        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "responseSchema", schema,
                "temperature", 0.2
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts",
                                List.of(Map.of("text", prompt))
                        )
                ),
                "generationConfig", generationConfig
        );

        try {
            return objectMapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not build Gemini request", e
            );
        }
    }

    private AiWordResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException(
                        "Gemini returned no candidates"
                );
            }

            String json = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return objectMapper.readValue(json, AiWordResponse.class);

        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException(
                    "Could not parse Gemini response", e
            );
        }
    }
}