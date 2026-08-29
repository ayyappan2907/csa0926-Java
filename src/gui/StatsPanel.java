package gui;

import controller.GameController;
import recipe.Resource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stats Panel — Real-time Production Dashboard & Session Database Stream.
 *
 * Features:
 *  - Fixed typography & dot overlap bug
 *  - High-resolution glass progress tubes with gradient liquid fills
 *  - Live factory efficiency badge & XP display
 *  - Formatted session history table (custom dark glass header, clean time formatting HH:mm:ss)
 *  - 180px height ensuring all 6 resource gauges and session status rows fit comfortably
 */
public class StatsPanel extends JPanel {

    private final GameController controller;

    private final Map<Resource, JLabel>       countLabels = new LinkedHashMap<>();
    private final Map<Resource, JProgressBar> bars        = new LinkedHashMap<>();

    private JLabel tickLabel;
    private JLabel totalLabel;
    private JLabel effLabel;
    private JLabel levelLabel;

    private DefaultTableModel tableModel;

    public StatsPanel(GameController controller) {
        this.controller = controller;
        setBackground(UITheme.BG_VOID);
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_GLASS));
        setPreferredSize(new Dimension(0, 180));

        add(buildSummaryBlock(), BorderLayout.WEST);
        add(buildResourceBars(),  BorderLayout.CENTER);
        add(buildHistoryTable(),  BorderLayout.EAST);
    }

    // ── Session Summary Block ─────────────────────────────────────────────────

    private JPanel buildSummaryBlock() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 0,
                    UITheme.GLASS_BG, UITheme.BORDER_GLASS, true);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.setPreferredSize(new Dimension(220, 0));

        JLabel title = new JLabel("📊  SESSION STATUS");
        title.setFont(UITheme.font(Font.BOLD, 10));
        title.setForeground(UITheme.NEON_GOLD);
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        tickLabel  = statLabel("⏱", "Tick", "0");
        totalLabel = statLabel("📦", "Items", "0");
        effLabel   = statLabel("⚡", "Eff.", "—");
        levelLabel = statLabel("⭐", "Level", "1 · Novice");

        p.add(tickLabel);
        p.add(Box.createVerticalStrut(6));
        p.add(totalLabel);
        p.add(Box.createVerticalStrut(6));
        p.add(effLabel);
        p.add(Box.createVerticalStrut(6));
        p.add(levelLabel);
        
        p.add(Box.createVerticalStrut(10));
        matchHudLabel = new JLabel("No active match");
        matchHudLabel.setFont(UITheme.font(Font.BOLD, 10));
        matchHudLabel.setForeground(UITheme.NEON_PURPLE);
        p.add(matchHudLabel);
        
        walletLabel = new JLabel("$0");
        walletLabel.setFont(UITheme.font(Font.BOLD, 10));
        walletLabel.setForeground(UITheme.NEON_GOLD);
        p.add(walletLabel);

        return p;
    }

    private JLabel matchHudLabel;
    private JLabel walletLabel;

    private JLabel statLabel(String icon, String key, String val) {
        JLabel lbl = new JLabel(icon + "  " + key + ": " + val);
        lbl.setFont(UITheme.font(Font.BOLD, 10));
        lbl.setForeground(UITheme.FG_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    // ── Resource Production Bars ──────────────────────────────────────────────

    private JPanel buildResourceBars() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("⚙  FACTORY OUTPUT GAUGES");
        title.setFont(UITheme.font(Font.BOLD, 10));
        title.setForeground(UITheme.NEON_GOLD);
        outer.add(title, BorderLayout.NORTH);

        // 2 columns × 3 rows
        JPanel grid = new JPanel(new GridLayout(3, 2, 16, 8));
        grid.setOpaque(false);

        Resource[] tracked = {
            Resource.IRON_ORE, Resource.COAL,
            Resource.IRON_PLATE, Resource.GEAR,
            Resource.STEEL, Resource.CIRCUIT
        };

        for (Resource res : tracked) {
            grid.add(buildBarRow(res));
        }

        outer.add(grid, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildBarRow(Resource res) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        // Resource Name Badge (No overlapping!)
        JPanel nameBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(new Color(20, 26, 42, 160));
                g2.fillRoundRect(0, 1, getWidth(), getHeight() - 2, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        nameBadge.setOpaque(false);
        nameBadge.setPreferredSize(new Dimension(88, 22));

        // Color dot
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(res.getColor());
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 16));
        nameBadge.add(dot);

        JLabel nameText = new JLabel(res.getDisplayName());
        nameText.setFont(UITheme.font(Font.BOLD, 9));
        nameText.setForeground(UITheme.FG_PRIMARY);
        nameBadge.add(nameText);

        row.add(nameBadge, BorderLayout.WEST);

        // Glass Progress Bar
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                int w = getWidth(), h = getHeight();

                // Track
                g2.setColor(new Color(15, 20, 34, 220));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 6, 6));

                // Fluid fill
                int val = getValue();
                if (val > 0) {
                    float pct = (float) val / Math.max(getMaximum(), 1);
                    int fillW = (int) (w * pct);

                    Color c = res.getColor();
                    GradientPaint gp = new GradientPaint(
                        0, 0, c,
                        fillW, 0, new Color(
                            Math.min(255, c.getRed() + 50),
                            Math.min(255, c.getGreen() + 50),
                            Math.min(255, c.getBlue() + 50)
                        )
                    );
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, 6, 6));

                    // Specular highlight
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fill(new RoundRectangle2D.Float(1, 1, Math.max(0, fillW - 2), h / 2, 4, 4));
                }

                // Border
                g2.setColor(UITheme.BORDER_GLASS);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 6, 6));
                g2.dispose();
            }
        };
        bar.setBorderPainted(false);
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(80, 12));
        bars.put(res, bar);
        row.add(bar, BorderLayout.CENTER);

        // Count Pill
        JLabel cnt = new JLabel("0");
        cnt.setForeground(UITheme.NEON_GREEN_LIGHT);
        cnt.setFont(UITheme.fontMono(Font.BOLD, 10));
        cnt.setPreferredSize(new Dimension(32, 22));
        cnt.setHorizontalAlignment(SwingConstants.RIGHT);
        countLabels.put(res, cnt);
        row.add(cnt, BorderLayout.EAST);

        return row;
    }

    // ── Session History Table ─────────────────────────────────────────────────

    private JScrollPane buildHistoryTable() {
        String[] cols = {"Session", "Start", "End", "Items", "Eff%"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setOpaque(false);
        table.setBackground(UITheme.BG_VOID);
        table.setForeground(UITheme.FG_PRIMARY);
        table.setFont(UITheme.fontMono(Font.PLAIN, 10));
        table.setGridColor(new Color(30, 42, 68));
        table.setRowHeight(22);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(40, 65, 115));
        table.setFillsViewportHeight(true);

        // Force custom Dark Glass Table Header Renderer (Eliminates White Windows Table Header!)
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                          boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                lbl.setOpaque(true);
                lbl.setBackground(new Color(18, 24, 44));
                lbl.setForeground(UITheme.NEON_GOLD);
                lbl.setFont(UITheme.font(Font.BOLD, 10));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, UITheme.BORDER_GLASS),
                    new EmptyBorder(4, 6, 4, 6)
                ));
                if (col == 3 || col == 4) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                }
                return lbl;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setReorderingAllowed(false);

        // Custom Cell Renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                          boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                c.setFont(UITheme.fontMono(Font.PLAIN, 10));

                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? new Color(16, 21, 36, 180) : new Color(20, 27, 46, 180));
                }

                if (col == 3) { // Items
                    c.setForeground(UITheme.NEON_GREEN_LIGHT);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 4) { // Eff%
                    c.setForeground(UITheme.NEON_GOLD);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    c.setForeground(UITheme.FG_PRIMARY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(75);  // Session
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  // Start
        table.getColumnModel().getColumn(2).setPreferredWidth(60);  // End
        table.getColumnModel().getColumn(3).setPreferredWidth(45);  // Items
        table.getColumnModel().getColumn(4).setPreferredWidth(50);  // Eff%

        JScrollPane sp = new JScrollPane(table);
        UITheme.styleScrollPane(sp);
        sp.setPreferredSize(new Dimension(330, 0));
        return sp;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public void refresh() {
        Map<Resource, Integer> totals = controller.getSessionTotals();
        int grand = totals.values().stream().mapToInt(Integer::intValue).sum();
        int maxV  = totals.values().stream().mapToInt(Integer::intValue).max().orElse(1);

        var profile = controller.getPlayerProfile();
        int lvl = profile.getCurrentLevel();

        tickLabel.setText("⏱  Tick:  " + controller.getTickCount());
        totalLabel.setText("📦  Items:  " + grand);
        levelLabel.setText("⭐  Lv." + lvl + " · " + profile.getLevelTitle() + " (" + profile.getTotalXP() + " XP)");

        double eff = controller.getLiveEfficiency();
        effLabel.setText(String.format("⚡  Eff.:  %.0f%%", eff));
        effLabel.setForeground(eff >= 70 ? UITheme.NEON_GREEN_LIGHT
                             : eff >= 40 ? UITheme.NEON_GOLD
                                         : UITheme.NEON_RED);

        for (Map.Entry<Resource, JLabel> e : countLabels.entrySet()) {
            int val = totals.getOrDefault(e.getKey(), 0);
            e.getValue().setText(String.valueOf(val));
            JProgressBar bar = bars.get(e.getKey());
            if (bar != null) {
                bar.setMaximum(Math.max(maxV, 1));
                bar.setValue(val);
            }
        }
        
        if (controller.isMatchRunning()) {
            matchHudLabel.setText("⚔ MATCH ACTIVE (Tick " + (controller.getTickCount()) + ")");
        } else {
            matchHudLabel.setText("No active match");
        }
        walletLabel.setText("💰 Wallet: " + controller.getWallet().formatBalance());

        if (controller.getTickCount() % 15 == 0) {
            refreshSessionHistory();
        }
    }

    public void refreshSessionHistory() {
        var sessions = controller.getProductionDAO().getRecentSessions(6);
        tableModel.setRowCount(0);
        for (String[] row : sessions) {
            // Clean up timestamps: Extract time HH:mm:ss for compact display
            String start = formatTimeOnly(row[1]);
            String end   = formatTimeOnly(row[2]);
            tableModel.addRow(new String[]{ row[0], start, end, row[3], row[4] });
        }
    }

    private String formatTimeOnly(String full) {
        if (full == null || full.isEmpty() || full.equalsIgnoreCase("Active")) return full;
        if (full.contains(" ")) {
            String[] parts = full.split(" ");
            return parts.length > 1 ? parts[1] : full;
        }
        if (full.contains("T")) {
            String[] parts = full.split("T");
            return parts.length > 1 ? parts[1] : full;
        }
        return full;
    }
}
