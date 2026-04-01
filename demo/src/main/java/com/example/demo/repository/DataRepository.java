package com.example.demo.repository;

import com.example.demo.model.DataRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataRepository extends MongoRepository<DataRequest, String> {
}