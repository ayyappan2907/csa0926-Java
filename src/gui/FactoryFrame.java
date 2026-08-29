package gui;

import auth.UserSession;
import controller.GameController;
import model.Direction;
import model.MachineType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Main Application Window — Factory Auto Chain.
 *
 * Features:
 *  - Floating Glassmorphic Top Navbar with glowing logo
 *  - Tactile glass buttons with hover glow and active states
 *  - Glass speed slider with live milliseconds badge
 *  - Global keyboard shortcuts (Space, R, 1-4, Del, P, L, H, D, C, Arrows)
 *  - Compact side panel and seamlessly centered grid canvas
 */
public class FactoryFrame extends JFrame {

    private final GameController controller;
    private final GridPanel  gridPanel;
    private final SidePanel  sidePanel;
    private final StatsPanel statsPanel;
    private final LevelPanel levelPanel;

    private JButton playPauseBtn;
    private JLabel  statusLabel;

    public FactoryFrame(GameController controller) {
        this.controller = controller;

        setTitle("⚙ Factory Auto Chain — Automation Simulator");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBackground(UITheme.BG_VOID);

        // Dark title bar emulation
        getRootPane().putClientProperty("JRootPane.titleBarBackground", UITheme.BG_VOID);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", UITheme.FG_PRIMARY);

        // ── Sub-panels ────────────────────────────────────────────────────────
        sidePanel  = new SidePanel(controller);
        gridPanel  = new GridPanel(controller, sidePanel);
        statsPanel = new StatsPanel(controller);
        levelPanel = new LevelPanel(controller.getPlayerProfile());

        // ── Level-up Listener ─────────────────────────────────────────────────
        controller.getPlayerProfile().addLevelListener((newLevel, levelName) -> {
            levelPanel.triggerLevelUpFlash();
            levelPanel.refresh();
            sidePanel.refreshUnlocks();
            showLevelUpDialog(newLevel, levelName);
        });

        // ── Simulation Tick Callback ──────────────────────────────────────────
        controller.setOnTickCallback(() -> {
            statsPanel.refresh();
            levelPanel.refresh();
            sidePanel.refreshWarehouse();
            int xpGained = controller.getPlayerProfile().drainPendingXPForPopup();
            if (xpGained > 0) levelPanel.showXpGain(xpGained);
            updateStatusLabel();
        });

        // ── Layout ────────────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG_VOID);

        center.add(gridPanel, BorderLayout.CENTER);
        center.add(sidePanel, BorderLayout.EAST);

        add(center,     BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);

        // ── Global Keyboard Shortcuts ─────────────────────────────────────────
        setupKeyboardShortcuts();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(920, 660));

