package pattern;

import java.time.Instant;
import java.util.*;

public class SavedBlueprint {
    public final String id;
    public final String name;
    public final String author;
    public final int width;
    public final int height;
    public final List<PlacementEntry> placements;
    public final Instant createdAt;
    public final boolean isBuiltIn;
    public final int cachedItems;
    public final double cachedEfficiency;

    public SavedBlueprint(String id, String name, String author, int width, int height,
                          List<PlacementEntry> placements, Instant createdAt, boolean isBuiltIn,
                          int cachedItems, double cachedEfficiency) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.width = width;
        this.height = height;
        this.placements = placements;
        this.createdAt = createdAt;
        this.isBuiltIn = isBuiltIn;
        this.cachedItems = cachedItems;
        this.cachedEfficiency = cachedEfficiency;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < placements.size(); i++) {
            sb.append(placements.get(i).toJson());
            if (i < placements.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<PlacementEntry> fromJson(String json) {
        List<PlacementEntry> list = new ArrayList<>();
        if (json == null || json.isEmpty() || json.equals("[]")) return list;
        
        json = json.substring(1, json.length() - 1); // remove [ ]
        String[] parts = json.split("},\\{");
        for (String p : parts) {
            if (!p.startsWith("{")) p = "{" + p;
            if (!p.endsWith("}")) p = p + "}";
            list.add(PlacementEntry.fromJson(p));
        }
        return list;
    }

    public static SavedBlueprint createBuiltIn(String name, List<PlacementEntry> placements) {
        int maxR = 0, maxC = 0;
        for (PlacementEntry p : placements) {
            if (p.row > maxR) maxR = p.row;
            if (p.col > maxC) maxC = p.col;
        }
        return new SavedBlueprint(UUID.randomUUID().toString(), name, "System", maxR + 1, maxC + 1,
            placements, Instant.now(), true, 0, 0.0);
    }
}
