package com.dailybot.task;

import com.dailybot.service.NewsService;
import com.dailybot.service.QqBotService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DailyNewsTask {
    private static final Logger logger = LoggerFactory.getLogger(DailyNewsTask.class);
    private final NewsService newsService;
    private final QqBotService qqBotService;

    private static final long TARGET_GROUP = 1034311904L; // 替换为正确的群号

    public DailyNewsTask(NewsService newsService, QqBotService qqBotService) {
        this.newsService = newsService;
        this.qqBotService = qqBotService;
    }

    @XxlJob("dailyNewsJobHandler")
    public void execute() {
        logger.info("Starting dailyNewsJobHandler execution");
        try {
            String news = newsService.generateDailyNews();
            String history = newsService.generateHistoryToday();

            String finalContent = "🗞️ 每日晨报\n" + news + "\n\n🕰️ 历史回顾\n" + history;
            System.out.println("生成内容:\n" + finalContent);

            String plainTextContent = convertMarkdownToPlainText(finalContent);
            logger.info("Generated plain text content: {}", plainTextContent);

            qqBotService.sendGroupMessage(TARGET_GROUP, plainTextContent); // 使用 plainTextContent
            logger.info("Message sent to group {}", TARGET_GROUP);
            XxlJobHelper.handleSuccess("消息发送成功");
        } catch (Exception e) {
            logger.error("Task execution failed", e);
            System.err.println("任务执行失败: " + e.getMessage());
            XxlJobHelper.handleFail("任务执行失败: " + e.getMessage());
        }
    }

    private String convertMarkdownToPlainText(String markdown) {
        return markdown
                .replaceAll("#+\\s+", "") // 移除标题
                .replaceAll("\\*\\*([^\\*]+)\\*\\*", "$1") // 移除粗体
                .replaceAll("\\*([^\\*]+)\\*", "$1") // 移除斜体
                .replaceAll("-\\s+", "") // 移除列表符号
                .replaceAll("```[a-z]*\\n", "") // 移除代码块开始
                .replaceAll("```\\n", "") // 移除代码块结束
                .replaceAll("\\n{2,}", "\n\n"); // 保留段落换行
    }
}