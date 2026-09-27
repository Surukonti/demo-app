package com.example.demo.importer;

import com.example.demo.model.Word;
import com.example.demo.repository.WordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class WordhoardImporter {

    private final WordRepository repository;

    public WordhoardImporter(WordRepository repository) {
        this.repository = repository;
    }

    public void importWords(String targetLevel, int limit) {

        String fileName = "data/wordhoard-de.csv";

        try {
            InputStream inputStream =
                    getClass().getClassLoader()
                            .getResourceAsStream(fileName);

            if (inputStream == null) {
                System.out.println("CSV file not found!");
                return;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );

            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String line;
            int processed = 0;

            for (CSVRecord record : csvParser) {

                if (processed >= limit) {
                    break;
                }

                String german = record.get("lemma");
                String partOfSpeech = record.get("pos");
                String article = record.get("gender");
                String level = record.get("cefr_estimate");
                String forms = record.get("forms");


                if (!targetLevel.equals(level)) {
                    continue;
                }


                String url =
                        "https://api.wiktapi.dev/v1/de/word/"
                                + german
                                + "/translations";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "GermanLearningApp/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response =
                        client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString()
                        );


                Word word;

                var existingWords = repository.findByGerman(german);

                if (!existingWords.isEmpty()) {
                    word = existingWords.get(0);
                } else {
                    word = new Word();
                }

                word.setGerman(german);
                word.setPartOfSpeech(partOfSpeech);
                word.setArticle(article);
                word.setLevel(level);
                word.setForms(forms);

                if (response.statusCode() != 200) {
                    System.out.println(
                            "Translation API failed for "
                                    + german
                                    + " - HTTP "
                                    + response.statusCode()
                    );
                    continue;
                }

                JsonNode root = mapper.readTree(response.body());
                String english = findFirstTranslation(root, "en");
                String arabic = findFirstTranslation(root, "ar");
                String ukrainian = findFirstTranslation(root, "uk");
                String russian = findFirstTranslation(root, "ru");
                String turkish = findFirstTranslation(root, "tr");

                System.out.println("WORD: " + german);
                System.out.println("English: " + english);
                System.out.println("Arabic: " + arabic);
                System.out.println("Ukrainian: " + ukrainian);
                System.out.println("Russian: " + russian);
                System.out.println("Turkish: " + turkish);

                if (english == null || english.isBlank()) {
                    System.out.println("No English translation: " + german);
                    continue;
                }

                word.setEnglish(english);
                word.setArabic(arabic);
                word.setUkrainian(ukrainian);
                word.setRussian(russian);
                word.setTurkish(turkish);

                repository.save(word);

                System.out.println(
                        "Saved/updated: "
                                + word.getGerman()
                                + " -> "
                                + word.getEnglish()
                );

                processed++;
            }

            csvParser.close();

            System.out.println(
                    "Import completed. Saved: "
                            + processed
                            + " words."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String findFirstTranslation(JsonNode node, String languageCode) {

        if (node.isObject()) {

            JsonNode langCode = node.get("lang_code");
            JsonNode word = node.get("word");

            if (langCode != null
                    && languageCode.equals(langCode.asText())
                    && word != null) {

                return word.asText();
            }

            var fields = node.fields();

            while (fields.hasNext()) {

                String result =
                        findFirstTranslation(
                                fields.next().getValue(),
                                languageCode
                        );

                if (result != null) {
                    return result;
                }
            }

        } else if (node.isArray()) {

            for (JsonNode child : node) {

                String result =
                        findFirstTranslation(
                                child,
                                languageCode
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
//    private String getFallbackTranslation(
//            HttpClient client,
//            String german,
//            String targetLanguage,
//            String existingTranslation) {
//
//        // WiktApi already has a translation → keep it
//        if (existingTranslation != null
//                && !existingTranslation.isBlank()) {
//            return existingTranslation;
//        }
//
//        try {
//
//            String url =
//                    "https://api.mymemory.translated.net/get"
//                            + "?q=" + java.net.URLEncoder.encode(
//                            german,
//                            StandardCharsets.UTF_8)
//                            + "&langpair=de%7C"
//                            + targetLanguage;
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(url))
//                    .header("User-Agent", "GermanLearningApp/1.0")
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response =
//                    client.send(
//                            request,
//                            HttpResponse.BodyHandlers.ofString()
//                    );
//
//            if (response.statusCode() != 200) {
//                return null;
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(response.body());
//
//            JsonNode responseData =
//                    root.get("responseData");
//
//            if (responseData == null) {
//                return null;
//            }
//
//            JsonNode translatedText =
//                    responseData.get("translatedText");
//
//            JsonNode match =
//                    responseData.get("match");
//
//            if (translatedText == null
//                    || translatedText.asText().isBlank()) {
//                return null;
//            }
//
//            // Reject weak MyMemory matches
//            if (match == null || match.asDouble() < 0.90) {
//                System.out.println(
//                        "Rejected weak fallback: "
//                                + german
//                                + " -> "
//                                + targetLanguage
//                                + " = "
//                                + translatedText.asText()
//                                + " (match="
//                                + (match == null
//                                ? "null"
//                                : match.asText())
//                                + ")"
//                );
//
//                return null;
//            }
//
//            return translatedText.asText();
//
//        } catch (Exception e) {
//
//            System.out.println(
//                    "Fallback error for "
//                            + german
//                            + " -> "
//                            + targetLanguage
//                            + " : "
//                            + e.getMessage()
//            );
//
//            return null;
//        }
//    }
}