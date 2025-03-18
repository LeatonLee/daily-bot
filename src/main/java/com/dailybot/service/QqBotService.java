package com.dailybot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class QqBotService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${qqbot.api-url}")
    private String apiUrl;

    @Value("${qqbot.access-token}")
    private String accessToken;

    public void sendGroupMessage(long groupId, String message) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("group_id", groupId);
            requestBody.put("message", message);
            requestBody.put("auto_escape", true); // 修改为 true，确保纯文本发送

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForEntity(apiUrl, request, String.class).getBody();
            System.out.println("Response: " + response); // 调试用
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }
}