# TextClassifier

A minimal Spring Boot API that classifies user text using Google's Gemini API.

## Environment setup
- Requirements: Java 17+, Maven, Internet access.
- Configure your Gemini API key in `src/main/resources/application.properties` (replace the placeholder value of `gemini.api.key`).
- The model endpoint is set to `gemini-2.5-flash` in the same properties file.

## Run the app
```bash
mvn spring-boot:run
# app starts on http://localhost:8080
```

## API example
- Endpoint: `POST http://localhost:8080/api/classify`
- Headers: `Content-Type: application/json`
- Body:
```json
{ "text": "The delivery was two hours late and the food was cold!" }
```
- Sample response:
```json
{ "category": "Complaint", "confidence": 0.92 }
```

## How AI is used
- The service sends your text and a short classification prompt to Gemini via `generateContent`.
- Gemini replies with a JSON object containing `category` (Complaint, Query, Feedback, Other) and a numeric `confidence`.
- The API simply forwards that structured result back to the client.

