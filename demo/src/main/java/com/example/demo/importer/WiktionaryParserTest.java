package com.example.demo.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WiktionaryParserTest {

    public static void main(String[] args) {

        String word = "mal";

        String url =
                "https://api.wiktapi.dev/v1/de/word/"
                        + word
                        + "/translations";

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "GermanLearningApp/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP status: " + response.statusCode());

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(response.body());

            List<String> englishTranslations = new ArrayList<>();

            findEnglishTranslations(root, englishTranslations);

            System.out.println("English translations:");

            for (String translation : englishTranslations) {
                System.out.println("- " + translation);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findEnglishTranslations(
            JsonNode node,
            List<String> translations) {

        if (node.isObject()) {

            JsonNode langCode = node.get("lang_code");
            JsonNode word = node.get("word");

            if (langCode != null
                    && "en".equals(langCode.asText())
                    && word != null) {

                String translation = word.asText();

                if (!translations.contains(translation)) {
                    translations.add(translation);
                }
            }

            node.fields().forEachRemaining(
                    entry -> findEnglishTranslations(
                            entry.getValue(),
                            translations)
            );

        } else if (node.isArray()) {

            for (JsonNode child : node) {
                findEnglishTranslations(child, translations);
            }
        }
    }
}