package pattern;

import model.Direction;
import model.MachineType;
import recipe.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Library of built-in production chain blueprints.
 *
 * Each pattern is inspired by Factorio's production-ratio philosophy:
 *   - Smelting Line:    2 Extractors → 2 Processors  (balanced ore-to-plate)
 *   - Gear Shop:        1 Extractor  → 1 Processor    (iron plate → gears)
 *   - Steel Mill:       2-lane iron + coal → Combiner  (1:1 ratio)
 *   - Circuit Factory:  Iron + Gear dual-feed → Combiner (circuit line)
 *   - Full Chain:       Complete end-to-end blueprint from ore to circuits
 */
public class PatternLibrary {

    private static final List<ProductionPattern> PATTERNS;

    static {
        List<ProductionPattern> list = new ArrayList<>();
        list.add(buildSmeltingLine());
        list.add(buildGearShop());
        list.add(buildSteelMill());
        list.add(buildCircuitFactory());
        list.add(buildFullChain());
        PATTERNS = Collections.unmodifiableList(list);
    }

    /** Returns all available patterns. */
    public static List<ProductionPattern> getAll() {
        return PATTERNS;
    }

    /** Returns patterns available for a given player level. */
    public static List<ProductionPattern> getForLevel(int level) {
        List<ProductionPattern> result = new ArrayList<>();
        for (ProductionPattern p : PATTERNS) {
            if (p.getMinLevel() <= level) result.add(p);
        }
        return result;
    }

    // ── Pattern builders ─────────────────────────────────────────────────────

