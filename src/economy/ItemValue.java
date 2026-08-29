package economy;

import recipe.Resource;

import java.util.EnumMap;
import java.util.Map;

/**
 * §1.3 — Cash value of each resource.
 *
 * Values are pre-computed from the spec's formula and stored as constants
 * to avoid any risk of recursive cycles (e.g. "Refine Coal" COAL→COAL).
 *
 * Formula used: round( inputCost × 1.8 + ticksRequired × 0.5 )
 * Worked values from spec §1.3:
 *   Iron Ore : 2   (raw base)
 *   Coal     : 1   (raw base)
 *   Iron Plate: round(2×1.8 + 3×0.5) = round(3.6+1.5) = 5
 *   Gear     : round(5×1.8 + 3×0.5) = round(9+1.5)    = 10  (floor of 10.5)
 *   Steel    : round((5+1)×1.8 + 4×0.5) = round(10.8+2) = 13
 *   Circuit  : round((5+10)×1.8 + 5×0.5) = round(27+2.5) = 29 (floor of 29.5)
 *   Computer : round((13+29)×1.8 + 6×0.5) = round(75.6+3) = 79
 */
public final class ItemValue {

    private static final Map<Resource, Long> VALUES = new EnumMap<>(Resource.class);

    static {
        VALUES.put(Resource.NONE,       0L);
        VALUES.put(Resource.IRON_ORE,   2L);
        VALUES.put(Resource.COAL,       1L);
        VALUES.put(Resource.IRON_PLATE, 5L);
        VALUES.put(Resource.GEAR,       10L);
        VALUES.put(Resource.STEEL,      13L);
        VALUES.put(Resource.CIRCUIT,    29L);
        VALUES.put(Resource.COMPUTER,   79L);
    }

    private ItemValue() {}

    /** Returns the cash value in $ of one unit of the given resource. Returns 0 for unknown. */
    public static long cashValue(Resource resource) {
        if (resource == null) return 0L;
        return VALUES.getOrDefault(resource, 0L);
    }
}
