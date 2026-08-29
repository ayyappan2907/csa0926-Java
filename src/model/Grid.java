package model;

public class Grid {
    private final int rows;
    private final int cols;
    private final Cell[][] cells;

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c] = new Cell();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public Cell getCell(int row, int col) {
        if (isValid(row, col)) return cells[row][col];
        return null;
    }

    public boolean placeMachine(int row, int col, MachineType type) {
        if (!isValid(row, col)) return false;
        if (cells[row][col].getMachineType() != MachineType.EMPTY) return false;
        cells[row][col].setMachineType(type);
        return true;
    }

    public boolean removeMachine(int row, int col) {
        if (!isValid(row, col)) return false;
        if (cells[row][col].getMachineType() == MachineType.EMPTY) return false;
        cells[row][col].setMachineType(MachineType.EMPTY);
        return true;
    }

    public Cell getAdjacentCell(int row, int col, Direction dir) {
        int[] delta = dir.getDelta();
        int nr = row + delta[0];
        int nc = col + delta[1];
        return getCell(nr, nc);
    }

    public int[] getAdjacentCoords(int row, int col, Direction dir) {
        int[] delta = dir.getDelta();
        return new int[]{row + delta[0], col + delta[1]};
    }

    public boolean isValid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public void clear() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c].setMachineType(MachineType.EMPTY);
    }
}
