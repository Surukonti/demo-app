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

        importer.importWords(level, limit);

        return "Import started for " + limit + " " + level + " words.";
    }


}