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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class WordhoardImporter {

    private final WordRepository repository;

    public WordhoardImporter(WordRepository repository) {
        this.repository = repository;
    }


    public void importWords(String targetLevel, int limit) {

        String fileName = "data/wordhoard-de.csv";

        try {

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                System.out.println("CSV file not found!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            CSVParser csvParser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader);

            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            int processed = 0;

            for (CSVRecord record : csvParser) {

                if (processed >= limit) {
                    break;
                }

                String level = record.get("cefr_estimate");

                if (!targetLevel.equals(level)) {
                    continue;
                }

                boolean saved = processWord(record, client, mapper);

                if (saved) {
                    processed++;
                }
            }

            csvParser.close();

            System.out.println("Import completed. Saved: " + processed + " words.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * IMPORT ONE SPECIFIC WORD
     * =========================================================
     */

    public void importSingleWord(String targetWord) {

        String fileName = "data/wordhoard-de.csv";

        try {

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                System.out.println("CSV file not found!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            CSVParser csvParser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader);

            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            boolean found = false;

            for (CSVRecord record : csvParser) {

                String german = record.get("lemma");

                if (!targetWord.equalsIgnoreCase(german)) {
                    continue;
                }

                found = true;

                System.out.println("========================================");

                System.out.println("SINGLE WORD IMPORT: " + german);

                System.out.println("========================================");

                processWord(record, client, mapper);

                break;
            }

            csvParser.close();

            if (!found) {
                System.out.println("Word not found in CSV: " + targetWord);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * PROCESS ONE CSV WORD
     * =========================================================
     */

    private boolean processWord(CSVRecord record, HttpClient client, ObjectMapper mapper) {

        try {

            String german = record.get("lemma");
            String partOfSpeech = record.get("pos");
            String article = record.get("gender");
            String level = record.get("cefr_estimate");
            String forms = record.get("forms");


            /*
             * ---------------------------------------------------------
             * FIND EXISTING WORD
             * ---------------------------------------------------------
             */

            Word word;

            var existingWords = repository.findByGerman(german);

            if (!existingWords.isEmpty()) {

                word = existingWords.get(0);

            } else {

                word = new Word();
            }


            /*
             * ---------------------------------------------------------
             * BASIC WORD DATA
             * ---------------------------------------------------------
             */

            word.setGerman(german);
            word.setPartOfSpeech(partOfSpeech);
            word.setArticle(article);
            word.setLevel(level);
            word.setForms(forms);


            /*
             * ---------------------------------------------------------
             * 1. TRANSLATIONS
             * ---------------------------------------------------------
             */

            JsonNode translationRoot = getJson(client, german, "/translations", mapper);

            if (translationRoot == null) {

                System.out.println("Translation API failed for " + german);

                return false;
            }


            List<String> englishMeanings = findAllTranslations(translationRoot, "en");

            List<String> arabicMeanings = findAllTranslations(translationRoot, "ar");

            List<String> ukrainianMeanings = findAllTranslations(translationRoot, "uk");

            List<String> russianMeanings = findAllTranslations(translationRoot, "ru");

            List<String> turkishMeanings = findAllTranslations(translationRoot, "tr");


            String english = firstOrNull(englishMeanings);

            String arabic = firstOrNull(arabicMeanings);

            String ukrainian = firstOrNull(ukrainianMeanings);

            String russian = firstOrNull(russianMeanings);

            String turkish = firstOrNull(turkishMeanings);


            System.out.println("WORD: " + german);

            System.out.println("English meanings: " + englishMeanings);

            System.out.println("Arabic meanings: " + arabicMeanings);

            System.out.println("Ukrainian meanings: " + ukrainianMeanings);

            System.out.println("Russian meanings: " + russianMeanings);

            System.out.println("Turkish meanings: " + turkishMeanings);


            if (english == null || english.isBlank()) {

                System.out.println("No English translation: " + german);

                return false;
            }


            word.setEnglish(english);
            word.setEnglishMeanings(englishMeanings);

            word.setArabic(arabic);
            word.setUkrainian(ukrainian);
            word.setRussian(russian);
            word.setTurkish(turkish);
            word.setArabicMeanings(arabicMeanings);
            word.setUkrainianMeanings(ukrainianMeanings);
            word.setRussianMeanings(russianMeanings);
            word.setTurkishMeanings(turkishMeanings);


            /*
             * ---------------------------------------------------------
             * 2. DEFINITIONS / EXAMPLES
             * ---------------------------------------------------------
             */

            JsonNode definitionsRoot = getJson(client, german, "/definitions", mapper);

            if (definitionsRoot != null) {

                List<String> examples = new ArrayList<>();

                List<String> examplesEnglish = new ArrayList<>();


                extractExamples(definitionsRoot, examples, examplesEnglish);


                if (!examples.isEmpty()) {

                    word.setExamples(examples);
                }


                if (!examplesEnglish.isEmpty()) {

                    word.setExamplesEnglish(examplesEnglish);
                }


                System.out.println("Examples: " + examples);

                System.out.println("Example translations: " + examplesEnglish);
            }


            /*
             * ---------------------------------------------------------
             * 3. INFLECTED FORMS
             * ---------------------------------------------------------
             */

            JsonNode formsRoot = getJson(client, german, "/forms", mapper);

            if (formsRoot != null) {

                String present = findFirstForm(formsRoot, "pres", "1", "sg");


                String preterite = findFirstForm(formsRoot, "past", "1", "sg");


                if (present != null) {

                    word.setPresent("ich " + present);
                }


                if (preterite != null) {
                    word.setPreterite(preterite.startsWith("ich ") ? preterite : "ich " + preterite);
                }

                /*
                 * We intentionally do NOT automatically
                 * construct Perfekt.
                 *
                 * The correct auxiliary can be
                 * "haben" or "sein".
                 *
                 * Therefore an existing manually
                 * verified value is kept.
                 */


                System.out.println("Present: " + word.getPresent());

                System.out.println("Preterite: " + word.getPreterite());

                System.out.println("Perfect: " + word.getPerfect());
            }


            /*
             * ---------------------------------------------------------
             * SAVE
             * ---------------------------------------------------------
             */

            repository.save(word);


            System.out.println("Saved/updated: " + word.getGerman() + " -> " + word.getEnglish());

            return true;

        } catch (Exception e) {

            System.out.println("Error processing word: " + record.get("lemma"));

            e.printStackTrace();

            return false;
        }
    }


    /*
     * =========================================================
     * WIKTAPI REQUEST
     * =========================================================
     */

    private JsonNode getJson(HttpClient client, String german, String endpoint, ObjectMapper mapper) {

        try {

            String encodedWord = URLEncoder.encode(german, StandardCharsets.UTF_8).replace("+", "%20");


            String url = "https://api.wiktapi.dev/v1/de/word/" + encodedWord + endpoint;


            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "GermanLearningApp/1.0").GET().build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() != 200) {

                System.out.println("WiktApi failed for " + german + endpoint + " - HTTP " + response.statusCode());

                return null;
            }


            return mapper.readTree(response.body());

        } catch (Exception e) {

            System.out.println("WiktApi error for " + german + endpoint + " : " + e.getMessage());

            return null;
        }
    }


    /*
     * =========================================================
     * MULTIPLE TRANSLATIONS
     * =========================================================
     */

    private List<String> findAllTranslations(JsonNode node, String languageCode) {

        Set<String> results = new LinkedHashSet<>();

        collectTranslations(node, languageCode, results);

        return new ArrayList<>(results);
    }


    private void collectTranslations(JsonNode node, String languageCode, Set<String> results) {

        if (node == null) {
            return;
        }


        if (node.isObject()) {

            JsonNode langCode = node.get("lang_code");

            JsonNode word = node.get("word");


            if (langCode != null && languageCode.equals(langCode.asText()) && word != null && !word.asText().isBlank()) {

                results.add(word.asText().trim());
            }


            var fields = node.fields();

            while (fields.hasNext()) {

                collectTranslations(fields.next().getValue(), languageCode, results);
            }


        } else if (node.isArray()) {

            for (JsonNode child : node) {

                collectTranslations(child, languageCode, results);
            }
        }
    }


    /*
     * =========================================================
     * EXAMPLES
     * =========================================================
     */

    private void extractExamples(JsonNode node, List<String> examples, List<String> examplesEnglish) {

        if (node == null) {
            return;
        }


        if (node.isObject()) {

            JsonNode examplesNode = node.get("examples");


            if (examplesNode != null && examplesNode.isArray()) {

                for (JsonNode exampleNode : examplesNode) {

                    if (!exampleNode.isObject()) {
                        continue;
                    }


                    JsonNode textNode = exampleNode.get("text");


                    if (textNode != null && !textNode.asText().isBlank()) {

                        addUnique(examples, textNode.asText().trim());
                    }


                    JsonNode translationNode = exampleNode.get("translation");


                    if (translationNode != null && !translationNode.asText().isBlank()) {

                        addUnique(examplesEnglish, translationNode.asText().trim());
                    }
                }
            }


            var fields = node.fields();

            while (fields.hasNext()) {

                extractExamples(fields.next().getValue(), examples, examplesEnglish);
            }


        } else if (node.isArray()) {

            for (JsonNode child : node) {

                extractExamples(child, examples, examplesEnglish);
            }
        }
    }


    /*
     * =========================================================
     * STRUCTURED FORMS
     * =========================================================
     */

    private String findFirstForm(JsonNode node, String requiredTag, String person, String number) {

        if (node == null) {
            return null;
        }


        if (node.isObject()) {

            JsonNode formNode = node.get("form");

            JsonNode tagsNode = node.get("tags");


            if (formNode != null && !formNode.asText().isBlank() && tagsNode != null && tagsNode.isArray()) {

                boolean hasRequiredTag = false;
                boolean hasPerson = false;
                boolean hasNumber = false;


                for (JsonNode tag : tagsNode) {

                    String value = tag.asText();


                    if (requiredTag.equals(value) || ("past".equals(requiredTag) && "preterite".equals(value))) {

                        hasRequiredTag = true;
                    }


                    if (person.equals(value) || ("1".equals(person) && "first-person".equals(value))) {

                        hasPerson = true;
                    }


                    if (number.equals(value) || ("sg".equals(number) && "singular".equals(value))) {

                        hasNumber = true;
                    }
                }


                if (hasRequiredTag && hasPerson && hasNumber) {

                    return formNode.asText().trim();
                }
            }


            var fields = node.fields();

            while (fields.hasNext()) {

                String result = findFirstForm(fields.next().getValue(), requiredTag, person, number);


                if (result != null) {
                    return result;
                }
            }


        } else if (node.isArray()) {

            for (JsonNode child : node) {

                String result = findFirstForm(child, requiredTag, person, number);


                if (result != null) {
                    return result;
                }
            }
        }


        return null;
    }


    /*
     * =========================================================
     * ADD UNIQUE
     * =========================================================
     */

    private void addUnique(List<String> list, String value) {

        if (value == null || value.isBlank()) {

            return;
        }


        if (!list.contains(value)) {

            list.add(value);
        }
    }


    private String firstOrNull(List<String> values) {

        if (values == null || values.isEmpty()) {

            return null;
        }


        return values.get(0);
    }
}