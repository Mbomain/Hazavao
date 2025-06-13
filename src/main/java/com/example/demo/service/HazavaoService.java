package com.example.demo.service;

import com.example.demo.model.HazavaoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class HazavaoService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HazavaoResponse getDefinition(String teny) {
        if (teny == null || teny.trim().isEmpty()) {
            return new HazavaoResponse(teny, "Tsy misy teny.");
        }
        try {
            String requestBody = String.format("""
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [
                        {"role": "system", "content": "Mpampianatra teny malagasy ianao. Hazavao fohy sy mazava ny teny rehetra apetraka aminao amin'ny teny malagasy."},
                        {"role": "user", "content": "Hazavao ity teny malagasy ity: %s"}
                    ],
                    "max_tokens": 150,
                    "temperature": 0.7
                }
                """, teny.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            var response = responseEntity.getBody();
            JsonNode json = objectMapper.readTree(response);

            if (json.has("error")) {
                String errorMessage = json.path("error").path("message").asText();
                return new HazavaoResponse(teny, "Tsy tafita: " + errorMessage);
            }

            var dikanteny = json.path("choices").get(0).path("message").path("content").asText();
            return new HazavaoResponse(teny.trim(), dikanteny.trim());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new HazavaoResponse(teny, "Tsy mety ny clé API. Jereo ny fandrindrana.");
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return new HazavaoResponse(teny, "Tafahoatra ny fangatahana. Mandrasa kely.");
            } else {
                return new HazavaoResponse(teny, "Tsy tafita: " + e.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}