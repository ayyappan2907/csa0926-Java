package recipe;

import java.util.*;

public class Recipe {
    private final String name;
    private final List<Resource> inputs;
    private final Resource output;
    private final int ticksRequired;

    public Recipe(String name, List<Resource> inputs, Resource output, int ticksRequired) {
        this.name = name;
        this.inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
        this.output = output;
        this.ticksRequired = ticksRequired;
    }

    public String getName() { return name; }
    public List<Resource> getInputs() { return inputs; }
    public Resource getOutput() { return output; }
    public int getTicksRequired() { return ticksRequired; }

    /**
     * Checks whether 'available' contains enough resources to satisfy this recipe.
     */
    public boolean canApply(EnumMap<Resource, Integer> available) {
        EnumMap<Resource, Integer> needed = new EnumMap<>(Resource.class);
        for (Resource r : inputs) {
            needed.merge(r, 1, Integer::sum);
        }
        for (Map.Entry<Resource, Integer> e : needed.entrySet()) {
            if (available.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name + ": " + inputs + " → " + output + " (" + ticksRequired + " ticks)";
    }
}
