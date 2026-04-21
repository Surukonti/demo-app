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

@CrossOrigin(origins = "https://german-ui.onrender.com")
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
    public Word saveData(@RequestBody Word request) {
        return repository.save(request);
    }
    @GetMapping("/word/random")
    public Word getRandomWord() {
        List<Word> words = repository.findAll();

        if (words.isEmpty()) {
            throw new RuntimeException("No words in database");
        }

        return words.get(new Random().nextInt(words.size()));
    }

    @GetMapping("/debug/messages")
    public Collection<Word> all() {
        return repository.findAll();
    }
    @GetMapping("/debug/db")
    public String getDbName() {
        return mongoTemplate.getDb().getName();
    }

    @GetMapping("/debug/env")
    public String env() {
        return System.getenv("SPRING_DATA_MONGODB_URI");
    }


    @GetMapping("/debug/real-uri")
    public String getRealUri() {
        return mongoClient.getClusterDescription().getClusterSettings().getHosts().toString();
    }
}
