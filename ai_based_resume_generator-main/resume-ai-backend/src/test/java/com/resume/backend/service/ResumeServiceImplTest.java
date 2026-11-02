package com.resume.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {


    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    private ResumeServiceImpl resumeService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        resumeService = new ResumeServiceImpl(chatClientBuilder);
    }

    @Test
    void testGenerateResumeResponseSuccess() throws IOException {
        String userInput = "I am a Java developer with 5 years of experience";
        String mockResponse = "<think>Processing resume data</think>\n```json\n{\"name\": \"John Doe\", \"experience\": \"5 years\"}\n```";

        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);

        // Stub only what ResumeServiceImpl.generateResumeResponse() actually calls.
        // It executes: chatClient.prompt(prompt).call().content()
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(mockResponse);







        Map<String, Object> result = resumeService.generateResumeResponse(userInput);

        assertNotNull(result);
        assertTrue(result.containsKey("think"));
        assertTrue(result.containsKey("data"));
        assertNotNull(result.get("think"));
        assertEquals("Processing resume data", result.get("think"));
    }

    @Test
    void testParseMultipleResponsesWithValidData() {
        String response = "<think>Analysis complete</think>\n```json\n{\"skills\": [\"Java\", \"Spring\"], \"experience\": \"5 years\"}\n```";

        Map<String, Object> result = ResumeServiceImpl.parseMultipleResponses(response);

        assertNotNull(result);
        assertEquals("Analysis complete", result.get("think"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("skills"));
        assertTrue(data.containsKey("experience"));
    }

    @Test
    void testParseMultipleResponsesWithMissingThinkTags() {
        String response = "```json\n{\"name\": \"Test\"}\n```";

        Map<String, Object> result = ResumeServiceImpl.parseMultipleResponses(response);

        assertNotNull(result);
        assertNull(result.get("think"));
        assertNotNull(result.get("data"));
    }

    @Test
    void testParseMultipleResponsesWithMissingJsonTags() {
        String response = "<think>Processing</think>";

        Map<String, Object> result = ResumeServiceImpl.parseMultipleResponses(response);

        assertNotNull(result);
        assertEquals("Processing", result.get("think"));
        assertNull(result.get("data"));
    }

    @Test
    void testParseMultipleResponsesWithInvalidJson() {
        String response = "<think>Processing</think>\n```json\n{invalid json}\n```";

        Map<String, Object> result = ResumeServiceImpl.parseMultipleResponses(response);

        assertNotNull(result);
        assertEquals("Processing", result.get("think"));
        assertNull(result.get("data"));
    }

    @Test
    void testPutValuesToTemplate() {
        resumeService = new ResumeServiceImpl(chatClientBuilder);
        String template = "Hello {{name}}, your experience is {{experience}} years";
        Map<String, String> values = Map.of(
                "name", "John Doe",
                "experience", "5"
        );

        String result = resumeService.putValuesToTemplate(template, values);

        assertEquals("Hello John Doe, your experience is 5 years", result);
    }

    @Test
    void testPutValuesToTemplateWithMultipleOccurrences() {
        resumeService = new ResumeServiceImpl(chatClientBuilder);
        String template = "{{skill}} is a {{skill}} technology";
        Map<String, String> values = Map.of("skill", "Java");

        String result = resumeService.putValuesToTemplate(template, values);

        assertEquals("Java is a Java technology", result);
    }

    @Test
    void testPutValuesToTemplateWithNoValues() {
        resumeService = new ResumeServiceImpl(chatClientBuilder);
        String template = "Hello {{name}}";
        Map<String, String> values = Map.of();

        String result = resumeService.putValuesToTemplate(template, values);

        assertEquals("Hello {{name}}", result);
    }

    @Test
    void testParseMultipleResponsesWithEmptyResponse() {
        String response = "";

        Map<String, Object> result = ResumeServiceImpl.parseMultipleResponses(response);

        assertNotNull(result);
        assertNull(result.get("think"));
        assertNull(result.get("data"));
    }

    @Test
    void testParseMultipleResponsesWithComplexJson() {
        String response = "<think>Deep analysis</think>\n```json\n{\"profile\": {\"name\": \"John\", \"skills\": [\"Java\", \"Spring\"]}, \"years\": 5}\n```";

        Map<String, Object> result = ResumeServiceImpl.parseMultipleResponses(response);

        assertNotNull(result);
        assertEquals("Deep analysis", result.get("think"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("profile"));
        assertEquals(5, data.get("years"));
    }

}

