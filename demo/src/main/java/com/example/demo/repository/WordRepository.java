package com.example.demo.repository;

import com.example.demo.model.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WordRepository extends MongoRepository<Word, String> {
    @Aggregation(pipeline = { "{ $sample: { size: 1 } }" })
    Word findRandomWord();

    List<Word> findByGerman(String german);

    List<Word> findByLevel(String level);

    List<Word> findByGermanContainingIgnoreCase(String german);

    Page<Word> findByLevel(String level, Pageable pageable);
}