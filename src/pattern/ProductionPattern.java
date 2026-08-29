package pattern;

import model.Direction;
import model.MachineType;
import recipe.Resource;

import java.util.Collections;
import java.util.List;

/**
 * Immutable blueprint representing a pre-built production chain pattern.
 * Inspired by Factorio blueprint books — each pattern defines an ordered list
 * of machine placements that can be stamped onto the factory grid.
 */
public final class ProductionPattern {

    /** A single machine placement within the pattern. */
    public static final class PlacementEntry {
        public final int         row;
        public final int         col;
        public final MachineType machineType;
        public final Direction   outputDirection;
        public final Resource    extractorResource; // null if not an extractor

        public PlacementEntry(int row, int col, MachineType machineType,
                              Direction outputDirection, Resource extractorResource) {
            this.row              = row;
            this.col              = col;
            this.machineType      = machineType;
            this.outputDirection  = outputDirection;
            this.extractorResource = extractorResource;
        }
    }

    private final String              name;
    private final String              description;
    private final int                 minLevel;    // minimum player level to load this pattern
    private final List<PlacementEntry> placements;

    public ProductionPattern(String name, String description,
                             int minLevel, List<PlacementEntry> placements) {
        this.name        = name;
        this.description = description;
        this.minLevel    = minLevel;
        this.placements  = Collections.unmodifiableList(placements);
    }

    public String getName()                        { return name; }
    public String getDescription()                 { return description; }
    public int    getMinLevel()                    { return minLevel; }
    public List<PlacementEntry> getPlacements()    { return placements; }

    /** Bounding-box width of this pattern (max col + 1). */
    public int getWidth() {
        return placements.stream().mapToInt(p -> p.col).max().orElse(0) + 1;
    }

    /** Bounding-box height of this pattern (max row + 1). */
    public int getHeight() {
        return placements.stream().mapToInt(p -> p.row).max().orElse(0) + 1;
    }
}
