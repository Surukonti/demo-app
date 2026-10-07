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

        Set<String> used = getUsedWords(session, "vocabulary");

        Word selected = repository.findRandomByLevelExcluding(
                "B2",
                new ArrayList<>(used)
        );

        // Start a fresh cycle when all B2 words have been shown
        if (selected == null) {
            used.clear();

            selected = repository.findRandomByLevelExcluding(
                    "B2",
                    List.of()
            );
        }

        if (selected == null) {
            throw new RuntimeException("No B2 vocabulary found");
        }

        used.add(selected.getGerman().toLowerCase());

        return selected;
    }

    @GetMapping("/puzzle")
    public Map<String, String> randomPuzzle(HttpSession session) {

        Set<String> used = getUsedWords(session, "puzzle");

        Word selected = repository.findRandomPuzzleWordExcluding(
                new ArrayList<>(used)
        );

        // Start a fresh cycle when all eligible words have been shown.
        if (selected == null) {
            used.clear();

            selected = repository.findRandomPuzzleWordExcluding(
                    List.of()
            );
        }

        if (selected == null) {
            throw new RuntimeException("No puzzle words found");
        }

        used.add(selected.getGerman().toLowerCase());

        return Map.of("german", selected.getGerman());
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