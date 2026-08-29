package gui;

import model.MachineType;
import pattern.PatternAnalyzer;
import pattern.PatternLibrary;
import pattern.ProductionPattern;
import player.PlayerProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Blueprint / Pattern Library Modal Dialog.
 *
 * Displays blueprints with mini-grid layout visualizers, real simulated
 * throughput statistics, and one-click blueprint loading.
 */
public class PatternDialog extends JDialog {

    private final PlayerProfile profile;
    private final Consumer<ProductionPattern> onLoad;

    public PatternDialog(Frame owner, PlayerProfile profile,
                         Consumer<ProductionPattern> onLoad) {
        super(owner, "📐 Production Blueprint Library", true);
        this.profile = profile;
        this.onLoad  = onLoad;

        getContentPane().setBackground(UITheme.BG_VOID);
        setLayout(new BorderLayout(0, 0));
        getRootPane().setBorder(new EmptyBorder(12, 14, 12, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildPatternList(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(760, 580);
        setLocationRelativeTo(owner);
        setResizable(true);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("📐 Production Blueprint Library");
        title.setForeground(UITheme.NEON_GOLD);
        title.setFont(UITheme.font(Font.BOLD, 15));
        p.add(title, BorderLayout.WEST);

        JLabel lvlInfo = new JLabel("Your Level: " + profile.getCurrentLevel() + " · " + profile.getLevelTitle());
        lvlInfo.setForeground(UITheme.NEON_GREEN_LIGHT);
        lvlInfo.setFont(UITheme.font(Font.BOLD, 12));
        p.add(lvlInfo, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildPatternList() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(4, 2, 4, 2));

        List<ProductionPattern> all = PatternLibrary.getAll();
        for (ProductionPattern pattern : all) {
            boolean locked = profile.getCurrentLevel() < pattern.getMinLevel();
            container.add(buildPatternCard(pattern, locked));
            container.add(Box.createVerticalStrut(8));
        }

        JScrollPane sp = new JScrollPane(container);
        UITheme.styleScrollPane(sp);
        return sp;
    }

    private JPanel buildPatternCard(ProductionPattern pattern, boolean locked) {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                Color fill = locked ? new Color(16, 20, 32, 180) : UITheme.GLASS_CARD;
                Color border = locked ? new Color(35, 45, 65) : UITheme.BORDER_GLASS;
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 10, fill, border, !locked);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Left Mini Preview
        card.add(buildMiniPreview(pattern, locked), BorderLayout.WEST);

        // Center Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(pattern.getName());
        nameLbl.setForeground(locked ? UITheme.FG_MUTED : UITheme.NEON_GOLD);
        nameLbl.setFont(UITheme.font(Font.BOLD, 13));
        info.add(nameLbl);

        info.add(Box.createVerticalStrut(3));

        JTextArea descArea = new JTextArea(pattern.getDescription());
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setForeground(locked ? UITheme.FG_LOCKED : UITheme.FG_PRIMARY);
        descArea.setFont(UITheme.font(Font.PLAIN, 11));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        info.add(descArea);

        info.add(Box.createVerticalStrut(4));
        info.add(buildStatsChip(pattern, locked));

        card.add(info, BorderLayout.CENTER);

        // Right Load Button
        JPanel btnPanel = new JPanel(new BorderLayout(0, 4));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(100, 50));

        JButton loadBtn = new JButton(locked ? "🔒 Locked" : "▶ Load") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                boolean isHover = getModel().isRollover();
                Color fill = locked ? new Color(20, 25, 38)
                           : isHover ? new Color(40, 110, 220) : new Color(25, 75, 170);
                Color border = locked ? UITheme.FG_LOCKED : UITheme.NEON_BLUE;
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 8, fill, border, !locked);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        loadBtn.setFont(UITheme.fontEmoji(Font.BOLD, 11));
        loadBtn.setEnabled(!locked);
        loadBtn.setOpaque(false);
        loadBtn.setContentAreaFilled(false);
        loadBtn.setBorderPainted(false);
        loadBtn.setFocusPainted(false);
        loadBtn.setForeground(locked ? UITheme.FG_LOCKED : Color.WHITE);
        loadBtn.setCursor(locked ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadBtn.setPreferredSize(new Dimension(100, 32));

        if (!locked) {
            loadBtn.addActionListener(e -> {
                onLoad.accept(pattern);
                dispose();
            });
        }

        JLabel reqLbl = new JLabel("Lv." + pattern.getMinLevel() + "+ required");
        reqLbl.setForeground(locked ? UITheme.NEON_RED : UITheme.NEON_GREEN_LIGHT);
        reqLbl.setFont(UITheme.fontMono(Font.BOLD, 9));
        reqLbl.setHorizontalAlignment(SwingConstants.CENTER);

        btnPanel.add(loadBtn, BorderLayout.NORTH);
        btnPanel.add(reqLbl, BorderLayout.SOUTH);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel buildStatsChip(ProductionPattern pattern, boolean locked) {
        PatternAnalyzer.Result stats = PatternAnalyzer.analyze(pattern);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chip.setOpaque(false);

        JLabel itemsLbl = new JLabel(String.format("📦 %d items / %d ticks", stats.totalItems, PatternAnalyzer.SIM_TICKS));
        itemsLbl.setFont(UITheme.fontMono(Font.PLAIN, 10));
        itemsLbl.setForeground(locked ? UITheme.FG_LOCKED : UITheme.FG_SECONDARY);
        chip.add(itemsLbl);

        Color effColor = locked ? UITheme.FG_LOCKED
            : stats.efficiencyPct >= 70 ? UITheme.NEON_GREEN_LIGHT
            : stats.efficiencyPct >= 40 ? UITheme.NEON_GOLD
                                        : UITheme.NEON_RED;

        JLabel effLbl = new JLabel(String.format("⚡ %.0f%% uptime", stats.efficiencyPct));
        effLbl.setFont(UITheme.fontMono(Font.BOLD, 10));
        effLbl.setForeground(effColor);
        chip.add(effLbl);

        return chip;
    }

    private JPanel buildMiniPreview(ProductionPattern pattern, boolean locked) {
        final int CELL = 11;
        int rows = Math.max(pattern.getHeight(), 1);
        int cols = Math.max(pattern.getWidth(), 1);

        JPanel preview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                g2.setColor(new Color(10, 14, 24));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Grid lines
                g2.setColor(new Color(25, 35, 55));
                for (int r = 0; r <= rows; r++) g2.drawLine(0, r * CELL, cols * CELL, r * CELL);
                for (int c = 0; c <= cols; c++) g2.drawLine(c * CELL, 0, c * CELL, rows * CELL);

                // Machine tiles
                for (ProductionPattern.PlacementEntry ent : pattern.getPlacements()) {
                    int px = ent.col * CELL;
                    int py = ent.row * CELL;
                    Color mc = locked ? new Color(50, 60, 80) : MachineRenderer.machineColor(ent.machineType);
                    g2.setColor(mc);
                    g2.fillRoundRect(px + 1, py + 1, CELL - 2, CELL - 2, 3, 3);
                }
                g2.dispose();
            }
        };
        preview.setPreferredSize(new Dimension(cols * CELL + 1, rows * CELL + 1));
        preview.setOpaque(false);
        preview.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GLASS, 1));
        return preview;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        addLegendItem(legend, MachineRenderer.C_EXTRACTOR, "Extractor");
        addLegendItem(legend, MachineRenderer.C_BELT,      "Belt");
        addLegendItem(legend, MachineRenderer.C_PROCESSOR, "Processor");
        addLegendItem(legend, MachineRenderer.C_COMBINER,  "Combiner");
        p.add(legend, BorderLayout.WEST);

        JButton closeBtn = new JButton("Close") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 8,
                    UITheme.GLASS_CARD, UITheme.BORDER_GLASS, true);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(UITheme.font(Font.BOLD, 11));
        closeBtn.setForeground(UITheme.FG_PRIMARY);
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(80, 30));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(closeBtn);
        p.add(btnWrap, BorderLayout.EAST);
        return p;
    }

    private void addLegendItem(JPanel parent, Color color, String name) {
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(color);
                g.fillRoundRect(0, 2, 10, 10, 3, 3);
            }
        };
        dot.setPreferredSize(new Dimension(10, 14));
        dot.setOpaque(false);
        parent.add(dot);

        JLabel lbl = new JLabel(name);
        lbl.setForeground(UITheme.FG_SECONDARY);
        lbl.setFont(UITheme.font(Font.PLAIN, 10));
        parent.add(lbl);
    }
}
