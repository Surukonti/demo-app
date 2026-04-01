package com.example.demo.controller;

import com.example.demo.model.DataRequest;
import com.example.demo.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private DataRepository repository;

    @PostMapping("/save")
    public DataRequest saveData(@RequestBody DataRequest request) {
        DataRequest saved = repository.save(request);
        System.out.println("Saved to DB: " + saved.getId());
        return saved;
    }
}
