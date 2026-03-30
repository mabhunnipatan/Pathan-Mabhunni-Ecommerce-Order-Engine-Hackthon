package service;

import event.Event;
import event.EventQueue;
import event.EventType;

import java.util.List;

public class EventService {
    private final EventQueue eventQueue;
    private final LoggingService loggingService;

    public EventService(LoggingService loggingService) {
        this.eventQueue = new EventQueue();
        this.loggingService = loggingService;
    }

    public void publish(EventType type, String orderId, String userId, String payload) {
        Event event = new Event(type, orderId, userId, payload);
        eventQueue.publish(event);
        loggingService.log("EVENT_PUBLISHED " + type + " for " + orderId);
    }

    public void processOrderEvents(String orderId, String userId, double amount) {
        System.out.println("\n  📡 Processing Event Queue for order: " + orderId);

        // Publish all events for this order
        publish(EventType.ORDER_CREATED, orderId, userId, "order created");
        publish(EventType.PAYMENT_SUCCESS, orderId, userId, "payment amount=₹" + amount);
        publish(EventType.INVENTORY_UPDATED, orderId, userId, "stock deducted");

        // Process them in order; failure stops the chain
        eventQueue.processAll(event -> {
            System.out.printf("  🔄 Processing event: %-20s ... ", event.getType());
            boolean result = simulateEventProcessing(event);
            System.out.println(result ? "✅ OK" : "❌ FAILED");
            return result;
        });
    }

    private boolean simulateEventProcessing(Event event) {
        // Simulate slight delay per event
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        return true; // In normal flow, events succeed
    }

    public void printEventHistory() {
        List<Event> history = eventQueue.getHistory();
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │                     EVENT HISTORY                          │");
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        if (history.isEmpty()) {
            System.out.println("  No events recorded.");
            return;
        }
        history.forEach(e -> System.out.println("  " + e));
    }
}
