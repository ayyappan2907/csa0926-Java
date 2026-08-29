package model;

public enum MachineType {
    EMPTY, EXTRACTOR, BELT, PROCESSOR, COMBINER,
    STORAGE_SILO, EXPORT_TERMINAL, ADVANCED_COMBINER, CONVEYOR;

    public String getDisplayName() {
        switch (this) {
            case EXTRACTOR:         return "Extractor";
            case BELT:              return "Belt";
            case PROCESSOR:         return "Processor";
            case COMBINER:          return "Combiner";
            case STORAGE_SILO:      return "Storage Silo";
            case EXPORT_TERMINAL:   return "Export Terminal";
            case ADVANCED_COMBINER: return "Adv. Combiner";
            case CONVEYOR:          return "Conveyor";
            default:                return "Empty";
        }
    }

    public String getIcon() {
        switch (this) {
            case EXTRACTOR:         return "⛏";
            case BELT:              return "➡";
            case PROCESSOR:         return "⚙";
            case COMBINER:          return "🔗";
            case STORAGE_SILO:      return "▦";
            case EXPORT_TERMINAL:   return "📤";
            case ADVANCED_COMBINER: return "🔗";
            case CONVEYOR:          return "⏩";
            default:                return " ";
        }
    }
}
