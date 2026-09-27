package com.example.demo.controller;

import com.example.demo.importer.WordhoardImporter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final WordhoardImporter importer;

    public ImportController(WordhoardImporter importer) {
        this.importer = importer;
    }

    @PostMapping
    public String importWords(
            @RequestParam(defaultValue = "B1") String level,
            @RequestParam(defaultValue = "10") int limit) {

        Thread thread = new Thread(() -> {
            importer.importWords(level, limit);
        });

        thread.setDaemon(true);
        thread.start();

        return "Import started in background for " + limit + " " + level + " words.";
    }

    @GetMapping("/word")
    public String importSingleWord(@RequestParam String word) {

        importer.importSingleWord(word);

        return "Import started for word: " + word;
    }
}