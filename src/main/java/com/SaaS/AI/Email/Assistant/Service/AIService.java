package com.SaaS.AI.Email.Assistant.Service;

import com.SaaS.AI.Email.Assistant.dto.MessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.Set;


@Service
public class AIService {

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
            key = "#messageRequest.content + '-' + #messageRequest.tone"
    )


    public String sendMessage(MessageRequest messageRequest) {
        String prompt = buildPrompt(messageRequest);
        String requestBody = String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }""", prompt);

        String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.valueOf("application/json"))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return extractResponseContent(response);
    }


    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.size() == 0) {
                throw new RuntimeException("No candidates found in AI response");
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            if (contentNode.isMissingNode()) {
                throw new RuntimeException("Content node missing in AI response");
            }

            JsonNode parts = contentNode.path("parts");
            if (!parts.isArray() || parts.size() == 0) {
                throw new RuntimeException("No parts found in AI response");
            }

            return parts.get(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }


    private String buildPrompt(MessageRequest messageRequest) {
        StringBuilder prompt = new StringBuilder();

        String tone = sanitizeTone(messageRequest.getTone());
        prompt.append("Respond in a ").append(tone).append(" tone.\n");

        prompt.append("""
                          You are an AI that generates high-quality email replies.
                
                Guidelines:
                1. Generate a complete email reply with Subject and Body.
                2. The tone MUST strictly follow the user-specified tone: <TONE>.
                3. Adapt wording, sentence structure, and style based on tone:
                   - Formal: professional, respectful, no contractions (e.g., "I am", "We would like").
                   - Casual: friendly, conversational, may use contractions (e.g., "I'm", "Thanks!").
                   - Apologetic: express regret clearly and politely.
                   - Assertive: confident, direct, but still respectful.
                   - Friendly: warm, positive, engaging.
                4. Do NOT mix tones. Stick to one consistent tone throughout.
                5. Do NOT copy phrases directly from the original email unless necessary.
                6. Keep sentences clear, natural, and concise.
                7. Ensure proper email format:
                   - Greeting
                   - Body (well-structured paragraphs)
                   - Closing line
                
                Output format:
                
                Generated Response:
                
                Subject: <clear and relevant subject line>
                
                Body:
                <full email reply with proper formatting>
                """);

        return prompt.toString();
    }

    private String sanitizeTone(String tone) {
        if (tone != null && ALLOWED_TONES.contains(tone.toLowerCase())) {
            return tone.toLowerCase();
        }
        return "professional";
    }
}