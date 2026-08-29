package controller;

import auth.UserSession;
import db.*;
import model.*;
import pattern.ProductionPattern;
import player.PlayerProfile;
import recipe.*;
import simulation.*;

import javax.swing.SwingUtilities;
import java.util.*;

/**
 * GameController — bridges all three modules.
 *   Module 1: owns the Grid and exposes placement methods for the GUI.
 *   Module 2: owns SimulationEngine; called processTick() on every tick.
 *   Module 3: owns DatabaseManager + ProductionDAO; logs production periodically.
 */
public class GameController {

    // ── Module 1: Grid ──────────────────────────────────────────────────────
    private final Grid grid;

    // ── Module 2: Simulation ────────────────────────────────────────────────
    private final RecipeBook recipeBook;
    private final SimulationEngine simulationEngine;
    private int tickCount;
    private static final int DB_LOG_INTERVAL = 10;

    // In-memory production totals (flushed to DB every DB_LOG_INTERVAL ticks)
    private final Map<Resource, Integer> pendingProduction;   // since last flush
    private final Map<Resource, Integer> sessionTotals;       // entire session

    // ── Module 3: Database ──────────────────────────────────────────────────
    private final DatabaseManager dbManager;
    private final ProductionDAO productionDAO;
    private final String sessionId;

    // ── Authenticated user ─────────────────────────────────────────────────
    private final UserSession userSession;

    // ── Player profile (levels + XP) ────────────────────────────────────────
    private final PlayerProfile playerProfile;

    // ── GUI callback ────────────────────────────────────────────────────────
    private Runnable onTickCallback;

    // ── Selected cell (for right-click edit) ────────────────────────────────
    private int selectedRow = -1;
    private int selectedCol = -1;

    // ── Economy & Multiplayer ───────────────────────────────────────────────
    private final economy.MachineShop shop;
    private final Map<String, economy.StorageSilo> silos = new HashMap<>();
    private final List<multiplayer.GhostFrame> matchFrames = new ArrayList<>();
    private boolean matchRunning = false;
    private long matchStartTick = 0;
    private int matchFootprintTiles = 0;
    private int matchSoldItems = 0;
    private int sessionSoldTotal = 0;
    private ResourceFlowManager.DeliveryCallback deliveryCallback;

