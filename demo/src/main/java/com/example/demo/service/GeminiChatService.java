package com.example.demo.service;

import com.example.demo.dto.ChatRequest;
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

@Service
public class GeminiChatService {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiChatService(
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-3.5-flash-lite}") String model) {

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chat(ChatRequest request) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured"
            );
        }

        if (request == null ||
                request.getMessage() == null ||
                request.getMessage().isBlank()) {

            throw new IllegalArgumentException(
                    "message is required"
            );
        }

        String language = request.getLanguage();

        if (language == null || language.isBlank()) {
            language = "English";
        }

        String prompt = """
                You are a helpful conversational AI assistant inside a German
                language learning application.

                The user may speak or type naturally.

                Understand what the user is asking and respond helpfully.
                The user may ask for:
                - translations
                - meanings of German words
                - German grammar explanations
                - corrections
                - example sentences
                - language learning questions
                - general questions
                - normal conversation

                Respond naturally, like a conversational assistant.

                Preferred response language: %s

                If the user asks about German, provide useful German examples
                when appropriate.

                Do not restrict the response to B1 or B2 unless the user
                explicitly asks for that.

                User message:
                %s
                """.formatted(
                language,
                request.getMessage().trim()
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts",
                                List.of(Map.of("text", prompt))
                        )
                )
        );

        try {
            String requestBody =
                    objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL.formatted(model)))
                    .header("x-goog-api-key", apiKey)
                    .header(
                            HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(requestBody)
                    )
                    .build();

            long start = System.currentTimeMillis();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("Gemini chat API time: "
                    + (System.currentTimeMillis() - start) + " ms");

            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Gemini API returned HTTP "
                                + response.statusCode()
                );
            }

            return extractResponse(response.body());

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Gemini request was interrupted",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Could not call Gemini API",
                    e
            );
        }
    }

    private String extractResponse(String responseBody) {

        try {
            JsonNode root =
                    objectMapper.readTree(responseBody);

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray() ||
                    candidates.isEmpty()) {

                throw new IllegalStateException(
                        "Gemini returned no candidates"
                );
            }

            return candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (IOException | RuntimeException e) {

            throw new IllegalStateException(
                    "Could not parse Gemini response",
                    e
            );
        }
    }
}