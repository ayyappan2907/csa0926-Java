package recipe;

import java.awt.Color;

public enum Resource {
    NONE, IRON_ORE, COAL, IRON_PLATE, STEEL, CIRCUIT, GEAR, COMPUTER;

    public String getDisplayName() {
        switch (this) {
            case IRON_ORE:   return "Iron Ore";
            case COAL:       return "Coal";
            case IRON_PLATE: return "Iron Plate";
            case STEEL:      return "Steel";
            case CIRCUIT:    return "Circuit";
            case GEAR:       return "Gear";
            case COMPUTER:   return "Computer";
            default:         return "None";
        }
    }

    public Color getColor() {
        switch (this) {
            case IRON_ORE:   return new Color(160, 110, 60);
            case COAL:       return new Color(55, 55, 65);
            case IRON_PLATE: return new Color(185, 195, 210);
            case STEEL:      return new Color(100, 149, 237);
            case CIRCUIT:    return new Color(50, 205, 100);
            case GEAR:       return new Color(240, 170, 40);
            case COMPUTER:   return new Color(180, 80, 220);
            default:         return Color.DARK_GRAY;
        }
    }

    public String getEmoji() {
        switch (this) {
            case IRON_ORE:   return "🪨";
            case COAL:       return "🪵";
            case IRON_PLATE: return "🔩";
            case STEEL:      return "🔷";
            case CIRCUIT:    return "💚";
            case GEAR:       return "⚙";
            case COMPUTER:   return "💻";
            default:         return "·";
        }
    }
}
