package pattern;

import model.*;

public class DirectFeedRule implements AdjacencyRule {
    
    @Override
    public double evaluate(Grid grid, int row, int col) {
        Cell cell = grid.getCell(row, col);
        if (cell == null) return 0.0;
        MachineType type = cell.getMachineType();
        if (type != MachineType.PROCESSOR && type != MachineType.COMBINER && type != MachineType.ADVANCED_COMBINER) {
            return 0.0;
        }
        
        Direction inputDir = cell.getOutputDirection().opposite();
        Cell preceding = grid.getAdjacentCell(row, col, inputDir);
        
        if (preceding != null) {
            MachineType pType = preceding.getMachineType();
            if (pType == MachineType.EXTRACTOR || pType == MachineType.PROCESSOR || pType == MachineType.COMBINER || pType == MachineType.ADVANCED_COMBINER) {
                if (preceding.getOutputDirection() == inputDir.opposite()) {
                    return 0.15;
                }
            }
        }
        return 0.0;
    }

    @Override
    public String describe() {
        return "Direct Feed: +15% speed";
    }
}
