//package com.example.demo.service;
//
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.translate.TranslateClient;
//import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
//
//@Service
//public class DariTranslationService {
//
//    private final TranslateClient translateClient;
//
//    public DariTranslationService() {
//        this.translateClient = TranslateClient.builder()
//                .region(Region.EU_CENTRAL_1)
//                .build();
//    }
//
//    public String translateGermanToDari(String german) {
//
//        if (german == null || german.isBlank()) {
//            return null;
//        }
//
//        try {
//            TranslateTextRequest request = TranslateTextRequest.builder()
//                    .sourceLanguageCode("de")
//                    .targetLanguageCode("fa-AF")
//                    .text(german)
//                    .build();
//
//            return translateClient.translateText(request).translatedText();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}