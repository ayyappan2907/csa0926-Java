package economy;

import recipe.Resource;
import java.util.*;

public class StorageSilo {

    private final Map<Resource, Integer> stock = new EnumMap<>(Resource.class);
    private final Map<Resource, Integer> capacity = new EnumMap<>(Resource.class);
    private final Deque<long[]> recentDeliveries = new ArrayDeque<>(); // {timestamp, qty}
    
    public StorageSilo() {
        for (Resource r : Resource.values()) {
            if (r != Resource.NONE) {
                capacity.put(r, 1000);
            }
        }
    }

    public boolean deposit(Resource r, int qty) {
        int cap = getCapacity(r);
        int cur = getStock(r);
        if (cur + qty > cap) {
            return false;
        }
        stock.put(r, cur + qty);
        recentDeliveries.addLast(new long[]{System.currentTimeMillis(), qty});
        return true;
    }

    public double throughputPerSecond() {
        trimWindow();
        int total = 0;
        for (long[] entry : recentDeliveries) {
            total += entry[1];
        }
        return total / 10.0;
    }

    public int getStock(Resource r) {
        return stock.getOrDefault(r, 0);
    }

    public int getCapacity(Resource r) {
        return capacity.getOrDefault(r, 1000);
    }

    public boolean isFull(Resource r) {
        return getStock(r) >= getCapacity(r);
    }

    public boolean isNearlyFull() {
        for (Map.Entry<Resource, Integer> entry : stock.entrySet()) {
            if (entry.getValue() > getCapacity(entry.getKey()) * 0.9) {
                return true;
            }
        }
        return false;
    }

    public void trimWindow() {
        long cutoff = System.currentTimeMillis() - 10000;
        while (!recentDeliveries.isEmpty() && recentDeliveries.peekFirst()[0] < cutoff) {
            recentDeliveries.pollFirst();
        }
    }

    public int getTotalStock() {
        int total = 0;
        for (int qty : stock.values()) {
            total += qty;
        }
        return total;
    }
}
