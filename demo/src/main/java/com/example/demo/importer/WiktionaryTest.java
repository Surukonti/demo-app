package com.example.demo.importer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WiktionaryTest {

    public static void main(String[] args) {

        String url =
                "https://de.wiktionary.org/w/api.php" +
                        "?action=parse" +
                        "&page=Gott" +
                        "&prop=wikitext" +
                        "&format=json";

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "GermanLearningApp/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP status: " + response.statusCode());
            System.out.println(response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}