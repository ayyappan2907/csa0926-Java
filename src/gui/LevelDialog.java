package gui;

import model.MachineType;
import player.LevelConfig;
import player.PlayerProfile;
import recipe.Resource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Career Progression Roadmap Dialog.
 *
 * Displays the full 10-level automation career tree with glowing level rings,
 * unlock milestones, and current progress highlighting.
 */
public class LevelDialog extends JDialog {

    private final PlayerProfile profile;

    public LevelDialog(Frame owner, PlayerProfile profile) {
        super(owner, "🎖 Automation Career Progression", true);
        this.profile = profile;

        getContentPane().setBackground(UITheme.BG_VOID);
        setLayout(new BorderLayout(0, 0));
        getRootPane().setBorder(new EmptyBorder(12, 14, 12, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildLevelList(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(680, 600);
        setLocationRelativeTo(owner);
        setResizable(true);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("🎖 Automation Career Progression (10 Tiers)");
        title.setForeground(UITheme.NEON_GOLD);
        title.setFont(UITheme.font(Font.BOLD, 15));
        p.add(title, BorderLayout.WEST);

        JLabel xpInfo = new JLabel(profile.getTotalXP() + " Total Career XP");
        xpInfo.setForeground(UITheme.NEON_GREEN_LIGHT);
        xpInfo.setFont(UITheme.font(Font.BOLD, 12));
        p.add(xpInfo, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildLevelList() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(4, 2, 4, 2));

        int current = profile.getCurrentLevel();
        JPanel currentCard = null;

        for (int lvl = 1; lvl <= LevelConfig.MAX_LEVEL; lvl++) {
            JPanel card = buildLevelCard(lvl, current);
            if (lvl == current) currentCard = card;
            container.add(card);
            container.add(Box.createVerticalStrut(8));
        }

        JScrollPane sp = new JScrollPane(container);
        UITheme.styleScrollPane(sp);

        final JPanel scrollTarget = currentCard;
        if (scrollTarget != null) {
            SwingUtilities.invokeLater(() -> {
                Rectangle r = scrollTarget.getBounds();
                r.y = Math.max(0, r.y - 60);
                r.height += 120;
                ((JComponent) scrollTarget.getParent()).scrollRectToVisible(r);
            });
        }
        return sp;
    }

    private JPanel buildLevelCard(int lvl, int currentLevel) {
        boolean achieved = lvl < currentLevel;
        boolean current  = lvl == currentLevel;
        boolean locked   = lvl > currentLevel;

        LevelConfig.LevelEntry entry = LevelConfig.getEntry(lvl);

        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                Color fill = current ? new Color(38, 32, 22, 220)
                           : locked ? new Color(16, 20, 32, 180)
                                    : UITheme.GLASS_CARD;

                Color border = current ? UITheme.NEON_GOLD
                             : locked ? new Color(35, 45, 65)
                                      : UITheme.BORDER_GLASS;

                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 10, fill, border, !locked);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        card.add(buildLevelBadge(lvl, achieved, current, locked), BorderLayout.WEST);

        // Center Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel("Tier " + lvl + " · " + entry.title);
        nameLbl.setForeground(current ? UITheme.NEON_GOLD_LIGHT : locked ? UITheme.FG_MUTED : UITheme.FG_PRIMARY);
        nameLbl.setFont(UITheme.font(Font.BOLD, 13));
        info.add(nameLbl);

        info.add(Box.createVerticalStrut(3));

        JTextArea descArea = new JTextArea(entry.description);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setForeground(locked ? UITheme.FG_LOCKED : UITheme.FG_SECONDARY);
        descArea.setFont(UITheme.font(Font.PLAIN, 11));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        info.add(descArea);

        String unlocks = newUnlocksText(lvl);
        if (!unlocks.isEmpty()) {
            info.add(Box.createVerticalStrut(3));
            JLabel unlockLbl = new JLabel(unlocks);
            unlockLbl.setForeground(locked ? UITheme.FG_LOCKED : UITheme.NEON_GREEN_LIGHT);
            unlockLbl.setFont(UITheme.fontMono(Font.BOLD, 10));
            info.add(unlockLbl);
        }

        card.add(info, BorderLayout.CENTER);

        // Right Status
        JPanel statusPanel = new JPanel(new BorderLayout(0, 4));
        statusPanel.setOpaque(false);
        statusPanel.setPreferredSize(new Dimension(95, 40));

        JLabel statusLbl = new JLabel(achieved ? "✅ Mastered" : current ? "▶ CURRENT" : "🔒 Locked");
        statusLbl.setFont(UITheme.font(Font.BOLD, 11));
        statusLbl.setForeground(achieved ? UITheme.NEON_GREEN_LIGHT : current ? UITheme.NEON_GOLD : UITheme.FG_MUTED);
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(statusLbl, BorderLayout.NORTH);

        String xpText = locked
            ? Math.max(0, entry.xpRequired - profile.getTotalXP()) + " XP to go"
            : entry.xpRequired + " XP";
        JLabel xpLbl = new JLabel(xpText);
        xpLbl.setFont(UITheme.fontMono(Font.PLAIN, 10));
        xpLbl.setForeground(locked ? UITheme.NEON_RED : UITheme.FG_MUTED);
        xpLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(xpLbl, BorderLayout.SOUTH);

        card.add(statusPanel, BorderLayout.EAST);
        return card;
    }

    private JPanel buildLevelBadge(int lvl, boolean achieved, boolean current, boolean locked) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                int size = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                Color ring = current ? UITheme.NEON_GOLD : achieved ? UITheme.NEON_GREEN : new Color(50, 60, 85);
                g2.setColor(ring);
                g2.setStroke(new BasicStroke(current ? 3f : 2f));
                g2.drawOval(x, y, size, size);

                g2.setColor(locked ? new Color(18, 22, 34) : new Color(22, 30, 52));
                g2.fillOval(x + 2, y + 2, size - 4, size - 4);

                String text = String.valueOf(lvl);
                g2.setFont(UITheme.font(Font.BOLD, 15));
                g2.setColor(locked ? UITheme.FG_LOCKED : ring);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(48, 48));
        return badge;
    }

    private String newUnlocksText(int lvl) {
        if (lvl == 1) {
            return "🔓 Unlocks: " + joinNames(LevelConfig.getEntry(1).unlockedMachines);
        }
        Set<MachineType> newMachines = new LinkedHashSet<>(LevelConfig.getEntry(lvl).unlockedMachines);
        newMachines.removeAll(LevelConfig.getEntry(lvl - 1).unlockedMachines);

        Set<Resource> newResources = new LinkedHashSet<>(LevelConfig.getEntry(lvl).unlockedResources);
        newResources.removeAll(LevelConfig.getEntry(lvl - 1).unlockedResources);

        if (newMachines.isEmpty() && newResources.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("🔓 Unlocks: ");
        if (!newMachines.isEmpty()) sb.append(joinNames(newMachines));
        if (!newMachines.isEmpty() && !newResources.isEmpty()) sb.append(" · ");
        for (Resource r : newResources) sb.append(r.getDisplayName()).append(" ");
        return sb.toString().trim();
    }

    private String joinNames(Set<MachineType> types) {
        StringBuilder sb = new StringBuilder();
        for (MachineType t : types) sb.append(t.getDisplayName()).append(" ");
        return sb.toString().trim();
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel hint = new JLabel("Every automated product earns XP — scale up assembly lines to climb ranks!");
        hint.setForeground(UITheme.FG_MUTED);
        hint.setFont(UITheme.font(Font.PLAIN, 11));
        p.add(hint, BorderLayout.WEST);

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
}
