package com.dailybot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NewsService {
    private final HttpService httpService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    public NewsService(HttpService httpService) {
        this.httpService = httpService;
    }

    public String generateDailyNews() {
        LocalDateTime today = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0);
        LocalDateTime yesterday = today.minusDays(1);
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        String yesterdayStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        String newsPrompt = String.format("""
        作为专业新闻编辑，请生成今日新闻摘要(今天范围%s 到 %s），要求：
        1. 包含5条国内外重要新闻
        2. 每条新闻包含时间、地点、核心内容
        3. 使用Markdown格式
        4. 最后添加一句正能量评论""",yesterdayStr, todayStr);
        String requestBody = buildRequestJson(newsPrompt);
        return parseResponse(httpService.postJsonRequest(API_URL, apiKey, requestBody));
    }

    public String generateHistoryToday() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM月dd日"));
        String historyPrompt = String.format("""
        生成"历史上的今天"内容(今天是%s)，要求：
        1. 包含3个不同年份的事件
        2. 每个事件包含年份、事件描述、历史影响
        3. 使用列表格式""", today);
        String requestBody = buildRequestJson(historyPrompt);
        return parseResponse(httpService.postJsonRequest(API_URL, apiKey, requestBody));
    }

    private String buildRequestJson(String prompt) {
        // 清理提示词中的控制字符并转义
        String cleanedPrompt = prompt
                .replace("\r", "")  // 移除回车符
                .replace("\n", "\\n")  // 将换行符转义为 \n
                .replace("\"", "\\\"");  // 转义双引号

        return String.format(
                "{\"model\": \"deepseek-chat\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"temperature\": 0.7}",
                cleanedPrompt
        );
    }

    private String parseResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("choices").get(0)
                    .path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("解析API响应失败", e);
        }
    }
}