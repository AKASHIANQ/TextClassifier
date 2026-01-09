package com.example.textclassifier.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    //gemini api key add the key in application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    // model - gemini 2.5
    @Value("${gemini.url}")
    private String apiUrl;

    // rest template has been used in this project for external api calls to gemini
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> classifyWithGemini(String text) {
        String requestUrl = apiUrl + "?key=" + apiKey;

        // simple prompt to get category and confidence
        String prompt = "Classify the text into one of: Complaint, Query, Feedback, Other. " +
                "Return only raw JSON (no code fences, no markdown): " +
                "{\"category\":\"<one>\",\"confidence\":<0-1>} " +
                "Text: " + text;
        Map<String, Object> generationConfig = Map.of(
    "response_mime_type", "application/json"
);
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(requestUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode partsNode = root.path("candidates").path(0).path("content").path("parts");
            String raw = partsNode.isArray() && partsNode.size() > 0
                    ? partsNode.get(0).path("text").asText("")
                    : "";

            String cleaned = raw.trim();
            if (cleaned.startsWith("```")) {
                int first = cleaned.indexOf("```");
                int last = cleaned.lastIndexOf("```");
                if (last > first) {
                    cleaned = cleaned.substring(first + 3, last).trim();
                }
            }

            if (cleaned.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("category", "Error");
                error.put("message", "Unexpected response from AI");
                return error;
            }

            JsonNode parsed = objectMapper.readTree(cleaned);
            String category = parsed.path("category").asText("").trim();
            double confidence = parsed.path("confidence").asDouble(Double.NaN);

            if (category.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("category", "Error");
                error.put("message", "Model did not return a category");
                return error;
            }

            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("category", category);
            if (!Double.isNaN(confidence)) {
                finalResult.put("confidence", confidence);
            }

            return finalResult;

        } catch (HttpStatusCodeException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("category", "Error");
            error.put("status", ex.getStatusCode().value());
            error.put("details", ex.getResponseBodyAsString());
            return error;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("category", "Error");
            error.put("message", e.getMessage());
            return error;
        }
    }
}

