package pattern;

import model.*;
import recipe.Recipe;
import recipe.RecipeBook;
import recipe.Resource;

import java.util.EnumMap;

public class HeatSharingRule implements AdjacencyRule {

    @Override
    public double evaluate(Grid grid, int row, int col) {
        Cell cell = grid.getCell(row, col);
        if (cell == null || cell.getMachineType() != MachineType.PROCESSOR) return 0.0;
        
        RecipeBook book = RecipeBook.getInstance();
        EnumMap<Resource, Integer> available = new EnumMap<>(Resource.class);
        for (Resource r : cell.getBufferSnapshot()) {
            available.merge(r, 1, Integer::sum);
        }
        Recipe recipe = book.findApplicableRecipe(MachineType.PROCESSOR, available);
        if (recipe == null) return 0.0;
        Resource output = recipe.getOutput();
        
        double bonus = 0.0;
        for (Direction dir : Direction.values()) {
            Cell neighbor = grid.getAdjacentCell(row, col, dir);
            if (neighbor != null && neighbor.getMachineType() == MachineType.PROCESSOR) {
                EnumMap<Resource, Integer> nAvailable = new EnumMap<>(Resource.class);
                for (Resource r : neighbor.getBufferSnapshot()) {
                    nAvailable.merge(r, 1, Integer::sum);
                }
                Recipe nRecipe = book.findApplicableRecipe(MachineType.PROCESSOR, nAvailable);
                if (nRecipe != null && nRecipe.getOutput() == output) {
                    bonus += 0.05;
                }
            }
        }
        
        return Math.min(bonus, 0.15);
    }

    @Override
    public String describe() {
        return "Heat Sharing: +5% per adjacent same-type Processor (max +15%)";
    }
}
