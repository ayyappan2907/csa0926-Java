package pattern;

import model.Grid;

/**
 * §3.2 — AdjacencyRule interface.
 * Each implementation returns a speed multiplier bonus (0.0–0.75) for a given cell.
 */
public interface AdjacencyRule {
    /**
     * Evaluates this rule for the cell at (row, col) and returns a speed bonus in [0, 1].
     * Returns 0 if this rule does not apply.
     */
    double evaluate(Grid grid, int row, int col);

    /** Short description shown as a tooltip badge on the cell. */
    String describe();
}
