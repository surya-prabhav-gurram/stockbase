package com.stockbase.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Sends notifications to an external HTTP webhook (Slack/Teams-compatible JSON
 * payload). The URL is configuration-driven; if it isn't set, sends are a no-op
 * that just log — so the app runs cleanly in dev without any external service.
 */
@Component
public class WebhookNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(WebhookNotificationSender.class);

    private final String webhookUrl;
    private final RestClient restClient = RestClient.create();

    public WebhookNotificationSender(@Value("${app.notifications.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void send(String title, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("[notifications] webhook not configured; would have sent: {} — {}", title, message);
            return;
        }
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", "*" + title + "*\n" + message))
                    .retrieve()
                    .toBodilessEntity();
            log.info("[notifications] sent webhook notification: {}", title);
        } catch (Exception e) {
            // Never let a notification failure break the business operation that triggered it.
            log.warn("[notifications] failed to send webhook notification: {}", e.getMessage());
        }
    }
}
