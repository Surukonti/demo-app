package com.example.demo.controller;

import com.example.demo.model.Word;
import com.example.demo.repository.WordRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://rams-langui.onrender.com"
})
@RestController
@RequestMapping("/api/random")
public class RandomWordController {

    private final WordRepository repository;

    public RandomWordController(WordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/vocabulary")
    public Word randomVocabulary(HttpSession session) {

        List<Word> b2Words = repository.findAll()
                .stream()
                .filter(word -> "B2".equalsIgnoreCase(word.getLevel()))
                .toList();

        if (b2Words.isEmpty()) {
            throw new RuntimeException("No B2 vocabulary found");
        }

        Set<String> used = getUsedWords(session, "vocabulary");

        List<Word> available = b2Words.stream()
                .filter(word -> !used.contains(word.getGerman().toLowerCase()))
                .toList();

        // Start a fresh cycle when all B2 words have been shown.
        if (available.isEmpty()) {
            used.clear();
            available = b2Words;
        }

        Word selected = randomWord(available);

        used.add(selected.getGerman().toLowerCase());

        return selected;
    }

    @GetMapping("/puzzle")
    public Map<String, String> randomPuzzle(HttpSession session) {

        List<Word> words = repository.findAll()
                .stream()
                .filter(word -> word.getGerman() != null)
                .filter(word -> word.getGerman().length() >= 3)
                .toList();

        if (words.isEmpty()) {
            throw new RuntimeException("No puzzle words found");
        }

        Set<String> used = getUsedWords(session, "puzzle");

        List<Word> available = words.stream()
                .filter(word -> !used.contains(word.getGerman().toLowerCase()))
                .toList();

        if (available.isEmpty()) {
            used.clear();
            available = words;
        }

        Word selected = randomWord(available);

        used.add(selected.getGerman().toLowerCase());

        return Map.of(
                "german", selected.getGerman()
        );
    }

    @SuppressWarnings("unchecked")
    private Set<String> getUsedWords(
            HttpSession session,
            String key
    ) {
        String sessionKey = "randomWords_" + key;

        Set<String> used =
                (Set<String>) session.getAttribute(sessionKey);

        if (used == null) {
            used = new HashSet<>();
            session.setAttribute(sessionKey, used);
        }

        return used;
    }

    private Word randomWord(List<Word> words) {
        return words.get(
                new Random().nextInt(words.size())
        );
    }
}