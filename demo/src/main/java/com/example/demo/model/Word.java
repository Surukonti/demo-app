package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "words")
public class Word {

    @Id
    private String id;

    private String german;

    private String english;

    private List<String> englishMeanings;
    private List<String> arabicMeanings;
    private List<String> ukrainianMeanings;

    public List<String> getRussianMeanings() {
        return russianMeanings;
    }

    public void setRussianMeanings(List<String> russianMeanings) {
        this.russianMeanings = russianMeanings;
    }

    public List<String> getArabicMeanings() {
        return arabicMeanings;
    }

    public void setArabicMeanings(List<String> arabicMeanings) {
        this.arabicMeanings = arabicMeanings;
    }

    public List<String> getUkrainianMeanings() {
        return ukrainianMeanings;
    }

    public void setUkrainianMeanings(List<String> ukrainianMeanings) {
        this.ukrainianMeanings = ukrainianMeanings;
    }

    public List<String> getTurkishMeanings() {
        return turkishMeanings;
    }

    public void setTurkishMeanings(List<String> turkishMeanings) {
        this.turkishMeanings = turkishMeanings;
    }

    private List<String> russianMeanings;
    private List<String> turkishMeanings;

    private String arabic;
    private String ukrainian;
    private String russian;
    private String turkish;

    private String level;
    private String article;
    private String plural;
    private String category;

    private String example;
    private String exampleEnglish;

    private List<String> examples;
    private List<String> examplesEnglish;

    private String partOfSpeech;

    // Existing word forms from Wordhoard/Wiktionary
    private String forms;

    private String present;
    private String preterite;
    private String perfect;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getGerman() {
        return german;
    }

    public void setGerman(String german) {
        this.german = german;
    }


    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }


    public List<String> getEnglishMeanings() {
        return englishMeanings;
    }

    public void setEnglishMeanings(List<String> englishMeanings) {
        this.englishMeanings = englishMeanings;
    }


    public String getArabic() {
        return arabic;
    }

    public void setArabic(String arabic) {
        this.arabic = arabic;
    }


    public String getUkrainian() {
        return ukrainian;
    }

    public void setUkrainian(String ukrainian) {
        this.ukrainian = ukrainian;
    }


    public String getRussian() {
        return russian;
    }

    public void setRussian(String russian) {
        this.russian = russian;
    }


    public String getTurkish() {
        return turkish;
    }

    public void setTurkish(String turkish) {
        this.turkish = turkish;
    }


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }


    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }


    public String getPlural() {
        return plural;
    }

    public void setPlural(String plural) {
        this.plural = plural;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }


    public String getExampleEnglish() {
        return exampleEnglish;
    }

    public void setExampleEnglish(String exampleEnglish) {
        this.exampleEnglish = exampleEnglish;
    }


    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }


    public List<String> getExamplesEnglish() {
        return examplesEnglish;
    }

    public void setExamplesEnglish(List<String> examplesEnglish) {
        this.examplesEnglish = examplesEnglish;
    }


    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }


    public String getForms() {
        return forms;
    }

    public void setForms(String forms) {
        this.forms = forms;
    }


    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }


    public String getPreterite() {
        return preterite;
    }

    public void setPreterite(String preterite) {
        this.preterite = preterite;
    }


    public String getPerfect() {
        return perfect;
    }

    public void setPerfect(String perfect) {
        this.perfect = perfect;
    }
}