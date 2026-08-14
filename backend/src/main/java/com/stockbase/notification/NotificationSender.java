package com.stockbase.notification;

/**
 * Abstraction over the outbound channel used to notify an external system.
 * Keeping this behind an interface lets the business logic (LowStockNotifier)
 * be unit-tested with a fake sender, and lets the transport (webhook, email,
 * SMS, …) be swapped without touching that logic.
 */
public interface NotificationSender {
    void send(String title, String message);
}
