package recipe;

import model.MachineType;
import java.util.*;

public class RecipeBook {

    // PRIMARY collection: machine type → ordered list of recipes (priority order)
    private final Map<MachineType, List<Recipe>> recipeMap;

    // LOOKUP SET: which machine types support recipes
    private final Set<MachineType> processingMachines;

    private static RecipeBook instance;

    private RecipeBook() {
        recipeMap = new HashMap<>();
        processingMachines = new HashSet<>();
        initRecipes();
    }

    public static synchronized RecipeBook getInstance() {
        if (instance == null) instance = new RecipeBook();
        return instance;
    }

    private void initRecipes() {
        List<Recipe> processorRecipes = new ArrayList<>();
        processorRecipes.add(new Recipe(
            "Smelt Iron Plate",
            Arrays.asList(Resource.IRON_ORE),
            Resource.IRON_PLATE, 3));
        processorRecipes.add(new Recipe(
            "Make Gear",
            Arrays.asList(Resource.IRON_PLATE),
            Resource.GEAR, 3));
        processorRecipes.add(new Recipe(
            "Refine Coal",
            Arrays.asList(Resource.COAL),
            Resource.COAL, 1));
        recipeMap.put(MachineType.PROCESSOR, processorRecipes);
        processingMachines.add(MachineType.PROCESSOR);

        // --- COMBINER recipes ---
        List<Recipe> combinerRecipes = new ArrayList<>();
        combinerRecipes.add(new Recipe(
            "Make Steel",
            Arrays.asList(Resource.IRON_PLATE, Resource.COAL),
            Resource.STEEL, 5));
        combinerRecipes.add(new Recipe(
            "Make Circuit",
            Arrays.asList(Resource.IRON_PLATE, Resource.GEAR),
            Resource.CIRCUIT, 4));
        recipeMap.put(MachineType.COMBINER, combinerRecipes);
        processingMachines.add(MachineType.COMBINER);

        // --- ADVANCED_COMBINER recipes ---
        List<Recipe> advancedCombinerRecipes = new ArrayList<>();
        advancedCombinerRecipes.add(new Recipe(
            "Make Computer",
            Arrays.asList(Resource.STEEL, Resource.CIRCUIT),
            Resource.COMPUTER, 6));
        advancedCombinerRecipes.add(new Recipe(
            "Make Circuit (Adv)",
            Arrays.asList(Resource.GEAR, Resource.CIRCUIT),
            Resource.CIRCUIT, 3));
        recipeMap.put(MachineType.ADVANCED_COMBINER, advancedCombinerRecipes);
        processingMachines.add(MachineType.ADVANCED_COMBINER);
    }

    /** Returns all recipes for a given machine type (unmodifiable). */
    public List<Recipe> getRecipes(MachineType type) {
        return Collections.unmodifiableList(
            recipeMap.getOrDefault(type, Collections.emptyList()));
    }

    /**
     * Searches all recipes and returns the first one whose output is the given resource.
     * Returns null for raw resources or if no recipe produces it.
     */
    public Recipe recipeThatProduces(Resource r) {
        for (List<Recipe> list : recipeMap.values()) {
            for (Recipe recipe : list) {
                if (recipe.getOutput() == r) {
                    return recipe;
                }
            }
        }
        return null;
    }

    /**
     * Finds the first applicable recipe for the given machine type
     * given the available resource counts.
     */
    public Recipe findApplicableRecipe(MachineType type, EnumMap<Resource, Integer> available) {
        List<Recipe> list = recipeMap.getOrDefault(type, Collections.emptyList());
        for (Recipe r : list) {
            if (r.canApply(available)) return r;
        }
        return null;
    }

    /** Returns the set of machine types that have processing recipes. */
    public Set<MachineType> getProcessingMachines() {
        return Collections.unmodifiableSet(processingMachines);
    }

    /** Returns a description of all recipes (for UI display). */
    public List<String> getAllRecipeDescriptions() {
        List<String> desc = new ArrayList<>();
        for (Map.Entry<MachineType, List<Recipe>> e : recipeMap.entrySet()) {
            desc.add("[ " + e.getKey().getDisplayName() + " ]");
            for (Recipe r : e.getValue()) {
                StringBuilder sb = new StringBuilder("  ");
                sb.append(r.getName()).append(": ");
                List<String> ins = new ArrayList<>();
                for (Resource res : r.getInputs()) ins.add(res.getDisplayName());
                sb.append(String.join(" + ", ins));
                sb.append(" → ").append(r.getOutput().getDisplayName());
                sb.append(" (").append(r.getTicksRequired()).append(" ticks)");
                desc.add(sb.toString());
            }
        }
        return desc;
    }
}
