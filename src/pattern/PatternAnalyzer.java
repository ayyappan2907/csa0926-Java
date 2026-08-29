package pattern;

import model.Cell;
import model.Grid;
import model.MachineType;
import recipe.RecipeBook;
import recipe.Resource;
import simulation.EfficiencyAnalyzer;
import simulation.ResourceFlowManager;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Headless blueprint analysis.
 *
 * Runs a pattern through the exact same tick engine that drives live
 * gameplay (ResourceFlowManager) on a scratch grid, with no GUI attached,
 * to measure REAL throughput and efficiency rather than guessing from the
 * layout alone. Inspired by the production-ratio calculators the Factorio
 * community builds around blueprints — players shouldn't have to guess
 * whether a blueprint is actually efficient before committing to it.
 */
public final class PatternAnalyzer {

    /** Number of simulated ticks each pattern is run for. */
    public static final int SIM_TICKS = 60;

    private static final Map<ProductionPattern, Result> CACHE = new HashMap<>();

    /** Result of simulating a pattern: what it produced and how efficiently. */
    public static final class Result {
        public final int totalItems;
        public final double efficiencyPct;
        public final Map<Resource, Integer> byResource;

        Result(int totalItems, double efficiencyPct, Map<Resource, Integer> byResource) {
            this.totalItems = totalItems;
            this.efficiencyPct = efficiencyPct;
            this.byResource = byResource;
        }
    }

    private PatternAnalyzer() {}

    /** Simulates the pattern and caches the result (patterns are static/fixed). */
    public static synchronized Result analyze(ProductionPattern pattern) {
        Result cached = CACHE.get(pattern);
        if (cached != null) return cached;

        Grid grid = new Grid(pattern.getHeight(), pattern.getWidth());
        for (ProductionPattern.PlacementEntry e : pattern.getPlacements()) {
            grid.placeMachine(e.row, e.col, e.machineType);
            Cell cell = grid.getCell(e.row, e.col);
            if (cell == null) continue;
            cell.setOutputDirection(e.outputDirection);
            if (e.machineType == MachineType.EXTRACTOR && e.extractorResource != null) {
                cell.setExtractorResource(e.extractorResource);
            }
        }

        RecipeBook recipeBook = RecipeBook.getInstance();
        Map<Resource, Integer> totals = new EnumMap<>(Resource.class);
        double effSum = 0;
        int samples = 0;

        for (int t = 0; t < SIM_TICKS; t++) {
            ResourceFlowManager.processTick(grid, recipeBook,
                (res, qty) -> totals.merge(res, qty, Integer::sum),
                null);
            if (t % 5 == 4) {
                effSum += EfficiencyAnalyzer.factoryEfficiency(grid, recipeBook);
                samples++;
            }
        }

        int total = totals.values().stream().mapToInt(Integer::intValue).sum();
        double avgEff = samples > 0 ? effSum / samples : 0.0;

        Result result = new Result(total, avgEff, totals);
        CACHE.put(pattern, result);
        return result;
    }
}
