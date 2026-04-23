package com.example.demo.repository;

import com.example.demo.model.Word;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WordRepository extends MongoRepository<Word, String> {
    @Aggregation(pipeline = { "{ $sample: { size: 1 } }" })
    Word findRandomWord();
}