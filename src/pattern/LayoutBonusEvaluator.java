package pattern;

import model.Grid;
import java.util.List;

public class LayoutBonusEvaluator {
    
    private static final List<AdjacencyRule> RULES = List.of(
        new DirectFeedRule(),
        new HeatSharingRule(),
        new CompactLoopRule()
    );
    private static final double MAX_STACK = 0.75;

    public static double totalBonus(Grid grid, int row, int col) {
        double total = 0.0;
        for (AdjacencyRule rule : RULES) {
            total += rule.evaluate(grid, row, col);
        }
        return Math.min(total, MAX_STACK);
    }

    public static String activeBonusDescription(Grid grid, int row, int col) {
        StringBuilder sb = new StringBuilder();
        for (AdjacencyRule rule : RULES) {
            double v = rule.evaluate(grid, row, col);
            if (v > 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(rule.describe());
            }
        }
        return sb.toString();
    }
}
