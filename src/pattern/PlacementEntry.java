package pattern;

import model.MachineType;
import recipe.Resource;

/**
 * A single machine placement within a blueprint footprint.
 * Coordinates are relative (0-based, normalized to top-left of the selection).
 */
public class PlacementEntry {
    public final int         row;
    public final int         col;
    public final MachineType type;
    public final model.Direction outputDirection;
    public final Resource    extractorResource; // null unless type == EXTRACTOR

    public PlacementEntry(int row, int col, MachineType type,
                          model.Direction outputDirection, Resource extractorResource) {
        this.row               = row;
        this.col               = col;
        this.type              = type;
        this.outputDirection   = outputDirection;
        this.extractorResource = extractorResource;
    }

    /** Serialize to a compact JSON-like string for DB storage. */
    public String toJson() {
        String res = (extractorResource != null) ? extractorResource.name() : "null";
        return String.format("{\"r\":%d,\"c\":%d,\"t\":\"%s\",\"d\":\"%s\",\"res\":\"%s\"}",
            row, col, type.name(), outputDirection.name(), res);
    }

    /** Parse a PlacementEntry from the compact JSON produced by toJson(). */
    public static PlacementEntry fromJson(String json) {
        json = json.replaceAll("[{}\"]", "");
        String[] parts = json.split(",");
        int r = 0, c = 0;
        MachineType t = MachineType.EMPTY;
        model.Direction d = model.Direction.EAST;
        Resource res = null;
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length < 2) continue;
            String key = kv[0].trim(), val = kv[1].trim();
            switch (key) {
                case "r":   r = Integer.parseInt(val); break;
                case "c":   c = Integer.parseInt(val); break;
                case "t":   t = MachineType.valueOf(val); break;
                case "d":   d = model.Direction.valueOf(val); break;
                case "res": if (!val.equals("null")) res = Resource.valueOf(val); break;
            }
        }
        return new PlacementEntry(r, c, t, d, res);
    }
}
