# What changed in this update

Your project already had a full leveling system and a 5-blueprint Pattern
Library built in — so this pass focused on making both of them actually
work end-to-end, plus adding real efficiency tooling. Verified by actually
running the game in a virtual display and screenshotting it, not just by
reading the code.

## Bugs fixed

1. **Leveling was soft-locked.** XP was only ever awarded when a
   Processor/Combiner recipe completed — raw extraction gave 0 XP. But
   Processor is unlocked at Level 3, which itself needs XP. A player who
   never clicked the "Demo" button had no legitimate way to ever earn XP
   or level up. Fixed in `simulation/ResourceFlowManager.java`: extraction
   now awards XP too (there was already an unused `XP_PER_ITEM_BASIC`
   constant clearly meant for this — it just wasn't wired up).

2. **The level widget in the toolbar was invisible.** `LevelPanel` had
   `setPreferredSize(new Dimension(230, 0))` — zero height — so it took
   up space but rendered nothing. Fixed alongside a related issue where
   the toolbar's fixed 44px height was clipping a second row of controls
   (the status label + History button) that wraps at this width. Toolbar
   is now 80px and both rows show fully.

## New: efficiency tooling (`simulation/EfficiencyAnalyzer.java`)

- Every machine now shows a small status dot: **green** = active,
  **amber** = starved (waiting on inputs), **red** = blocked (output
  backed up). Hover any machine for a plain-English tooltip explaining
  what it's doing.
- The **Eff.** stat in the bottom panel is now a live, real number (%
  of production machines currently active) instead of a rough estimate
  refreshed only every 20 ticks.

## New: pattern stats (`pattern/PatternAnalyzer.java`)

Each blueprint in the Pattern Library now shows **real measured numbers**
— items produced and average uptime — from actually running that pattern
through the simulation engine headlessly for 60 ticks, not just
describing it in prose. You can now genuinely compare which blueprints
are efficient rather than take it on faith.

## New: `gui/LevelDialog.java`

Click the level badge in the toolbar to open a full 10-level career
roadmap — every level's requirement, flavor text, and what it newly
unlocks, with your current level highlighted and auto-scrolled into view.

## New: small interactive touches

- A floating **"+N XP"** popup over the level badge when you earn XP.
- A brief gold **pulse ring** where you place a machine.
- The level badge now has a tooltip previewing what your next level
  unlocks.

## Housekeeping

- Added `.gitignore` — since you linked a GitHub page, worth flagging:
  **`google_credentials.json` is your real OAuth client secret.** It's
  included in this zip since you need it to run the app, but the
  `.gitignore` keeps it (and the local `factory.db`/`crash.log`/`out/`
  build folder) out of version control if you push this to GitHub.
- Removed the stale precompiled `out/` folder — `run.bat` already
  recompiles fresh each time, and shipping old `.class` files risked a
  Java-version mismatch on your machine anyway.

## Notes / things to double check on your machine

- I tested visually on Linux with font fallback (no "Segoe UI" installed
  there), and noticed a few side-panel labels look slightly truncated
  ("PLACE M...", "OUTPU..."). This may be specific to that fallback font
  being wider than Segoe UI — worth a glance on your actual Windows setup,
  but I didn't touch `SidePanel.java`'s sizing since I couldn't verify a
  fix would actually be correct on Windows without risking making it worse.
- The Pattern Library's live uptime numbers are honest, not tuned to look
  good — e.g. Circuit Factory measures ~42% uptime vs. Smelting Line's
  ~75%, reflecting that dual-lane patterns take longer to synchronize.
  That's real simulation output, not a bug.
