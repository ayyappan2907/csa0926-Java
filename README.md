# ⚙️ Factory Auto Chain — Automation Simulator

A grid-based factory automation and economic simulation game built in **Java (Swing + Multithreading + JDBC SQLite)** inspired by *Factorio*. 

Players design automated production chains where raw materials are extracted, transported via conveyor belts, processed through multi-tier recipes, and delivered to the central warehouse to earn cash and XP.

---

## 🚀 Key Features

* **60 FPS Vector Canvas (`GridPanel`):** Micro-animations including flowing conveyor chevrons, machine status auras, particle pulses, and flying warehouse delivery bubbles with live `+$Value` popups.
* **4-Phase Multithreaded Simulation (`ResourceFlowManager`):** Runs on a background daemon thread using `ScheduledExecutorService` (Extract → Process → Combine → Push/Export).
* **Dual Progression & Economy (`PlayerProfile`, `Wallet`, `MachineShop`):**
  * **XP:** Earned upon production completion to level up and unlock advanced technology.
  * **Cash ($):** Earned when products reach the Warehouse or Storage Terminals.
  * **Dynamic Price Scaling:** Machine costs scale as more units are placed.
* **Layout Synergies & Adjacency Bonuses (`LayoutBonusEvaluator`):**
  * *Direct Feed:* +15% speed for directly touching producers/consumers.
  * *Heat Sharing:* +5% to +15% speed boost when stacking identical processors.
  * *Compact Loop:* +10% efficiency for tight footprint bounding boxes.
* **Live Storage & Warehouse Dashboard (`WarehousePanel`):** Custom progress bars, rolling throughput rates (`+$N/s`), and per-product inventory racks.
* **Persistence & Analytics (`ProductionDAO`, `DatabaseManager`):** Automatic batch logging of production metrics and session history to SQLite via JDBC.
* **Blueprint System & Ghost Runs:** Reusable blueprint capture/placement and competitive timed challenge scoring.

---

## 🏛️ Project Architecture

```
FactoryAutoChain/
├── src/
│   ├── auth/         # Google OAuth & local user session loopback
│   ├── controller/   # Central GameController orchestrator
│   ├── db/           # SQLite connection & ProductionDAO batch operations
│   ├── economy/      # Wallet, ItemValue, MachineShop, StorageSilo
│   ├── gui/          # Glassmorphic UI, GridPanel canvas, SidePanel, StatsPanel
│   ├── main/         # Main application entry point
│   ├── model/        # Grid, Cell, MachineType, Direction models
│   ├── multiplayer/  # Ghost runs, MatchSession, ScoreCalculator
│   ├── pattern/      # Adjacency rules, Blueprint serializer
│   ├── player/       # PlayerProfile, LevelConfig, XP progression
│   ├── recipe/       # Resource enums, RecipeBook registry
│   └── simulation/   # ResourceFlowManager tick engine, EfficiencyAnalyzer
├── lib/              # sqlite-jdbc.jar, slf4j-api.jar, slf4j-nop.jar
├── resources/        # Custom typography & assets
├── launch.bat        # Quick-launch script
└── run.bat           # Compile and launch script
```

---

## 🧪 Recipe & Processing Graph

```
Iron Ore ($2) ──[Smelt - 3t]──> Iron Plate ($5) ──[Machine - 3t]──> Gear ($10)
                                      │                                 │
Coal ($1) ──────[Alloy - 4t]──────────┴──> Steel ($13)                  │
                                              │                         │
                                              └──[Assemble - 5t]────────┴──> Circuit ($29)
                                                     │                            │
                                                     └──[Electronics - 6t]────────┴──> Computer ($79)
```

---

## 🛠️ How to Compile & Run

### Prerequisites
* Java Development Kit (JDK 17 or higher)

### Run via Batch Scripts (Windows)
* Double-click **`run.bat`** (compiles and launches) or **`launch.bat`** (launches directly).

### Run from Command Line
```bat
# 1. Compile
javac -cp "lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-nop.jar" -d out -sourcepath src src/main/Main.java

# 2. Run
java -cp "out;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-nop.jar" main.Main
```

---

## 🎮 Controls & Shortcuts

| Key / Action | Function |
|---|---|
| **Left Click** | Place / configure selected machine |
| **Left Drag** | Rapidly draw continuous conveyor belts |
| **Right Click** | Demolish / remove machine from grid |
| **R** | Rotate placement direction (North / South / East / West) |
| **Space** | Pause / Resume simulation engine |
| **1 – 8** | Quick-select machine type |
| **Build / Warehouse** | Toggle between machine placement tools and warehouse metrics |
