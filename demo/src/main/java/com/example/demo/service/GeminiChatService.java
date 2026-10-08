package com.example.demo.service;

import com.example.demo.dto.ChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
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

    public String chat(
            ChatRequest request,
            List<MultipartFile> images) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured");
        }

        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        String message = request.getMessage() == null
                ? ""
                : request.getMessage().trim();

        boolean hasImages = images != null && !images.isEmpty();

        if (message.isBlank() && !hasImages) {
            throw new IllegalArgumentException(
                    "message or image is required");
        }

        String language = request.getLanguage();

        if (language == null || language.isBlank()) {
            language = "English";
        }

        String prompt = """
                You are a helpful conversational AI assistant inside a German
                language learning application.

                The user may speak, type, or send images.

                Understand what the user is asking and respond helpfully.

                If an image is attached:
                - Carefully inspect the image.
                - Read any visible text.
                - If the user asks for a translation, translate the visible
                  text into the requested language.
                - Explain the meaning when requested.
                - If the user asks something else about the image, answer
                  based on what you can see.
                - Do not say that you cannot see the image if an image was
                  actually provided.

                Preferred response language: %s

                If the user asks about German, provide useful German examples
                when appropriate.

                Do not restrict the response to B1 or B2 unless the user
                explicitly asks for that.

                User message:
                %s
                """.formatted(
                language,
                message.isBlank()
                        ? "Please analyze the attached image(s) and help the user."
                        : message
        );

        try {
            List<Map<String, Object>> parts = new ArrayList<>();

            // Add text prompt
            parts.add(Map.of("text", prompt));

            // Add images
            if (hasImages) {
                for (MultipartFile image : images) {

                    if (image == null || image.isEmpty()) {
                        continue;
                    }

                    String contentType = image.getContentType();

                    if (contentType == null ||
                            !contentType.startsWith("image/")) {
                        continue;
                    }

                    String base64 =
                            Base64.getEncoder()
                                    .encodeToString(image.getBytes());

                    parts.add(
                            Map.of(
                                    "inline_data",
                                    Map.of(
                                            "mime_type", contentType,
                                            "data", base64
                                    )
                            )
                    );
                }
            }

            Map<String, Object> body = Map.of(
                    "contents",
                    List.of(
                            Map.of(
                                    "parts",
                                    parts
                            )
                    )
            );

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
                    "Gemini request was interrupted", e);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Could not call Gemini API", e);
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
                        "Gemini returned no candidates");
            }

            return candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (IOException | RuntimeException e) {

            throw new IllegalStateException(
                    "Could not parse Gemini response", e);
        }
    }
}