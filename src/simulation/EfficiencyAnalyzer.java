package simulation;

import model.Cell;
import model.Grid;
import model.MachineType;
import recipe.Recipe;
import recipe.RecipeBook;
import recipe.Resource;

import java.util.EnumMap;

/**
 * Module 2 — Efficiency Analyzer.
 *
 * Pure, stateless analysis of machine and factory-wide efficiency.
 * Inspired by Factorio's belt/inserter "starved" and "full" alert icons,
 * and by production-ratio calculators the Factorio community builds
 * (blueprint analysers, ratio calculators) to spot bottlenecks before
 * they cost throughput.
 *
 * Used by:
 *   - GridPanel / MachineRenderer, to draw a per-cell status dot.
 *   - GameController, to compute a live factory-wide efficiency %.
 *   - PatternAnalyzer, to score blueprint patterns before they're loaded.
 */
public final class EfficiencyAnalyzer {

    /** Operating status of a single machine cell. */
    public enum Status {
        EMPTY,    // no machine placed
        ACTIVE,   // currently producing / moving resources
        STARVED,  // waiting on missing inputs
        BLOCKED,  // output buffer full, cannot push downstream
        IDLE      // valid machine, simply has nothing to do right now
    }

    private EfficiencyAnalyzer() {}

    /** Determines the current operating status of a single cell. */
    public static Status statusOf(Cell cell, RecipeBook recipeBook) {
        MachineType type = cell.getMachineType();

        switch (type) {
            case EMPTY:
                return Status.EMPTY;

            case EXTRACTOR:
                return cell.isBufferFull() ? Status.BLOCKED : Status.ACTIVE;

            case BELT:
                if (cell.isBufferFull())  return Status.BLOCKED;
                if (cell.isBufferEmpty()) return Status.IDLE;
                return Status.ACTIVE;

            case PROCESSOR:
            case COMBINER: {
                if (cell.getProcessingProgress() > 0) return Status.ACTIVE;

                EnumMap<Resource, Integer> available = new EnumMap<>(Resource.class);
                for (Resource r : cell.getBufferSnapshot()) available.merge(r, 1, Integer::sum);

                Recipe recipe = recipeBook.findApplicableRecipe(type, available);
                if (recipe != null) return Status.ACTIVE;
                return cell.isBufferFull() ? Status.BLOCKED : Status.STARVED;
            }

            default:
                return Status.EMPTY;
        }
    }

    /**
     * Computes factory-wide efficiency: the percentage of production
     * machines (Extractors, Processors, Combiners — Belts are transport,
     * not production, so they're excluded) that are currently ACTIVE.
     *
     * @return a value in [0, 100], or 0 if no production machines exist.
     */
    public static double factoryEfficiency(Grid grid, RecipeBook recipeBook) {
        int total = 0, active = 0;
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                Cell cell = grid.getCell(r, c);
                MachineType type = cell.getMachineType();
                if (type == MachineType.EMPTY || type == MachineType.BELT) continue;

                total++;
                if (statusOf(cell, recipeBook) == Status.ACTIVE) active++;
            }
        }
        return total == 0 ? 0.0 : (100.0 * active / total);
    }
}
