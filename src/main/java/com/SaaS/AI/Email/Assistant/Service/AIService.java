package com.SaaS.AI.Email.Assistant.Service;

import com.SaaS.AI.Email.Assistant.dto.MessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private final WebClient webClient;

    @Value("${ai.google.genai.api-key}")
    private String apiKey;

    @Value("${ai.google.genai.api.url}")
    private String apiUrl;

    private static final Set<String> ALLOWED_TONES = Set.of(
            "friendly", "formal", "concise", "empathetic", "enthusiastic", "professional"
    );

    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();

    }

    @Cacheable(
            value = "aiResponses",
            key = "#messageRequest.content + '-' + #messageRequest.tone + '-' + #email"
    )


    public String sendMessage(MessageRequest messageRequest, String email) {
        String prompt = buildPrompt(messageRequest);
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(
                        Retry.backoff(5, Duration.ofSeconds(1))
                                .maxBackoff(Duration.ofSeconds(32))
                                .jitter(0.5)
                                .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                                .doBeforeRetry(signal -> logger.warn(
                                        "Rate limited (429). Retrying attempt {}/10 after backoff delay",
                                        signal.totalRetries() + 1
                                ))
                )
                .doOnError(ex -> logger.error("AI service call failed after all retries", ex))
                .block();
        return extractResponseContent(response);
    }


    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RuntimeException("No candidates found in AI response");
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            if (contentNode.isMissingNode()) {
                throw new RuntimeException("Content node missing in AI response");
            }

            JsonNode parts = contentNode.path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("No parts found in AI response");
            }

            return parts.get(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }


    private String buildPrompt(MessageRequest messageRequest) {

        String tone = sanitizeTone(messageRequest.getTone());
        String emailContent = messageRequest.getContent();

        return """
                You are an AI assistant specialized in generating high-quality email replies.
                
                ==============================
                INPUT:
                Original Email:
                """ + emailContent + """
                
                Desired Tone:
                """ + tone + """
                ==============================
                
                INSTRUCTIONS:
                
                1. Carefully read and understand the original email.
                
                2. Generate a reply that directly addresses all key points, questions, or requests.
                
                3. Tone Rules (STRICT):
                   - Use ONLY the specified tone: """ + tone + """
                   - Do NOT mix tones.
                
                   Tone Guidelines:
                   - formal → professional, respectful, no contractions
                   - casual → conversational, relaxed, may use contractions
                   - apologetic → express clear regret and offer resolution
                   - assertive → confident, direct, but polite
                   - friendly → warm and engaging
                   - professional → neutral, business-appropriate tone
                
                4. Writing Rules:
                   - Keep it concise but complete
                   - Do NOT copy sentences from the original email
                   - Use natural, human-like language
                   - Avoid unnecessary filler
                
                5. Email Structure (MANDATORY):
                   - Subject line
                   - Greeting
                   - Body (clear paragraphs)
                   - Closing line
                   - Signature: [Your Name]
                
                6. If details are missing, use placeholders like:
                   [Recipient Name], [Details]
                
                7. DO NOT include any explanation outside the email.
                
                ==============================
                OUTPUT FORMAT (STRICT):
                
                Subject: <clear subject>
                
                Body:
                Dear [Recipient Name],
                
                <Your response>
                
                Best regards,  
                [Your Name]
                ==============================
                """;
    }

    private String sanitizeTone(String tone) {
        if (tone != null && ALLOWED_TONES.contains(tone.toLowerCase())) {
            return tone.toLowerCase();
        }
        return "professional";
    }
}