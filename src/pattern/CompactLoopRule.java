package pattern;

import model.*;
import java.util.*;

public class CompactLoopRule implements AdjacencyRule {

    @Override
    public double evaluate(Grid grid, int row, int col) {
        Cell startCell = grid.getCell(row, col);
        if (startCell == null || startCell.getMachineType() == MachineType.EMPTY) return 0.0;

        Set<String> visited = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{row, col});
        visited.add(row + "," + col);

        int minRow = row, maxRow = row, minCol = col, maxCol = col;
        int machineCount = 0;

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int r = pos[0], c = pos[1];
            Cell cell = grid.getCell(r, c);
            if (cell == null || cell.getMachineType() == MachineType.EMPTY) continue;
            
            minRow = Math.min(minRow, r);
            maxRow = Math.max(maxRow, r);
            minCol = Math.min(minCol, c);
            maxCol = Math.max(maxCol, c);
            
            if (cell.getMachineType() != MachineType.BELT && cell.getMachineType() != MachineType.CONVEYOR) {
                machineCount++;
            }

            Direction out = cell.getOutputDirection();
            int[] d = out.getDelta();
            int nr = r + d[0];
            int nc = c + d[1];
            if (grid.isValid(nr, nc) && !visited.contains(nr + "," + nc)) {
                Cell next = grid.getCell(nr, nc);
                if (next != null && next.getMachineType() != MachineType.EMPTY) {
                    visited.add(nr + "," + nc);
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        if (machineCount > 0) {
            int area = (maxRow - minRow + 1) * (maxCol - minCol + 1);
            if ((double) area / machineCount <= 1.5) {
                return 0.10;
            }
        }
        return 0.0;
    }

    @Override
    public String describe() {
        return "Compact Loop: +10% for tight cluster";
    }
}
