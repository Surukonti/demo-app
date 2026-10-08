package com.example.demo.controller;

import com.example.demo.model.AiWordRequest;
import com.example.demo.model.AiWordResponse;
import com.example.demo.service.GeminiWordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ChatRequest;
import com.example.demo.service.GeminiChatService;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://rams-langui.onrender.com"
})
@RestController
@RequestMapping("/api/ai")
public class AiWordController {

    private final GeminiWordService geminiWordService;

    private final GeminiChatService geminiChatService;

    public AiWordController(
            GeminiWordService geminiWordService,
            GeminiChatService geminiChatService) {

        this.geminiWordService = geminiWordService;
        this.geminiChatService = geminiChatService;
    }

    @PostMapping("/word")
    public ResponseEntity<AiWordResponse> getWord(@RequestBody AiWordRequest request) {
        return ResponseEntity.ok(geminiWordService.getWord(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> chat(
            @RequestParam("message") String message,
            @RequestParam("language") String language,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        System.out.println("Message: " + message);
        System.out.println("Language: " + language);
        System.out.println("Images received: " +
                (images == null ? 0 : images.size()));

        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setLanguage(language);

        String response = geminiChatService.chat(request, images);

        return ResponseEntity.ok(Map.of("response", response));
    }
}
