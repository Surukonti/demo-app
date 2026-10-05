package com.example.demo.model;

public record AiWordRequest(
        String germanWord,
        String targetLanguage,
        String level
) {
}
