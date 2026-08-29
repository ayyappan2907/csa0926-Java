package simulation;

import model.*;
import recipe.*;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Module 2 — Resource Flow Simulation.
 * Executes a single game tick:
 *   Phase 1 — EXTRACTORs produce raw resources.
 *   Phase 2 — PROCESSORs transform inputs to outputs (recipe-based).
 *   Phase 3 — COMBINERs / ADVANCED_COMBINERs merge inputs into output.
 *   Phase 4 — ALL machines push their output buffer:
 *             - If neighbour is another machine/belt: transfers into neighbour's buffer.
 *             - If neighbour is STORAGE_SILO or EXPORT_TERMINAL: deposited & sold.
 *             - If neighbour is EMPTY space or off-grid: completed product is delivered directly to Warehouse!
 */
public class ResourceFlowManager {

    @FunctionalInterface
    public interface DeliveryCallback {
        void onDelivery(int row, int col, Direction dir, Resource res);
    }

    private static final int EXTRACTOR_INTERVAL = 2; // ticks between each extraction

    public static void processTick(Grid grid, RecipeBook recipeBook,
                                   BiConsumer<Resource, Integer> productionCallback,
                                   BiConsumer<Resource, Integer> soldCallback) {
        processTick(grid, recipeBook, productionCallback, soldCallback, null);
    }

    public static void processTick(Grid grid, RecipeBook recipeBook,
                                   BiConsumer<Resource, Integer> productionCallback,
                                   BiConsumer<Resource, Integer> soldCallback,
                                   DeliveryCallback deliveryCallback) {
        int rows = grid.getRows();
        int cols = grid.getCols();

        // ── Phase 1: EXTRACTOR generates resources ──────────────────────────
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid.getCell(r, c);
                synchronized (cell) {
                    if (cell.getMachineType() == MachineType.EXTRACTOR) {
                        cell.incrementTickCounter();
                        if (cell.getTickCounter() >= EXTRACTOR_INTERVAL) {
                            cell.resetTickCounter();
                            if (!cell.isBufferFull()) {
                                Resource extracted = cell.getExtractorResource();
                                cell.addToBuffer(extracted);
                                if (productionCallback != null) productionCallback.accept(extracted, 1);
                            }
                        }
                    }
                }
            }
        }

        // ── Phase 2: PROCESSOR transforms one input ──────────────────────────
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid.getCell(r, c);
                synchronized (cell) {
                    if (cell.getMachineType() == MachineType.PROCESSOR) {
                        processWithRecipe(grid, r, c, cell, MachineType.PROCESSOR, recipeBook, productionCallback);
                    }
                }
            }
        }

        // ── Phase 3: COMBINER merges two inputs ──────────────────────────────
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid.getCell(r, c);
                synchronized (cell) {
                    if (cell.getMachineType() == MachineType.COMBINER) {
                        processWithRecipe(grid, r, c, cell, MachineType.COMBINER, recipeBook, productionCallback);
                    } else if (cell.getMachineType() == MachineType.ADVANCED_COMBINER) {
                        processWithRecipe(grid, r, c, cell, MachineType.ADVANCED_COMBINER, recipeBook, productionCallback);
                    }
                }
            }
        }

        // ── Phase 4: Push Buffer → Neighbour or EMPTY SPACE (Warehouse) ─────
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid.getCell(r, c);
                if (cell.getMachineType() == MachineType.EMPTY) continue;

                synchronized (cell) {
                    if (cell.isBufferEmpty()) continue;
                    Direction dir = cell.getOutputDirection();
                    Cell neighbour = grid.getAdjacentCell(r, c, dir);

                    // If pointing to EMPTY space or off-grid: complete product moves directly to Warehouse
                    if (neighbour == null || neighbour.getMachineType() == MachineType.EMPTY) {
                        Resource res = cell.pollFromBuffer();
                        if (res != null) {
                            if (soldCallback != null) {
                                soldCallback.accept(res, 1);
                            }
                            if (deliveryCallback != null) {
                                deliveryCallback.onDelivery(r, c, dir, res);
                            }
                        }
                    } else {
                        synchronized (neighbour) {
                            if (neighbour.getMachineType() == MachineType.STORAGE_SILO || 
                                neighbour.getMachineType() == MachineType.EXPORT_TERMINAL) {
                                Resource res = cell.pollFromBuffer();
                                if (res != null) {
                                    if (soldCallback != null) {
                                        soldCallback.accept(res, 1);
                                    }
                                    if (deliveryCallback != null) {
                                        deliveryCallback.onDelivery(r, c, dir, res);
                                    }
                                }
                            } else if (!neighbour.isBufferFull()) {
                                Resource res = cell.pollFromBuffer();
                                if (res != null) neighbour.addToBuffer(res);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Shared recipe-execution logic for PROCESSOR and COMBINER. */
    private static void processWithRecipe(Grid grid, int row, int col, Cell cell, MachineType machineType,
                                          RecipeBook recipeBook,
                                          BiConsumer<Resource, Integer> callback) {
        if (cell.isBufferEmpty()) {
            cell.setProcessingProgress(0);
            return;
        }

        // Build an EnumMap of available resources in the buffer
        EnumMap<Resource, Integer> available = new EnumMap<>(Resource.class);
        for (Resource res : cell.getBufferSnapshot()) {
            available.merge(res, 1, Integer::sum);
        }

        Recipe recipe = recipeBook.findApplicableRecipe(machineType, available);
        if (recipe == null) {
            cell.setProcessingProgress(0);
            return;
        }

        // Compute effective ticks using LayoutBonusEvaluator
        double bonus = pattern.LayoutBonusEvaluator.totalBonus(grid, row, col);
        double effectiveTicks = recipe.getTicksRequired() / (1 + bonus);
        int required = Math.max(1, (int) Math.round(effectiveTicks));

        // Increment and check progress
        int progress = cell.getProcessingProgress() + 1;
        cell.setProcessingProgress(progress);

        if (progress >= required) {
            // Consume all required inputs
            for (Resource input : recipe.getInputs()) {
                cell.removeFromBuffer(input);
            }
            // Produce output
            cell.addToBuffer(recipe.getOutput());
            cell.setProcessingProgress(0);

            if (callback != null) callback.accept(recipe.getOutput(), 1);
        }
    }
}