    public GameController(UserSession userSession) {
        this.userSession = userSession;

        // Module 1
        this.grid = new Grid(12, 16);

        // Module 2
        this.recipeBook = RecipeBook.getInstance();
        this.tickCount = 0;
        this.pendingProduction = new EnumMap<>(Resource.class);
        this.sessionTotals    = new EnumMap<>(Resource.class);
        this.simulationEngine = new SimulationEngine(this);

        // Module 3 — session ID is prefixed with the user's email for isolation
        String userPrefix = (userSession != null && userSession.getEmail() != null)
            ? userSession.getEmail().replace("@", "_at_").replace(".", "_") + "__"
            : "guest__";
        this.sessionId   = userPrefix + UUID.randomUUID().toString().substring(0, 8);
        this.dbManager   = new DatabaseManager();
        this.productionDAO = new ProductionDAO(dbManager);
        this.productionDAO.initSession(sessionId);

        // Player profile
        this.playerProfile = new PlayerProfile();
        this.shop = new economy.MachineShop();

        // Default central warehouse storage for products moved to warehouse
        this.silos.put("warehouse", new economy.StorageSilo());
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    /** Called by TickTask on the simulation thread. */
    public void processTick() {
        tickCount++;

        ResourceFlowManager.processTick(grid, recipeBook, (resource, qty) -> {
            pendingProduction.merge(resource, qty, Integer::sum);
            sessionTotals.merge(resource, qty, Integer::sum);
            // Award XP for each item produced
            for (int i = 0; i < qty; i++) playerProfile.awardXP(resource);
        }, (resource, qty) -> {
            long val = economy.ItemValue.cashValue(resource) * qty;
            playerProfile.getWallet().credit(val);
            matchSoldItems += qty;
            sessionSoldTotal += qty;
            // update silos
            boolean deposited = false;
            for (economy.StorageSilo silo : silos.values()) {
                if (!silo.isFull(resource)) {
                    silo.deposit(resource, qty);
                    deposited = true;
                    break;
                }
            }
            if (!deposited) {
                economy.StorageSilo mainSilo = silos.get("warehouse");
                if (mainSilo != null) mainSilo.deposit(resource, qty);
            }
        }, (r, c, dir, res) -> {
            if (deliveryCallback != null) {
                deliveryCallback.onDelivery(r, c, dir, res);
            }
        });

        if (matchRunning && tickCount % 10 == 0) {
            matchFrames.add(new multiplayer.GhostFrame(
                (int)(tickCount - matchStartTick), 
                playerProfile.getWallet().getBalance(), 
                matchSoldItems, 
                getLiveEfficiency()
            ));
        }

        // Apply level-9 speed bonus (once) — reduces tick interval by 10%
        if (playerProfile.consumeSpeedBonus()) {
            int current = simulationEngine.getTickSpeedMs();
            simulationEngine.setTickSpeedMs((int) (current * 0.9));
        }

        if (tickCount % DB_LOG_INTERVAL == 0) {
            flushToDB();
        }

        if (onTickCallback != null) {
            SwingUtilities.invokeLater(onTickCallback);
        }
    }

    private void flushToDB() {
        for (Map.Entry<Resource, Integer> e : pendingProduction.entrySet()) {
            if (e.getValue() > 0) {
                productionDAO.logProduction(sessionId, e.getKey().getDisplayName(), e.getValue());
            }
        }
        pendingProduction.clear();

        int total = sessionTotals.values().stream().mapToInt(Integer::intValue).sum();
        productionDAO.updateSessionSummary(sessionId, total, getLiveEfficiency());
    }

    // ── Grid / Machine Placement ────────────────────────────────────────────

    public boolean placeMachine(int row, int col, MachineType type) {
        if (!shop.purchase(playerProfile, type)) {
            return false;
        }
        boolean placed = grid.placeMachine(row, col, type);
        if (placed) {
            if (type != MachineType.BELT && type != MachineType.CONVEYOR) matchFootprintTiles++;
            if (type == MachineType.STORAGE_SILO) {
                silos.put(row + "," + col, new economy.StorageSilo());
            }
        }
        return placed;
    }

    public boolean removeMachine(int row, int col) {
        Cell c = grid.getCell(row, col);
        if (c != null && c.getMachineType() == MachineType.STORAGE_SILO) {
            silos.remove(row + "," + col);
        }
        return grid.removeMachine(row, col);
    }

    public void setOutputDirection(int row, int col, Direction dir) {
        Cell c = grid.getCell(row, col);
        if (c != null) c.setOutputDirection(dir);
    }

    public void setExtractorResource(int row, int col, Resource res) {
        Cell c = grid.getCell(row, col);
        if (c != null && c.getMachineType() == MachineType.EXTRACTOR)
            c.setExtractorResource(res);
    }

    public void clearGrid() { grid.clear(); }

    // ── Simulation control ──────────────────────────────────────────────────

    public void startSimulation()  { simulationEngine.start(); }
    public void stopSimulation()   { simulationEngine.stop(); }
    public boolean isRunning()     { return simulationEngine.isRunning(); }
    public void setTickSpeed(int ms) { simulationEngine.setTickSpeedMs(ms); }
    public int getTickSpeedMs()    { return simulationEngine.getTickSpeedMs(); }

    // ── Demo chain ──────────────────────────────────────────────────────────

    /**
     * Loads a pre-built demo chain covering all 4 machine types:
     * Iron Ore Extractor → Belt → Processor(Iron Plate) → Belt → Combiner(Steel) → Belt → output
     * Coal Extractor → Belt → Belt → Belt(N) ─→ Combiner
     */
    public void loadDemoChain() {
        grid.clear();

        // Row 2: main iron ore chain
        place(2, 1, MachineType.EXTRACTOR, Direction.EAST, Resource.IRON_ORE);
        place(2, 2, MachineType.BELT,      Direction.EAST, null);
        place(2, 3, MachineType.PROCESSOR, Direction.EAST, null);   // → Iron Plate
        place(2, 4, MachineType.BELT,      Direction.EAST, null);
        place(2, 5, MachineType.COMBINER,  Direction.EAST, null);   // + Coal → Steel
        place(2, 6, MachineType.BELT,      Direction.EAST, null);
        place(2, 7, MachineType.PROCESSOR, Direction.EAST, null);   // Iron Plate → Gear

        // Coal feeder for combiner (from below)
        place(4, 1, MachineType.EXTRACTOR, Direction.EAST, Resource.COAL);
        place(4, 2, MachineType.BELT,      Direction.EAST, null);
        place(4, 3, MachineType.BELT,      Direction.EAST, null);
        place(4, 4, MachineType.BELT,      Direction.EAST, null);
        place(4, 5, MachineType.BELT,      Direction.NORTH, null);  // push NORTH → combiner (2,5)
        place(3, 5, MachineType.BELT,      Direction.NORTH, null);  // intermediate
    }

    /**
     * Loads a production pattern blueprint onto the grid.
     * Clears the grid first, then stamps each placement from the pattern.
     */
    public void loadPattern(ProductionPattern pattern) {
        grid.clear();
        for (ProductionPattern.PlacementEntry ent : pattern.getPlacements()) {
            place(ent.row, ent.col, ent.machineType, ent.outputDirection, ent.extractorResource);
        }
    }

    private void place(int row, int col, MachineType type, Direction dir, Resource extractRes) {
        grid.placeMachine(row, col, type);
        Cell c = grid.getCell(row, col);
        if (c == null) return;
        c.setOutputDirection(dir);
        if (type == MachineType.EXTRACTOR && extractRes != null)
            c.setExtractorResource(extractRes);
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    /**
     * Live factory-wide efficiency (0-100): the share of production
     * machines — Extractors, Processors, Combiners — that are currently
     * ACTIVE rather than starved for input or blocked on output. Safe to
     * call every tick; it's a cheap scan of a 12×16 grid.
     */
    public double getLiveEfficiency() {
        return EfficiencyAnalyzer.factoryEfficiency(grid, recipeBook);
    }

    public Grid getGrid()                  { return grid; }
    public RecipeBook getRecipeBook()      { return recipeBook; }
    public Map<Resource, Integer> getSessionTotals() { return Collections.unmodifiableMap(sessionTotals); }
    public ProductionDAO getProductionDAO() { return productionDAO; }
    public String getSessionId()           { return sessionId; }
    public int getTickCount()              { return tickCount; }
    public PlayerProfile getPlayerProfile() { return playerProfile; }
    public UserSession getUserSession()    { return userSession; }
    public void setOnTickCallback(Runnable r) { this.onTickCallback = r; }

    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }
    public void setSelected(int row, int col) { selectedRow = row; selectedCol = col; }
    public void clearSelection() { selectedRow = -1; selectedCol = -1; }
    
    public int getSessionSoldTotal() { return sessionSoldTotal; }
    public void setDeliveryCallback(ResourceFlowManager.DeliveryCallback cb) { this.deliveryCallback = cb; }
    
    public player.Wallet getWallet() { return playerProfile.getWallet(); }
    public economy.MachineShop getShop() { return shop; }
    public Map<String, economy.StorageSilo> getSilos() { return Collections.unmodifiableMap(silos); }
    public economy.StorageSilo getSiloAt(int row, int col) { return silos.get(row + "," + col); }
    public List<multiplayer.GhostFrame> getMatchFrames() { return Collections.unmodifiableList(matchFrames); }
    public boolean isMatchRunning() { return matchRunning; }
    
    public void saveBlueprintFromGrid(String name) {
        List<pattern.PlacementEntry> placements = new ArrayList<>();
        int rows = grid.getRows(), cols = grid.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid.getCell(r, c);
                if (cell != null && cell.getMachineType() != MachineType.EMPTY) {
                    placements.add(new pattern.PlacementEntry(r, c, cell.getMachineType(), cell.getOutputDirection(), cell.getExtractorResource()));
                }
            }
        }
        pattern.SavedBlueprint blueprint = pattern.SavedBlueprint.createBuiltIn(name, placements);
        dbManager.insertBlueprint(blueprint);
    }
    
    public void startMatch(String seed) {
        matchRunning = true;
        matchStartTick = tickCount;
        matchFrames.clear();
        matchFootprintTiles = 0;
        matchSoldItems = 0;
    }
    
    public multiplayer.PlayerMatchResult endMatch() {
        matchRunning = false;
        double avgEff = matchFrames.isEmpty() ? 0 : matchFrames.stream().mapToDouble(multiplayer.GhostFrame::efficiencyPct).average().orElse(0);
        return new multiplayer.PlayerMatchResult(
            userSession != null ? userSession.getEmail() : "Guest",
            "match-" + System.currentTimeMillis(),
            new ArrayList<>(matchFrames),
            avgEff,
            matchFootprintTiles,
            playerProfile.getWallet().getBalance(),
            0 // Will be calculated by ScoreCalculator
        );
    }

    public void shutdown() {
        simulationEngine.stop();
        flushToDB();
        dbManager.close();
    }
}
