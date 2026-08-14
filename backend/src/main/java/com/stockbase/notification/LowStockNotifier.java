package com.stockbase.notification;

import com.stockbase.model.Product;
import com.stockbase.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watches inventory for products that fall to or below their reorder threshold
 * and pushes an alert to an external system via {@link NotificationSender}.
 *
 * <p>This is an integration between an internal system (the inventory database)
 * and an external one (a webhook / chat channel). It runs on a schedule, and
 * can also be triggered on demand. Products are de-duplicated so a product that
 * stays low isn't re-alerted every run; if it recovers above threshold and later
 * drops again, it will alert again.
 */
@Service
public class LowStockNotifier {

    private static final Logger log = LoggerFactory.getLogger(LowStockNotifier.class);

    private final ProductRepository productRepository;
    private final NotificationSender sender;
    private final Set<Long> alreadyNotified = ConcurrentHashMap.newKeySet();

    public LowStockNotifier(ProductRepository productRepository, NotificationSender sender) {
        this.productRepository = productRepository;
        this.sender = sender;
    }

    /**
     * Check current low-stock products and notify for any that are newly low
     * since the last check. Returns the products that were notified this run.
     */
    public List<Product> checkAndNotify() {
        List<Product> low = productRepository.findLowStockProducts();
        Set<Long> currentLowIds = new java.util.HashSet<>();
        for (Product p : low) {
            currentLowIds.add(p.getId());
        }
        // Forget products that have recovered, so they can alert again if they drop later.
        alreadyNotified.retainAll(currentLowIds);

        List<Product> newlyLow = low.stream()
                .filter(p -> alreadyNotified.add(p.getId())) // add() is true only if not already present
                .toList();

        for (Product p : newlyLow) {
            sender.send(
                    "Low stock: " + p.getName(),
                    String.format("SKU %s is at %d units (reorder threshold %d).",
                            p.getSku(), p.getQuantity(), p.getReorderThreshold()));
        }
        if (!newlyLow.isEmpty()) {
            log.info("[notifications] alerted on {} newly low-stock product(s)", newlyLow.size());
        }
        return newlyLow;
    }

    /** Scheduled sweep (default: top of every hour; override with app.notifications.cron). */
    @Scheduled(cron = "${app.notifications.cron:0 0 * * * *}")
    public void scheduledCheck() {
        checkAndNotify();
    }
}
