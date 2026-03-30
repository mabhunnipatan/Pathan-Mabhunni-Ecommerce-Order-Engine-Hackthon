package service;

import util.TimeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingService {
    private final List<String> logs = new ArrayList<>();

    public void log(String message) {
        String entry = String.format("[%s] %s", TimeUtil.now(), message);
        logs.add(entry);
        System.out.println("  📝 LOG: " + entry);
    }

    public void logAction(String userId, String action, String detail) {
        log(String.format("%s %s %s", userId, action, detail));
    }

    public void logOrder(String orderId, String event) {
        log(String.format("%s %s", orderId, event));
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public void printAllLogs() {
        if (logs.isEmpty()) {
            System.out.println("  No logs recorded.");
            return;
        }
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      AUDIT LOGS                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        logs.forEach(l -> System.out.println("  " + l));
        System.out.println("  Total: " + logs.size() + " log entries.");
    }
}
