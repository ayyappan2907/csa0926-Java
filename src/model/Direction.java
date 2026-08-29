package model;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public int[] getDelta() {
        switch (this) {
            case NORTH: return new int[]{-1, 0};
            case SOUTH: return new int[]{1, 0};
            case EAST:  return new int[]{0, 1};
            case WEST:  return new int[]{0, -1};
            default:    return new int[]{0, 0};
        }
    }

    public String getArrow() {
        switch (this) {
            case NORTH: return "↑";
            case SOUTH: return "↓";
            case EAST:  return "→";
            case WEST:  return "←";
            default:    return "→";
        }
    }

    public Direction opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST:  return WEST;
            case WEST:  return EAST;
            default:    return EAST;
        }
    }
}
