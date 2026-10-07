package com.example.demo.controller;

import com.example.demo.model.AiWordRequest;
import com.example.demo.model.AiWordResponse;
import com.example.demo.service.GeminiWordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ChatRequest;
import com.example.demo.service.GeminiChatService;
import java.util.Map;

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

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody ChatRequest request) {

        String response = geminiChatService.chat(request);

        return ResponseEntity.ok(
                Map.of("response", response)
        );
    }
}
