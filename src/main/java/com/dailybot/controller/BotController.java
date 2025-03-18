package com.dailybot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {

    @PostMapping("/qqbot/event")
    public String handleEvent(@RequestBody String event) {
        // 处理心跳检测等基础事件
        if (event.contains("\"post_type\":\"meta_event\"")) {
            return "{\"status\": \"ok\"}";
        }
        return "{}";
    }
}