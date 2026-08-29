package player;

import model.MachineType;
import recipe.Resource;

import java.util.*;

/**
 * Static configuration table for all 10 player levels.
 * Each entry defines the XP threshold, display title, flavour text,
 * and which MachineTypes / Resources become available at that level.
 *
 * Unlock progression is inspired by Factorio's tech-tree philosophy:
 *  - Start with raw extraction only.
 *  - Gradually unlock smelting, combining, and advanced resources.
 */
public class LevelConfig {

    public static final int MAX_LEVEL = 10;

    /** Immutable descriptor for a single level. */
    public static final class LevelEntry {
        public final int     level;
        public final String  title;
        public final String  description;
        public final int     xpRequired;        // cumulative XP to REACH this level
        public final Set<MachineType> unlockedMachines;
        public final Set<Resource>    unlockedResources;

        LevelEntry(int level, String title, String description, int xpRequired,
                   MachineType[] machines, Resource[] resources) {
            this.level = level;
            this.title = title;
            this.description = description;
            this.xpRequired = xpRequired;
            this.unlockedMachines  = Collections.unmodifiableSet(
                    new LinkedHashSet<>(Arrays.asList(machines)));
            this.unlockedResources = Collections.unmodifiableSet(
                    new LinkedHashSet<>(Arrays.asList(resources)));
        }
    }

    /** Indexed 0-based (index = level-1). */
    private static final LevelEntry[] ENTRIES = {
        new LevelEntry(1, "Novice",
            "A fresh factory worker with a pickaxe and a dream.",
            0,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT },
            new Resource[]{ Resource.IRON_ORE }),

        new LevelEntry(2, "Apprentice",
            "You discovered coal — fuel for greater things.",
            100,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(3, "Tinkerer",
            "Smelting unlocked! Turn raw ore into iron plates.",
            250,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT, MachineType.PROCESSOR },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(4, "Fabricator",
            "Gear production online. Mechanical parts are key.",
            500,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT, MachineType.PROCESSOR },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(5, "Engineer",
            "The Combiner is yours. Steel production begins!",
            900,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT,
                               MachineType.PROCESSOR,  MachineType.COMBINER },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(6, "Senior Engineer",
            "Circuits! The foundation of all advanced automation.",
            1500,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT,
                               MachineType.PROCESSOR,  MachineType.COMBINER },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(7, "Industrialist",
            "Production blueprints unlocked in the Pattern Library.",
            2300,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT,
                               MachineType.PROCESSOR,  MachineType.COMBINER },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(8, "Architect",
            "Full Chain blueprint available. Build the perfect factory!",
            3400,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT,
                               MachineType.PROCESSOR,  MachineType.COMBINER },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(9, "Automation Expert",
            "Your factory runs at peak efficiency — +10% tick speed bonus.",
            5000,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT,
                               MachineType.PROCESSOR,  MachineType.COMBINER },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),

        new LevelEntry(10, "Master Automator",
            "The apex of factory engineering. All systems nominal.",
            7000,
            new MachineType[]{ MachineType.EXTRACTOR, MachineType.BELT,
                               MachineType.PROCESSOR,  MachineType.COMBINER },
            new Resource[]{ Resource.IRON_ORE, Resource.COAL }),
    };

    /** Returns the LevelEntry for a given level (1–10). */
    public static LevelEntry getEntry(int level) {
        if (level < 1)  level = 1;
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return ENTRIES[level - 1];
    }

    /** Returns the XP required to reach a given level. */
    public static int xpForLevel(int level) {
        return getEntry(level).xpRequired;
    }

    /** Returns the XP required to reach the next level (or Integer.MAX_VALUE at cap). */
    public static int xpForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return Integer.MAX_VALUE;
        return getEntry(currentLevel + 1).xpRequired;
    }

    /** Computes what level a given cumulative XP value corresponds to. */
    public static int levelForXP(int xp) {
        int level = 1;
        for (int i = MAX_LEVEL; i >= 2; i--) {
            if (xp >= ENTRIES[i - 1].xpRequired) {
                level = i;
                break;
            }
        }
        return level;
    }
}