        // ── Window Close ──────────────────────────────────────────────────────
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.shutdown();
                dispose();
                System.exit(0);
            }
        });
    }

    // ── Global Hotkeys ────────────────────────────────────────────────────────

    private void setupKeyboardShortcuts() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        // Space: Play / Pause
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggleSim");
        am.put("toggleSim", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { toggleSimulation(); }
        });

        // R: Rotate Clockwise
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotateDir");
        am.put("rotateDir", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                sidePanel.rotateClockwise();
                gridPanel.repaint();
            }
        });

        // 1-4: Select Tool
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "tool1");
        am.put("tool1", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.selectMachineType(MachineType.EXTRACTOR); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "tool2");
        am.put("tool2", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.selectMachineType(MachineType.BELT); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "tool3");
        am.put("tool3", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.selectMachineType(MachineType.PROCESSOR); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "tool4");
        am.put("tool4", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.selectMachineType(MachineType.COMBINER); }
        });

        // Delete / Backspace / X: Demolish selected
        Action deleteAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int r = controller.getSelectedRow(), c = controller.getSelectedCol();
                if (r >= 0 && c >= 0) {
                    controller.removeMachine(r, c);
                    controller.clearSelection();
                    gridPanel.repaint();
                }
            }
        };
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delMachine");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delMachine");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "delMachine");
        am.put("delMachine", deleteAction);

        // D: Load Demo
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "loadDemo");
        am.put("loadDemo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { loadDemoChain(); }
        });

        // C: Clear Grid
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "clearGrid");
        am.put("clearGrid", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { promptClearGrid(); }
        });

        // P: Pattern Library
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "openPatterns");
        am.put("openPatterns", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (controller.getPlayerProfile().isPatternsUnlocked()) {
                    new PatternDialog(FactoryFrame.this, controller.getPlayerProfile(), pattern -> {
                        controller.loadPattern(pattern);
                        gridPanel.repaint();
                    }).setVisible(true);
                }
            }
        });

        // L: Career Progression
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "openCareer");
        am.put("openCareer", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                new LevelDialog(FactoryFrame.this, controller.getPlayerProfile()).setVisible(true);
            }
        });

        // H: History Stats
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "openStats");
        am.put("openStats", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { showStatsDialog(); }
        });

        // Arrow Keys: Direction
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "dirN");
        am.put("dirN", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.setDirection(Direction.NORTH); gridPanel.repaint(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "dirS");
        am.put("dirS", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.setDirection(Direction.SOUTH); gridPanel.repaint(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "dirW");
        am.put("dirW", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.setDirection(Direction.WEST); gridPanel.repaint(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "dirE");
        am.put("dirE", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sidePanel.setDirection(Direction.EAST); gridPanel.repaint(); }
        });
    }

    // ── Floating Navbar ───────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 0,
                    UITheme.GLASS_BG, UITheme.BORDER_GLASS, true);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 54));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        left.setOpaque(false);

        // Logo
        JLabel logo = new JLabel("⚙  FACTORY AUTO CHAIN");
        logo.setForeground(UITheme.NEON_GOLD);
        logo.setFont(UITheme.font(Font.BOLD, 13));
        left.add(logo);

        left.add(makeToolSep());

        // Play / Pause Glass Button
        playPauseBtn = glassButton("▶  Start", UITheme.NEON_GREEN, 92, e -> toggleSimulation());
        left.add(playPauseBtn);

        // Load Demo
        JButton demoBtn = glassButton("🔗  Demo", UITheme.NEON_BLUE, 82, e -> loadDemoChain());
        left.add(demoBtn);

        // Clear
        JButton clearBtn = glassButton("🗑  Clear", UITheme.NEON_RED, 82, e -> promptClearGrid());
        left.add(clearBtn);

        left.add(makeToolSep());
        
        // Save Blueprint
        JButton saveBpBtn = glassButton("💾  Save BP", UITheme.NEON_GREEN_LIGHT, 92, e -> {
            String name = JOptionPane.showInputDialog(this, "Enter blueprint name:");
            if (name != null && !name.trim().isEmpty()) {
                controller.saveBlueprintFromGrid(name);
                JOptionPane.showMessageDialog(this, "Blueprint saved!");
            }
        });
        left.add(saveBpBtn);
        
        // Start Match
        JButton matchBtn = glassButton("⚔  Match", UITheme.NEON_PURPLE, 82, e -> {
            if (controller.isMatchRunning()) {
                multiplayer.PlayerMatchResult res = controller.endMatch();
                JOptionPane.showMessageDialog(this, "Match Ended!\nItems: " + res.footprintTiles + "\nScore: " + res.score);
            } else {
                controller.startMatch("seed");
                JOptionPane.showMessageDialog(this, "Match Started!");
            }
        });
        left.add(matchBtn);

        left.add(makeToolSep());

        // Speed slider
        JLabel speedIcon = new JLabel("⚡");
        speedIcon.setFont(UITheme.fontEmoji(Font.PLAIN, 12));
        speedIcon.setForeground(UITheme.NEON_GOLD);
        left.add(speedIcon);

        final JLabel speedVal = new JLabel("800ms");
        speedVal.setFont(UITheme.fontMono(Font.BOLD, 10));
        speedVal.setForeground(UITheme.NEON_BLUE_LIGHT);
        speedVal.setPreferredSize(new Dimension(44, 16));

        JSlider speedSlider = new JSlider(200, 2000, 800);
        speedSlider.setInverted(true);
        speedSlider.setOpaque(false);
        speedSlider.setForeground(UITheme.NEON_GOLD);
        speedSlider.setPreferredSize(new Dimension(84, 22));
        speedSlider.setToolTipText("Simulation Speed (lower ms = faster)");
        speedSlider.addChangeListener(e -> {
            speedVal.setText(speedSlider.getValue() + "ms");
            if (!speedSlider.getValueIsAdjusting()) {
                controller.setTickSpeed(speedSlider.getValue());
            }
        });
        left.add(speedSlider);
        left.add(speedVal);

        left.add(makeToolSep());

        // Status Label
        statusLabel = new JLabel("⏸  Stopped");
        statusLabel.setForeground(UITheme.FG_MUTED);
        statusLabel.setFont(UITheme.font(Font.BOLD, 11));
        left.add(statusLabel);

        left.add(makeToolSep());

        // DB Stats Button
        JButton statsBtn = glassButton("📊  History", UITheme.NEON_PURPLE, 90, e -> showStatsDialog());
        left.add(statsBtn);

        bar.add(left, BorderLayout.CENTER);

        // Right side: User Profile Badge & LevelPanel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        rightPanel.setOpaque(false);
        rightPanel.add(buildUserBadge());
        rightPanel.add(levelPanel);
        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }

    private JButton glassButton(String text, Color accent, int width, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                boolean isHover = getModel().isRollover();

                Color fill = isHover
                    ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 65)
                    : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35);

                Color border = isHover
                    ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220)
                    : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 130);

                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 6, fill, border, true);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UITheme.fontEmoji(Font.BOLD, 10));
        btn.setForeground(new Color(
            Math.min(accent.getRed() + 90, 255),
            Math.min(accent.getGreen() + 90, 255),
            Math.min(accent.getBlue() + 90, 255)
        ));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 30));
        btn.addActionListener(action);
        return btn;
    }

    private JSeparator makeToolSep() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 18));
        sep.setForeground(new Color(40, 55, 90));
        return sep;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void toggleSimulation() {
        if (controller.isRunning()) {
            controller.stopSimulation();
            playPauseBtn.setText("▶  Start");
        } else {
            controller.startSimulation();
            playPauseBtn.setText("⏸  Pause");
        }
        updateStatusLabel();
        playPauseBtn.repaint();
    }

    private void updateStatusLabel() {
        String state = controller.isRunning() ? "▶  Running" : "⏸  Paused";
        statusLabel.setText(state + " │ Tick " + controller.getTickCount());
        statusLabel.setForeground(controller.isRunning() ? UITheme.NEON_GREEN_LIGHT : UITheme.FG_MUTED);
    }

    private void loadDemoChain() {
        controller.loadDemoChain();
        gridPanel.repaint();
        JOptionPane.showMessageDialog(this,
            "Demo Factory Chain Loaded!\n\nExtractors → Belts → Processor (Iron Plate) → Combiner (Steel) → Processor (Gears)",
            "Demo Chain Loaded", JOptionPane.INFORMATION_MESSAGE);
    }

    private void promptClearGrid() {
        int ok = JOptionPane.showConfirmDialog(this,
            "Demolish and clear the entire factory grid?", "Clear Grid",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            controller.clearGrid();
            gridPanel.repaint();
        }
    }

    // ── User Profile Badge ────────────────────────────────────────────────────

    private JPanel buildUserBadge() {
        UserSession session = controller.getUserSession();
        String displayName = (session != null && session.getGivenName() != null) ? session.getGivenName() : "Guest";
        char initial = (session != null) ? session.getAvatarInitial() : 'G';
        final char avatarChar = initial;
        final String name = displayName;

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 6,
                    new Color(18, 25, 45, 180), UITheme.BORDER_GLASS, true);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(UITheme.NEON_BLUE);
                g2.fillOval(0, 0, 24, 24);
                g2.setColor(Color.WHITE);
                g2.setFont(UITheme.font(Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String letter = String.valueOf(avatarChar);
                int tx = (24 - fm.stringWidth(letter)) / 2;
                int ty = (24 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, tx, ty);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(24, 24));
        avatar.setOpaque(false);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setForeground(UITheme.FG_PRIMARY);
        nameLbl.setFont(UITheme.font(Font.BOLD, 11));

        JButton signOutBtn = new JButton("🚪");
        signOutBtn.setFont(UITheme.fontEmoji(Font.PLAIN, 11));
        signOutBtn.setOpaque(false);
        signOutBtn.setContentAreaFilled(false);
        signOutBtn.setBorderPainted(false);
        signOutBtn.setFocusPainted(false);
        signOutBtn.setForeground(UITheme.NEON_RED);
        signOutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signOutBtn.setToolTipText("Sign out");
        signOutBtn.addActionListener(e -> signOut());

        badge.add(avatar);
        badge.add(nameLbl);
        badge.add(signOutBtn);
        return badge;
    }

    private void signOut() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Sign out of Factory Auto Chain?\nYour current session progress will be saved.",
            "Sign Out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        controller.shutdown();
        dispose();

        SwingUtilities.invokeLater(() -> {
            LoginScreen login = new LoginScreen(session -> {
                main.Main.launchGamePublic(session);
            });
            login.setVisible(true);
        });
    }

    private void showLevelUpDialog(int newLevel, String levelName) {
        String nextUnlock = "";
        if (newLevel == 2) nextUnlock = "\n🔓 Coal Extraction unlocked!";
        else if (newLevel == 3) nextUnlock = "\n🔓 Processor (Smelting) unlocked!";
        else if (newLevel == 4) nextUnlock = "\n🔓 Processor (Gear) unlocked!";
        else if (newLevel == 5) nextUnlock = "\n🔓 Combiner (Steel) unlocked!";
        else if (newLevel == 6) nextUnlock = "\n🔓 Combiner (Circuit) unlocked!";
        else if (newLevel == 7) nextUnlock = "\n🔓 Pattern Library unlocked! Press 'P' or check side panel.";
        else if (newLevel == 8) nextUnlock = "\n🔓 Full Chain blueprint available!";
        else if (newLevel == 9) nextUnlock = "\n⚡ Speed Bonus applied — 10% faster tick interval!";
        else if (newLevel == 10) nextUnlock = "\n⭐ MASTER AUTOMATOR achieved!";

        JOptionPane.showMessageDialog(this,
            "🎉  LEVEL UP!  🎉\n\nYou are now: Level " + newLevel + " — " + levelName + nextUnlock,
            "Level Up!", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Stats Dialog ──────────────────────────────────────────────────────────

    private void showStatsDialog() {
        JDialog dlg = new JDialog(this, "📊 Production Database History", true);
        dlg.getContentPane().setBackground(UITheme.BG_VOID);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getRootPane().setBorder(new EmptyBorder(12, 12, 12, 12));

        var totals = controller.getProductionDAO().getProductionTotals(controller.getSessionId());
        StringBuilder sb = new StringBuilder("=== Current Session Production ===\n\n");
        if (totals.isEmpty()) {
            sb.append("  No production logged yet.\n  Run the simulation to log data to SQLite!\n");
        } else {
            totals.forEach((res, qty) -> sb.append(String.format("  %-16s : %d items%n", res, qty)));
        }

        sb.append("\n=== Recent Sessions (SQLite Database) ===\n\n");
        var sessions = controller.getProductionDAO().getRecentSessions(10);
        if (sessions.isEmpty()) {
            sb.append("  No past sessions found.\n");
        } else {
            sb.append(String.format("  %-12s %-20s %-20s %-10s %s%n", "Session", "Start", "End", "Produced", "Efficiency"));
            sb.append("  " + "─".repeat(72) + "\n");
            for (String[] row : sessions) {
                sb.append(String.format("  %-12s %-20s %-20s %-10s %s%n",
                    row[0], row[1], row[2], row[3], row[4]));
            }
        }

        sb.append("\nGrand total across all sessions: ")
          .append(controller.getProductionDAO().getGrandTotal()).append(" items");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setBackground(new Color(16, 22, 38));
        area.setForeground(UITheme.NEON_GREEN_LIGHT);
        area.setFont(UITheme.fontMono(Font.PLAIN, 11));
        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane sp = new JScrollPane(area);
        UITheme.styleScrollPane(sp);
        dlg.add(sp, BorderLayout.CENTER);

        JButton close = glassButton("Close", UITheme.NEON_BLUE, 76, e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(close);
        dlg.add(btnRow, BorderLayout.SOUTH);

        dlg.setSize(680, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
