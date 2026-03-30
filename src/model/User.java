package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private boolean flagged;
    private List<Long> orderTimestamps;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.flagged = false;
        this.orderTimestamps = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    public List<Long> getOrderTimestamps() { return orderTimestamps; }

    public void recordOrder() {
        orderTimestamps.add(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.format("User[%s] %s%s", userId, name, flagged ? " ⚠ FLAGGED" : "");
    }
}
