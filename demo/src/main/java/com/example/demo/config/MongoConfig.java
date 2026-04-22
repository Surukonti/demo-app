//package com.example.demo.config;
//
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class MongoConfig {
//
//    @Value("${SPRING_DATA_MONGODB_URI}")
//    private String uri;
//
//    @Bean
//    public MongoClient mongoClient() {
//        return MongoClients.create(uri);
//    }
//}