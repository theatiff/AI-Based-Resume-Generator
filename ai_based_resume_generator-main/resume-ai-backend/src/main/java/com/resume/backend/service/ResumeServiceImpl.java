package com.resume.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    private ChatClient chatClient;

    @Value("${spring.ai.mock:false}")
    private boolean aiMock;

    public ResumeServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public   Map<String, Object> generateResumeResponse(String userResumeDescription) throws IOException {

        if (aiMock) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("think", null);
            Map<String, Object> data = new HashMap<>();
            data.put("name", "John Doe");
            data.put("summary", "Experienced Java developer with 3 years of experience building backend services. Generated mock output.");
            data.put("skills", new String[]{"Java", "Spring Boot", "REST APIs"});
            data.put("experience", new String[]{"Company A - Backend Developer (2021-2024)"});
            fallback.put("data", data);
            return fallback;
        }

        String promptString = this.loadPromptFromFile("resume_prompt.txt");
        String promptContent = this.putValuesToTemplate(promptString, Map.of(
                "userDescription", userResumeDescription
        ));
            // If the local Ollama endpoint is not available, return a helpful fallback immediately
            if (!isLocalAiAvailable("localhost", 11434, 300)) {
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("think", null);
                Map<String, Object> data = new HashMap<>();
                data.put("name", "(unable to generate - AI unavailable)");
                data.put("summary", "AI backend not available; showing fallback based on your input: " + userResumeDescription);
                fallback.put("data", data);
                return fallback;
            }

            Prompt prompt = new Prompt(promptContent);
        String response;
        try {
                response = CompletableFuture.supplyAsync(() -> chatClient.prompt(prompt).call().content())
                    .get(30, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            System.err.println("AI client timed out: " + te.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("think", null);
            Map<String, Object> data = new HashMap<>();
            data.put("name", "(generation timed out)");
            data.put("summary", "AI generation timed out; showing fallback based on your input: " + userResumeDescription);
            fallback.put("data", data);
            return fallback;
        } catch (Exception e) {
            System.err.println("AI client failed: " + e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("think", null);
            Map<String, Object> data = new HashMap<>();
            data.put("name", "(unable to generate)");
            data.put("summary", "AI generation failed; showing fallback based on your input: " + userResumeDescription);
            fallback.put("data", data);
            return fallback;
        }

        Map<String, Object> stringObjectMap = parseMultipleResponses(response);
        return stringObjectMap;
    }


    String loadPromptFromFile(String filename) throws IOException {
        Path path = new ClassPathResource(filename).getFile().toPath();
        return Files.readString(path);
    }

    String putValuesToTemplate(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {

            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());

        }
        return template;
    }


    public static Map<String, Object> parseMultipleResponses(String response) {
        Map<String, Object> jsonResponse = new HashMap<>();

        // Extract content inside <think> tags
        int thinkStart = response.indexOf("<think>") + 7;
        int thinkEnd = response.indexOf("</think>");
        if (thinkStart != -1 && thinkEnd != -1) {
            String thinkContent = response.substring(thinkStart, thinkEnd).trim();
            jsonResponse.put("think", thinkContent);
        } else {
            jsonResponse.put("think", null); // Handle missing <think> tags
        }

        // Extract content that is in JSON format
        int jsonStart = response.indexOf("```json") + 7; // Start after ```json
        int jsonEnd = response.lastIndexOf("```");       // End before ```
        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
            String jsonContent = response.substring(jsonStart, jsonEnd).trim();
            try {
                // Convert JSON string to Map using Jackson ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> dataContent = objectMapper.readValue(jsonContent, Map.class);
                jsonResponse.put("data", dataContent);
            } catch (Exception e) {
                jsonResponse.put("data", null); // Handle invalid JSON
                System.err.println("Invalid JSON format in the response: " + e.getMessage());
            }
        } else {
            jsonResponse.put("data", null); // Handle missing JSON
        }

        return jsonResponse;
    }

    private boolean isLocalAiAvailable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}