    /**
     * SMELTING LINE — Level 3+
     * 2× Iron Ore Extractors → 2× Belts → 2× Processors → output belts
     *
     * Row 1:  [EXT:IronOre]→[Belt]→[Belt]→[Processor]→[Belt]
     * Row 3:  [EXT:IronOre]→[Belt]→[Belt]→[Processor]→[Belt]
     *
     * Throughput: ~2 Iron Plates / 3 ticks
     */
    private static ProductionPattern buildSmeltingLine() {
        List<ProductionPattern.PlacementEntry> p = new ArrayList<>();
        // Row 1 — upper smelting line
        p.add(e(1,1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(1,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(1,3, MachineType.BELT,      Direction.EAST, null));
        p.add(e(1,4, MachineType.PROCESSOR, Direction.EAST, null));
        p.add(e(1,5, MachineType.BELT,      Direction.EAST, null));
        // Row 3 — lower smelting line
        p.add(e(3,1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(3,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(3,3, MachineType.BELT,      Direction.EAST, null));
        p.add(e(3,4, MachineType.PROCESSOR, Direction.EAST, null));
        p.add(e(3,5, MachineType.BELT,      Direction.EAST, null));

        return new ProductionPattern("Smelting Line",
            "2× Iron Ore Extractors feed 2 Processors in parallel.\n" +
            "Throughput: ~2 Iron Plates every 3 ticks.\n" +
            "Requires: Level 3 (Tinkerer)", 3, p);
    }

    /**
     * GEAR SHOP — Level 4+
     * Iron Ore → Smelt → Iron Plate → Gear
     *
     * Row 2: [EXT:IronOre]→[Belt]→[Processor:Smelt]→[Belt]→[Processor:Gear]→[Belt]
     */
    private static ProductionPattern buildGearShop() {
        List<ProductionPattern.PlacementEntry> p = new ArrayList<>();
        p.add(e(2,1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(2,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,3, MachineType.PROCESSOR, Direction.EAST, null));  // smelt
        p.add(e(2,4, MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,5, MachineType.PROCESSOR, Direction.EAST, null));  // gear
        p.add(e(2,6, MachineType.BELT,      Direction.EAST, null));

        return new ProductionPattern("Gear Shop",
            "Iron Ore → Smelt (Iron Plate) → Gear Production.\n" +
            "Simple 2-stage chain producing Gears from raw ore.\n" +
            "Requires: Level 4 (Fabricator)", 4, p);
    }

    /**
     * STEEL MILL — Level 5+
     * Iron lane + Coal lane → Combiner → Steel
     *
     * Row 2: [EXT:Iron]→[Belt]→[Belt]→[Belt]→[Combiner]→[Belt]
     * Row 4: [EXT:Coal]→[Belt]→[Belt]→[Belt]↑→ (feeds row 2 Combiner via North)
     * Row 3: [Belt:North] (bridge)
     */
    private static ProductionPattern buildSteelMill() {
        List<ProductionPattern.PlacementEntry> p = new ArrayList<>();
        // Iron lane
        p.add(e(2,1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(2,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,3, MachineType.PROCESSOR, Direction.EAST, null));  // smelt → iron plate
        p.add(e(2,4, MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,5, MachineType.COMBINER,  Direction.EAST, null));  // iron plate + coal → steel
        p.add(e(2,6, MachineType.BELT,      Direction.EAST, null));
        // Coal lane + bridge
        p.add(e(4,1, MachineType.EXTRACTOR, Direction.EAST, Resource.COAL));
        p.add(e(4,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,3, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,4, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,5, MachineType.BELT,      Direction.NORTH, null)); // turn north
        p.add(e(3,5, MachineType.BELT,      Direction.NORTH, null)); // bridge to combiner

        return new ProductionPattern("Steel Mill",
            "Iron Ore (smelted) + Coal merge in a Combiner to produce Steel.\n" +
            "Dual-lane feeding with a coal bridge (inspired by Factorio 1:1 ratio).\n" +
            "Requires: Level 5 (Engineer)", 5, p);
    }

    /**
     * CIRCUIT FACTORY — Level 6+
     * Iron Plate + Gear → Circuit via Combiner
     *
     * Row 1: [EXT:Iron]→[Belt]→[Processor:Smelt]→[Belt]→[Belt]→[Belt]→[Combiner]→[Belt]
     * Row 3: [EXT:Iron]→[Belt]→[Processor:Smelt]→[Belt]→[Processor:Gear]→[Belt]↑ (feeds Combiner)
     * Row 2: [Belt:North] bridge
     */
    private static ProductionPattern buildCircuitFactory() {
        List<ProductionPattern.PlacementEntry> p = new ArrayList<>();
        // Iron plate lane → feeds combiner directly
        p.add(e(1,1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(1,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(1,3, MachineType.PROCESSOR, Direction.EAST, null));   // → iron plate
        p.add(e(1,4, MachineType.BELT,      Direction.EAST, null));
        p.add(e(1,5, MachineType.BELT,      Direction.EAST, null));
        p.add(e(1,6, MachineType.BELT,      Direction.EAST, null));
        p.add(e(1,7, MachineType.COMBINER,  Direction.EAST, null));   // iron plate + gear → circuit
        p.add(e(1,8, MachineType.BELT,      Direction.EAST, null));
        // Gear production lane → feeds combiner from south
        p.add(e(3,1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(3,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(3,3, MachineType.PROCESSOR, Direction.EAST, null));   // → iron plate
        p.add(e(3,4, MachineType.BELT,      Direction.EAST, null));
        p.add(e(3,5, MachineType.PROCESSOR, Direction.EAST, null));   // → gear
        p.add(e(3,6, MachineType.BELT,      Direction.EAST, null));
        p.add(e(3,7, MachineType.BELT,      Direction.NORTH, null));  // turn north
        p.add(e(2,7, MachineType.BELT,      Direction.NORTH, null));  // bridge

        return new ProductionPattern("Circuit Factory",
            "Dual-lane: Iron Plate + Gear feed a Combiner to produce Circuits.\n" +
            "Two parallel smelting lines with a gear upgrade stage.\n" +
            "Requires: Level 6 (Senior Engineer)", 6, p);
    }

    /**
     * FULL CHAIN — Level 8+
     * Complete pipeline: Ore → Smelt → Gear + Steel + Circuit
     *
     * Rows 1-5 cover the entire production tree compactly.
     */
    private static ProductionPattern buildFullChain() {
        List<ProductionPattern.PlacementEntry> p = new ArrayList<>();

        // ── Iron main line (Row 2) ──────────────────────────────────────────
        p.add(e(2,1,  MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE));
        p.add(e(2,2,  MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,3,  MachineType.PROCESSOR, Direction.EAST, null));  // → iron plate
        p.add(e(2,4,  MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,5,  MachineType.COMBINER,  Direction.EAST, null));  // iron plate+coal → steel
        p.add(e(2,6,  MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,7,  MachineType.PROCESSOR, Direction.EAST, null));  // iron plate → gear
        p.add(e(2,8,  MachineType.BELT,      Direction.EAST, null));
        p.add(e(2,9,  MachineType.COMBINER,  Direction.EAST, null));  // iron plate+gear → circuit
        p.add(e(2,10, MachineType.BELT,      Direction.EAST, null));

        // ── Coal feeder for steel combiner (Row 4 → Row 3 bridge) ──────────
        p.add(e(4,1, MachineType.EXTRACTOR, Direction.EAST, Resource.COAL));
        p.add(e(4,2, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,3, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,4, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,5, MachineType.BELT,      Direction.NORTH, null));
        p.add(e(3,5, MachineType.BELT,      Direction.NORTH, null));

        // ── Gear feed for circuit combiner (Row 4 continued → Row 3 bridge) ─
        p.add(e(4,6, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,7, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,8, MachineType.BELT,      Direction.EAST, null));
        p.add(e(4,9, MachineType.BELT,      Direction.NORTH, null));
        p.add(e(3,9, MachineType.BELT,      Direction.NORTH, null));

        return new ProductionPattern("Full Chain",
            "Complete end-to-end factory: Ore → Iron Plate → Steel + Gears → Circuits.\n" +
            "Covers all machine types in an optimised compact layout.\n" +
            "Requires: Level 8 (Architect)", 8, p);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static ProductionPattern.PlacementEntry e(int row, int col,
            MachineType type, Direction dir, Resource res) {
        return new ProductionPattern.PlacementEntry(row, col, type, dir, res);
    }
}
