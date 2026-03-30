package event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EventQueue {
    private final Queue<Event> queue = new LinkedList<>();
    private final List<Event> history = new ArrayList<>();

    public void publish(Event event) {
        queue.offer(event);
        history.add(event);
    }

    public Event poll() {
        return queue.poll();
    }

    public boolean hasEvents() {
        return !queue.isEmpty();
    }

    public List<Event> getHistory() {
        return new ArrayList<>(history);
    }

    public void processAll(EventProcessor processor) {
        while (hasEvents()) {
            Event event = poll();
            boolean success = processor.process(event);
            if (success) {
                event.markProcessed();
            } else {
                event.markFailed();
                System.out.println("  ⛔ Event processing stopped at: " + event.getType());
                break; // failure stops next events
            }
        }
    }

    @FunctionalInterface
    public interface EventProcessor {
        boolean process(Event event);
    }
}
