package com.example.demo.controller;

import com.example.demo.importer.WordhoardImporter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final WordhoardImporter importer;
    // private final DariTranslationService dariTranslationService;

    public ImportController(
            WordhoardImporter importer) {

        this.importer = importer;
    }

    @PostMapping
    public String importWords(
            @RequestParam(defaultValue = "B1") String level,
            @RequestParam(defaultValue = "10") int limit) {

        importer.importWords(level, limit);

        return "Import started for " + limit + " " + level + " words.";
    }

    @GetMapping("/word")
    public String importSingleWord(@RequestParam String word) {

        importer.importSingleWord(word);

        return "Import started for word: " + word;
    }

//    @GetMapping("/dari")
//    public String testDari(@RequestParam String word) {
//
//        String result = dariTranslationService.translateGermanToDari(word);
//
//        return word + " -> " + result;
//    }

    @PostMapping("/meanings")
    public String importMeanings(
            @RequestParam(defaultValue = "B1") String level,
            @RequestParam(defaultValue = "10") int limit) {

        Thread thread = new Thread(() -> {
            importer.importMeanings(level, limit);
        });

        thread.setDaemon(true);
        thread.start();

        return "Meanings import started for " + limit + " " + level + " words.";
    }
}