package com.example.demo.controller;

import com.example.demo.model.Word;
import com.example.demo.repository.WordRepository;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private WordRepository repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoClient mongoClient;


    @PostMapping("/save")
    public List<Word> saveData(@RequestBody List<Word> request) {
        return repository.saveAll(request);
    }

    @GetMapping("/word/random")
    public Word getRandomWord() {
        return repository.findRandomWord();
    }
//    @GetMapping("/word/random")
//    public Word getRandomWord() {
//        List<Word> words = repository.findAll();
//
//        if (words.isEmpty()) {
//            throw new RuntimeException("No data found in database");
//        }
//
//        return words.get(new Random().nextInt(words.size()));
//    }

    @GetMapping("/debug/all")
    public List<Word> debugAll() {
        return repository.findAll();
    }

    @GetMapping("/debug/db")
    public String getDbName() {
        return mongoTemplate.getDb().getName();
    }

    @GetMapping("/word/all")
    public List<Word> getAllWords() {
        return repository.findAll();
    }


    @GetMapping("/debug/real-uri")
    public String getRealUri() {
        return mongoClient.getClusterDescription().getClusterSettings().getHosts().toString();
    }
}
