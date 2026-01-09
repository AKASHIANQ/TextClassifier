package com.example.textclassifier.controller;

import com.example.textclassifier.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClassificationController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/classify")  //single end point to classify a string input
    public ResponseEntity<?> classifyText(@RequestBody Map<String, String> request) {
        String userText = request.get("text");

        if (userText == null || userText.isEmpty()) {
            return ResponseEntity.badRequest().body("Please provide text to classify.");
        }

        Map<String, Object> result = geminiService.classifyWithGemini(userText);

        return ResponseEntity.ok(result);
    }
}

