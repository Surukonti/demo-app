package com.example.demo.model;

import java.util.List;

public record AiWordResponse(
        String germanWord,
        String translation,
        List<String> meanings,
        String targetLanguage,
        String wordType,
        String level,
        String article,
        String plural,
        List<ExampleSentence> examples,
        VerbForms verbForms
) {
    public record ExampleSentence(
            String german,
            String translation
    ) {}

    public record VerbForms(
            String infinitiveGerman,
            String infinitiveTranslation,
            String praeteritumGerman,
            String praeteritumTranslation,
            String perfektGerman,
            String perfektTranslation
    ) {}
}
