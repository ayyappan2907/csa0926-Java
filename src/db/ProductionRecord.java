package db;

public class ProductionRecord {
    private int id;
    private final String sessionId;
    private final String resource;
    private final int quantity;
    private final String timestamp;

    public ProductionRecord(String sessionId, String resource, int quantity, String timestamp) {
        this.sessionId = sessionId;
        this.resource = resource;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public String getResource() { return resource; }
    public int getQuantity() { return quantity; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + resource + " x" + quantity + " (session: " + sessionId.substring(0,8) + ")";
    }
}
