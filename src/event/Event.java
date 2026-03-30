package event;

import util.TimeUtil;

public class Event {
    private EventType type;
    private String orderId;
    private String userId;
    private String payload;
    private long timestamp;
    private boolean processed;
    private boolean failed;

    public Event(EventType type, String orderId, String userId, String payload) {
        this.type = type;
        this.orderId = orderId;
        this.userId = userId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
        this.processed = false;
        this.failed = false;
    }

    public EventType getType() { return type; }
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getPayload() { return payload; }
    public long getTimestamp() { return timestamp; }
    public boolean isProcessed() { return processed; }
    public boolean isFailed() { return failed; }

    public void markProcessed() { this.processed = true; }
    public void markFailed() { this.failed = true; }

    @Override
    public String toString() {
        String status = failed ? "FAILED" : (processed ? "PROCESSED" : "PENDING");
        return String.format("[%s] EVENT: %-20s | Order: %-12s | User: %-8s | %s | %s",
                TimeUtil.format(timestamp), type, orderId, userId, payload, status);
    }
}
