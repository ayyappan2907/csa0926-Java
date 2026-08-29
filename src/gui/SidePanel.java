package gui;

import controller.GameController;
import model.*;
import player.PlayerProfile;
import recipe.Resource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Side Panel — Sleek, Compact Dark Glassmorphism Control Center.
 *
 * Designed with a slim, compact profile (218px) to maximize the main grid canvas.
 */
public class SidePanel extends JPanel {

    private final GameController controller;
    private final Map<MachineType, JToggleButton> machineButtons = new LinkedHashMap<>();
    private Direction selectedDirection = Direction.EAST;
    private Resource  selectedResource  = Resource.IRON_ORE;

    private JToggleButton btnN, btnS, btnE, btnW;
    private JComboBox<Resource> resourceCombo;
    private JPanel recipeCardPanel;
    private JButton patternsBtn;
    private JPanel compassCenter;
    private WarehousePanel warehousePanel;
    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);
    private JButton tabBuildBtn;
    private JButton tabWarehouseBtn;
    private boolean isBuildActive = true;
    private boolean warehouseAlert = false;

    public SidePanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(240, 0));
        setBackground(UITheme.BG_VOID);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_GLASS));

        // ── Custom Segmented Tab Bar at Top ──────────────────────────────────
        add(buildSegmentedTabBar(), BorderLayout.NORTH);

        // ── Card Views ───────────────────────────────────────────────────────
        JPanel buildTab = new JPanel();
        buildTab.setLayout(new BoxLayout(buildTab, BoxLayout.Y_AXIS));
        buildTab.setBackground(UITheme.BG_VOID);
        buildTab.add(buildMachineSection());
        buildTab.add(vGap(4));
        buildTab.add(buildPatternsSection());
        buildTab.add(vGap(4));
        buildTab.add(buildDirectionSection());
        buildTab.add(vGap(4));
        buildTab.add(buildResourceSection());
        buildTab.add(vGap(4));
        buildTab.add(buildRecipeSection());
        buildTab.add(Box.createVerticalGlue());
        buildTab.add(buildFooter());

        warehousePanel = new WarehousePanel(controller);

        cardsPanel.setOpaque(false);
        cardsPanel.add(buildTab, "BUILD");
        cardsPanel.add(warehousePanel, "WAREHOUSE");
        add(cardsPanel, BorderLayout.CENTER);
    }

    private JPanel buildSegmentedTabBar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 6, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, 8, 4, 8));

        tabBuildBtn = createTabButton("🏭 Build", () -> switchTab("BUILD"));
        tabWarehouseBtn = createTabButton("🏪 Warehouse", () -> switchTab("WAREHOUSE"));

        bar.add(tabBuildBtn);
        bar.add(tabWarehouseBtn);
        return bar;
    }

    private JButton createTabButton(String text, Runnable onClick) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                boolean isCurrent = (this == (isBuildActive ? tabBuildBtn : tabWarehouseBtn));
                boolean hover = getModel().isRollover();

                Color bg = isCurrent ? UITheme.BRAND_PRIMARY_BLUE
                         : hover     ? new Color(28, 38, 64, 220)
                                     : new Color(16, 22, 38, 180);

                Color border = isCurrent ? UITheme.BRAND_VIVID_SKY_BLUE
                             : hover     ? new Color(80, 120, 200, 140)
                                         : UITheme.BORDER_GLASS;

                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 6, bg, border, isCurrent);

                if (this == tabWarehouseBtn && warehouseAlert) {
                    g2.setColor(UITheme.NEON_RED);
                    g2.fillOval(getWidth() - 14, 6, 8, 8);
                }

                g2.setFont(UITheme.font(Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(isCurrent ? Color.WHITE : (hover ? UITheme.FG_PRIMARY : UITheme.TEXT_SECONDARY_MUTED));
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(100, 30));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onClick.run());
        return btn;
    }

    private void switchTab(String name) {
        isBuildActive = "BUILD".equals(name);
        cardLayout.show(cardsPanel, name);
        if (tabBuildBtn != null) tabBuildBtn.repaint();
        if (tabWarehouseBtn != null) tabWarehouseBtn.repaint();
    }
    
    public void refreshWarehouse() {
        if (warehousePanel != null) {
            warehousePanel.refresh(controller.getSessionSoldTotal());
            boolean nearlyFull = false;
            for (economy.StorageSilo s : controller.getSilos().values()) {
                if (s.isNearlyFull()) {
                    nearlyFull = true;
                    break;
                }
            }
            updateWarehouseAlert(nearlyFull);
        }
    }
    
    public void updateWarehouseAlert(boolean nearlyFull) {
        this.warehouseAlert = nearlyFull;
        if (tabWarehouseBtn != null) tabWarehouseBtn.repaint();
    }

    private Component vGap(int h) { return Box.createVerticalStrut(h); }

    private JLabel sectionHeader(String icon, String title) {
        JLabel lbl = new JLabel(" " + icon + "  " + title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 4,
                    new Color(16, 22, 38, 180), UITheme.BORDER_GLASS, false);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(UITheme.font(Font.BOLD, 10));
        lbl.setForeground(UITheme.NEON_GOLD);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    // ── Machine Section ───────────────────────────────────────────────────────

    private JPanel buildMachineSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setBorder(new EmptyBorder(6, 8, 0, 8));

        section.add(sectionHeader("🏗", "MACHINES"));
        section.add(vGap(4));

        JPanel grid = new JPanel(new GridLayout(4, 2, 4, 4));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        MachineType[] types = {
            MachineType.EXTRACTOR, MachineType.BELT,
            MachineType.PROCESSOR, MachineType.COMBINER,
            MachineType.STORAGE_SILO, MachineType.EXPORT_TERMINAL,
            MachineType.ADVANCED_COMBINER, MachineType.CONVEYOR
        };

        for (MachineType type : types) {
            JToggleButton btn = createMachineButton(type);
            group.add(btn);
            machineButtons.put(type, btn);
            grid.add(btn);
        }

        machineButtons.get(MachineType.EXTRACTOR).setSelected(true);
        section.add(grid);
        return section;
    }

    private JToggleButton createMachineButton(MachineType type) {
        Color base = MachineRenderer.machineColor(type);
        PlayerProfile profile = controller.getPlayerProfile();
        boolean unlocked = profile.isUnlocked(type);

        JToggleButton btn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                boolean sel = isSelected();
                boolean isHover = getModel().isRollover();
                boolean isEn = isEnabled();
                int w = getWidth(), h = getHeight();

                Color fill = !isEn ? new Color(14, 18, 28, 160)
                           : sel ? new Color(base.getRed() / 4, base.getGreen() / 4, base.getBlue() / 4, 230)
                           : isHover ? UITheme.GLASS_BG_HOVER
                                     : UITheme.GLASS_CARD;

                Color border = !isEn ? new Color(35, 42, 60)
                             : sel ? base
                             : isHover ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 160)
                                       : UITheme.BORDER_GLASS;

                UITheme.drawGlassPanel(g2, 0, 0, w, h, 8, fill, border, isEn);

                // Top Accent Bar
                Color accent = isEn ? base : new Color(50, 60, 80);
                g2.setColor(sel ? accent : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 3f, 3, 3));

                // Icon (Cleanly separated from name!)
                String icon = isEn ? type.getIcon() : "🔒";
                g2.setFont(UITheme.fontEmoji(Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                int ix = (w - fm.stringWidth(icon)) / 2;
                g2.setColor(isEn ? Color.WHITE : UITheme.FG_LOCKED);
                g2.drawString(icon, ix, 18);

                // Name
                String name = type.getDisplayName();
                g2.setFont(UITheme.font(Font.BOLD, 9));
                FontMetrics nm = g2.getFontMetrics();
                g2.setColor(isEn ? (sel ? base.brighter() : UITheme.FG_PRIMARY) : UITheme.FG_LOCKED);
                g2.drawString(name, (w - nm.stringWidth(name)) / 2, 34);

                g2.dispose();
            }
        };

        btn.setEnabled(unlocked);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(106, 40));
        btn.setCursor(unlocked ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        if (!unlocked) btn.setToolTipText("🔒 Locked — unlock at higher level");

        btn.addItemListener(e -> {
            btn.repaint();
            updateRecipeCards(type);
            if (resourceCombo != null) {
                resourceCombo.setEnabled(getSelectedMachineType() == MachineType.EXTRACTOR);
            }
        });
        return btn;
    }

    // ── Patterns Section ──────────────────────────────────────────────────────

    private JPanel buildPatternsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(new EmptyBorder(0, 8, 0, 8));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        section.setAlignmentX(LEFT_ALIGNMENT);

        PlayerProfile profile = controller.getPlayerProfile();
        boolean ok = profile.isPatternsUnlocked();

        patternsBtn = new JButton(ok ? "📐 Blueprint Library" : "🔒 Patterns (Lv.7+)") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                boolean isEn = isEnabled();
                boolean isHover = getModel().isRollover();

                Color fill = !isEn ? new Color(16, 20, 32, 160)
                           : isHover ? new Color(38, 90, 185, 220)
                                     : new Color(25, 65, 145, 180);

                Color border = !isEn ? new Color(40, 48, 70)
                             : isHover ? UITheme.NEON_BLUE_LIGHT
                                       : UITheme.NEON_BLUE;

                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 6, fill, border, isEn);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        patternsBtn.setFont(UITheme.fontEmoji(Font.BOLD, 10));
        patternsBtn.setEnabled(ok);
        patternsBtn.setOpaque(false);
        patternsBtn.setContentAreaFilled(false);
        patternsBtn.setBorderPainted(false);
        patternsBtn.setFocusPainted(false);
        patternsBtn.setForeground(ok ? UITheme.FG_PRIMARY : UITheme.FG_MUTED);
        patternsBtn.setCursor(ok ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        patternsBtn.setPreferredSize(new Dimension(202, 28));

        patternsBtn.addActionListener(e -> {
            if (controller.getPlayerProfile().isPatternsUnlocked()) {
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
                new PatternDialog(owner, controller.getPlayerProfile(), pattern -> {
                    controller.loadPattern(pattern);
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w != null) w.repaint();
                }).setVisible(true);
            }
        });

        section.add(patternsBtn, BorderLayout.CENTER);
        return section;
    }

    // ── Direction Pad Section ─────────────────────────────────────────────────

    private JPanel buildDirectionSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setBorder(new EmptyBorder(0, 8, 0, 8));

        section.add(sectionHeader("🧭", "DIRECTION"));
        section.add(vGap(4));
        section.add(buildDirectionPad());
        return section;
    }

    private JPanel buildDirectionPad() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 2, 2, 2);

        btnN = dirBtn(Direction.NORTH);
        btnS = dirBtn(Direction.SOUTH);
        btnE = dirBtn(Direction.EAST);
        btnW = dirBtn(Direction.WEST);

        ButtonGroup dg = new ButtonGroup();
        dg.add(btnN); dg.add(btnS); dg.add(btnE); dg.add(btnW);
        btnE.setSelected(true);

        compassCenter = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                int w = getWidth(), h = getHeight();
                int cx = w / 2, cy = h / 2;

                g2.setColor(new Color(15, 22, 38, 240));
                g2.fillOval(2, 2, w - 4, h - 4);
                g2.setColor(UITheme.BORDER_GLASS);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(2, 2, w - 4, h - 4);

                int[] delta = selectedDirection.getDelta();
                int dx = delta[1], dy = delta[0];

                g2.setColor(UITheme.NEON_BLUE_LIGHT);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy, cx + dx * 8, cy + dy * 8);

                g2.setColor(UITheme.NEON_GOLD);
                g2.fillOval(cx - 3, cy - 3, 6, 6);
                g2.dispose();
            }
        };
        compassCenter.setOpaque(false);
        compassCenter.setPreferredSize(new Dimension(28, 28));

        g.gridx = 1; g.gridy = 0; wrap.add(btnN, g);
        g.gridx = 0; g.gridy = 1; wrap.add(btnW, g);
        g.gridx = 1; g.gridy = 1; wrap.add(compassCenter, g);
        g.gridx = 2; g.gridy = 1; wrap.add(btnE, g);
        g.gridx = 1; g.gridy = 2; wrap.add(btnS, g);

        return wrap;
    }

    private JToggleButton dirBtn(Direction dir) {
        JToggleButton btn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                boolean sel = isSelected();
                boolean isHover = getModel().isRollover();
                int w = getWidth(), h = getHeight();
                int cx = w / 2, cy = h / 2;

                Color fill = sel ? new Color(30, 95, 200, 240)
                           : isHover ? UITheme.GLASS_BG_HOVER
                                     : UITheme.GLASS_CARD;

                Color border = sel ? UITheme.NEON_BLUE_LIGHT
                             : isHover ? UITheme.BORDER_GLOW
                                       : UITheme.BORDER_GLASS;

                UITheme.drawGlassPanel(g2, 0, 0, w, h, 6, fill, border, true);
                g2.setColor(sel ? Color.WHITE : (isHover ? UITheme.FG_PRIMARY : UITheme.FG_SECONDARY));

                int arrowSz = 5;
                Polygon poly = new Polygon();
                switch (dir) {
                    case NORTH:
                        poly.addPoint(cx, cy - arrowSz - 1);
                        poly.addPoint(cx - arrowSz, cy + arrowSz);
                        poly.addPoint(cx + arrowSz, cy + arrowSz);
                        break;
                    case SOUTH:
                        poly.addPoint(cx, cy + arrowSz + 1);
                        poly.addPoint(cx - arrowSz, cy - arrowSz);
                        poly.addPoint(cx + arrowSz, cy - arrowSz);
                        break;
                    case WEST:
                        poly.addPoint(cx - arrowSz - 1, cy);
                        poly.addPoint(cx + arrowSz, cy - arrowSz);
                        poly.addPoint(cx + arrowSz, cy + arrowSz);
                        break;
                    case EAST:
                    default:
                        poly.addPoint(cx + arrowSz + 1, cy);
                        poly.addPoint(cx - arrowSz, cy - arrowSz);
                        poly.addPoint(cx - arrowSz, cy + arrowSz);
                        break;
                }
                g2.fill(poly);
                g2.dispose();
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(38, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            selectedDirection = dir;
            int sr = controller.getSelectedRow(), sc = controller.getSelectedCol();
            if (sr >= 0) {
                controller.setOutputDirection(sr, sc, dir);
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) w.repaint();
            }
            if (compassCenter != null) compassCenter.repaint();
            btn.repaint();
        });

        return btn;
    }

    // ── Resource Selection Section ────────────────────────────────────────────

    private JPanel buildResourceSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setBorder(new EmptyBorder(0, 8, 0, 8));

        section.add(sectionHeader("⛏", "RESOURCE"));
        section.add(vGap(4));

        PlayerProfile profile = controller.getPlayerProfile();
        java.util.List<Resource> extractables = new java.util.ArrayList<>();
        if (profile.isResourceUnlocked(Resource.IRON_ORE)) extractables.add(Resource.IRON_ORE);
        if (profile.isResourceUnlocked(Resource.COAL))     extractables.add(Resource.COAL);
        if (extractables.isEmpty()) extractables.add(Resource.IRON_ORE);

        resourceCombo = new JComboBox<>(extractables.toArray(new Resource[0]));
        resourceCombo.setUI(new UITheme.GlassComboBoxUI());
        resourceCombo.setFont(UITheme.font(Font.BOLD, 11));
        resourceCombo.setForeground(UITheme.FG_PRIMARY);
        resourceCombo.setBackground(UITheme.BG_CARD_SOLID);
        resourceCombo.setPreferredSize(new Dimension(202, 28));
        resourceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        resourceCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, isSel, focus);
                setOpaque(true);
                setFont(UITheme.font(Font.BOLD, 11));
                setBorder(new EmptyBorder(4, 6, 4, 6));

                if (value instanceof Resource) {
                    Resource r = (Resource) value;
                    setText(" ● " + r.getDisplayName());
                    setForeground(r.getColor().brighter());
                }

                setBackground(isSel ? new Color(40, 60, 105) : UITheme.BG_CARD_SOLID);
                return this;
            }
        });

        resourceCombo.addActionListener(e -> {
            selectedResource = (Resource) resourceCombo.getSelectedItem();
            int sr = controller.getSelectedRow(), sc = controller.getSelectedCol();
            if (sr >= 0) {
                controller.setExtractorResource(sr, sc, selectedResource);
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) w.repaint();
            }
        });

        section.add(resourceCombo);
        return section;
    }

    // ── Recipe Section ────────────────────────────────────────────────────────

    private JPanel buildRecipeSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setBorder(new EmptyBorder(0, 8, 0, 8));

        section.add(sectionHeader("📋", "RECIPES"));
        section.add(vGap(4));

        recipeCardPanel = new JPanel();
        recipeCardPanel.setLayout(new BoxLayout(recipeCardPanel, BoxLayout.Y_AXIS));
        recipeCardPanel.setOpaque(false);

        JScrollPane sp = new JScrollPane(recipeCardPanel);
        UITheme.styleScrollPane(sp);
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        sp.setPreferredSize(new Dimension(202, 115));

        section.add(sp);
        updateRecipeCards(MachineType.EXTRACTOR);
        return section;
    }

    private void updateRecipeCards(MachineType type) {
        if (recipeCardPanel == null) return;
        recipeCardPanel.removeAll();

        if (type == MachineType.EXTRACTOR) {
            recipeCardPanel.add(infoCard("⛏", "Extracts ore/coal", "Cycle: 2 ticks"));
        } else if (type == MachineType.BELT) {
            recipeCardPanel.add(infoCard("📦", "Transports cargo", "Direct flow"));
        } else {
            var recipes = controller.getRecipeBook().getRecipes(type);
            if (recipes.isEmpty()) {
                recipeCardPanel.add(infoCard("❓", "No recipes available.", ""));
            } else {
                for (var r : recipes) {
                    StringBuilder ins = new StringBuilder();
                    r.getInputs().forEach(res -> ins.append(res.getDisplayName()).append(" + "));
                    String inStr = ins.length() > 3 ? ins.substring(0, ins.length() - 3) : "?";

                    recipeCardPanel.add(recipeCard(
                        r.getName(),
                        inStr,
                        r.getOutput().getDisplayName(),
                        r.getTicksRequired() + "t",
                        r.getOutput().getColor()
                    ));
                    recipeCardPanel.add(Box.createVerticalStrut(3));
                }
            }
        }
        recipeCardPanel.revalidate();
        recipeCardPanel.repaint();
    }

    private JPanel infoCard(String icon, String desc, String sub) {
        JPanel card = new JPanel(new BorderLayout(4, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 6,
                    UITheme.GLASS_CARD, UITheme.BORDER_GLASS, true);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(6, 8, 6, 8));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel l1 = new JLabel(icon + " " + desc);
        l1.setFont(UITheme.font(Font.BOLD, 9));
        l1.setForeground(UITheme.FG_PRIMARY);
        card.add(l1, BorderLayout.NORTH);

        if (!sub.isEmpty()) {
            JLabel l2 = new JLabel(sub);
            l2.setFont(UITheme.fontMono(Font.PLAIN, 9));
            l2.setForeground(UITheme.NEON_BLUE_LIGHT);
            card.add(l2, BorderLayout.SOUTH);
        }
        return card;
    }

    private JPanel recipeCard(String name, String inputs, String output, String time, Color accent) {
        JPanel card = new JPanel(new BorderLayout(2, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 6,
                    UITheme.GLASS_CARD, UITheme.BORDER_GLASS, true);

                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 3, getHeight(), 2, 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(4, 8, 4, 6));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel nameLbl = new JLabel(name + " (" + time + ")");
        nameLbl.setFont(UITheme.font(Font.BOLD, 9));
        nameLbl.setForeground(accent.brighter());
        card.add(nameLbl, BorderLayout.NORTH);

        JLabel flowLbl = new JLabel(inputs + " → " + output);
        flowLbl.setFont(UITheme.fontMono(Font.PLAIN, 8));
        flowLbl.setForeground(UITheme.FG_PRIMARY);
        card.add(flowLbl, BorderLayout.CENTER);

        return card;
    }

    // ── Footer Helper Chips ───────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridLayout(2, 2, 3, 3));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 8, 6, 8));

        String[][] tips = {
            {"Left", "Place"},
            {"Right", "Del"},
            {"R", "Rotate"},
            {"Space", "Pause"}
        };

        for (String[] tip : tips) {
            JPanel chip = new JPanel(new BorderLayout(3, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UITheme.applyQualityHints(g2);
                    UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 4,
                        new Color(16, 22, 38, 180), UITheme.BORDER_GLASS, false);
                    g2.dispose();
                }
            };
            chip.setOpaque(false);
            chip.setBorder(new EmptyBorder(2, 4, 2, 4));

            JLabel k = new JLabel(tip[0]);
            k.setFont(UITheme.font(Font.BOLD, 9));
            k.setForeground(UITheme.NEON_GOLD);
            chip.add(k, BorderLayout.WEST);

            JLabel v = new JLabel(tip[1]);
            v.setFont(UITheme.font(Font.PLAIN, 9));
            v.setForeground(UITheme.FG_SECONDARY);
            chip.add(v, BorderLayout.CENTER);
            p.add(chip);
        }
        return p;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public MachineType getSelectedMachineType() {
        for (Map.Entry<MachineType, JToggleButton> e : machineButtons.entrySet()) {
            if (e.getValue().isSelected()) return e.getKey();
        }
        return MachineType.EXTRACTOR;
    }

    public void selectMachineType(MachineType type) {
        if (machineButtons.containsKey(type)) {
            machineButtons.get(type).setSelected(true);
        }
    }

    public Direction getSelectedDirection() { return selectedDirection; }
    public Resource  getSelectedResource()  { return selectedResource; }

    public void rotateClockwise() {
        Direction next;
        switch (selectedDirection) {
            case NORTH: next = Direction.EAST;  break;
            case EAST:  next = Direction.SOUTH; break;
            case SOUTH: next = Direction.WEST;  break;
            case WEST:  default: next = Direction.NORTH; break;
        }
        setDirection(next);
    }

    public void setDirection(Direction dir) {
        selectedDirection = dir;
        switch (dir) {
            case NORTH: if (btnN != null) btnN.setSelected(true); break;
            case SOUTH: if (btnS != null) btnS.setSelected(true); break;
            case WEST:  if (btnW != null) btnW.setSelected(true); break;
            case EAST:  default: if (btnE != null) btnE.setSelected(true); break;
        }
        int sr = controller.getSelectedRow(), sc = controller.getSelectedCol();
        if (sr >= 0) {
            controller.setOutputDirection(sr, sc, dir);
        }
        if (compassCenter != null) compassCenter.repaint();
        repaint();
    }

    public void refreshUnlocks() {
        PlayerProfile profile = controller.getPlayerProfile();

        for (Map.Entry<MachineType, JToggleButton> entry : machineButtons.entrySet()) {
            MachineType type = entry.getKey();
            JToggleButton btn = entry.getValue();
            boolean unlocked = profile.isUnlocked(type);
            btn.setEnabled(unlocked);
            btn.setToolTipText(unlocked ? null : "🔒 Locked — reach higher level");
            btn.setCursor(unlocked ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            btn.repaint();
        }

        if (patternsBtn != null) {
            boolean patternsUnlocked = profile.isPatternsUnlocked();
            patternsBtn.setEnabled(patternsUnlocked);
            patternsBtn.setText(patternsUnlocked ? "📐 Blueprint Library" : "🔒 Patterns (Lv.7+)");
            patternsBtn.setForeground(patternsUnlocked ? UITheme.FG_PRIMARY : UITheme.FG_MUTED);
            patternsBtn.setCursor(patternsUnlocked ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            patternsBtn.repaint();
        }

        java.util.List<Resource> extractables = new java.util.ArrayList<>();
        if (profile.isResourceUnlocked(Resource.IRON_ORE)) extractables.add(Resource.IRON_ORE);
        if (profile.isResourceUnlocked(Resource.COAL))     extractables.add(Resource.COAL);
        if (extractables.isEmpty()) extractables.add(Resource.IRON_ORE);
        resourceCombo.setModel(new DefaultComboBoxModel<>(extractables.toArray(new Resource[0])));

        revalidate();
        repaint();
    }

    public void syncFromCell(int row, int col) {
        var cell = controller.getGrid().getCell(row, col);
        if (cell == null) return;

        Direction dir = cell.getOutputDirection();
        setDirection(dir);

        if (cell.getMachineType() == MachineType.EXTRACTOR) {
            resourceCombo.setSelectedItem(cell.getExtractorResource());
            selectedResource = cell.getExtractorResource();
        }
    }
}
