package gui;

import player.LevelConfig;
import player.PlayerProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Level Panel — Compact Glassmorphic Career Status in Toolbar.
 *
 * Features:
 *  - Radiant circular level badge with level number
 *  - Player title & animated XP progress bar with liquid gradient
 *  - Rising "+N XP" particle toast on production events
 *  - Golden flash animation upon level-up
 *  - Click opens the full 10-level Career Roadmap Dialog
 */
public class LevelPanel extends JPanel {

    private final PlayerProfile profile;

    // Animated flash on level-up
    private float flashAlpha = 0f;
    private Timer flashTimer;

    // Floating "+N XP" popup
    private int   popupXpAmount = 0;
    private float popupAlpha    = 0f;
    private int   popupRise     = 0;
    private Timer popupTimer;

    // Sub-components
    private JLabel titleLabel;
    private JLabel xpLabel;
    private JProgressBar xpBar;
    private JPanel badge;

    public LevelPanel(PlayerProfile profile) {
        this.profile = profile;
        setOpaque(false);
        setPreferredSize(new Dimension(240, 52));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_GLASS),
            new EmptyBorder(4, 10, 4, 10)
        ));
        setLayout(new BorderLayout(10, 0));

        badge = buildBadge();
        add(badge, BorderLayout.WEST);
        add(buildDetails(), BorderLayout.CENTER);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(LevelPanel.this);
                new LevelDialog(owner, profile).setVisible(true);
            }
        };
        addMouseListener(clickHandler);
        badge.addMouseListener(clickHandler);

        updateTooltip();
    }

    // ── Level Badge Circle ────────────────────────────────────────────────────

    private JPanel buildBadge() {
        JPanel b = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                int size = Math.min(getWidth(), getHeight()) - 6;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Level-up flash effect
                if (flashAlpha > 0f) {
                    g2.setColor(new Color(1f, 0.85f, 0.2f, flashAlpha));
                    g2.fillOval(x - 3, y - 3, size + 6, size + 6);
                }

                // Outer Neon Ring
                g2.setColor(UITheme.NEON_GREEN);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x, y, size, size);

                // Core Fill
                g2.setColor(new Color(18, 25, 45, 230));
                g2.fillOval(x + 2, y + 2, size - 4, size - 4);

                // Level Number
                int lvl = profile.getCurrentLevel();
                String text = String.valueOf(lvl);
                g2.setFont(UITheme.font(Font.BOLD, 15));
                g2.setColor(UITheme.NEON_GOLD);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);

                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(44, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Details (Title + XP Bar) ─────────────────────────────────────────────

    private JPanel buildDetails() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Lv.1 · " + profile.getLevelTitle());
        titleLabel.setForeground(UITheme.NEON_GOLD);
        titleLabel.setFont(UITheme.font(Font.BOLD, 12));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(3));

        xpBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                int w = getWidth(), h = getHeight();

                // Track
                g2.setColor(new Color(16, 22, 38, 220));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 6, 6));

                // Fluid XP Fill
                float pct = (float) getValue() / Math.max(getMaximum(), 1);
                int fillW = (int) (w * pct);
                if (fillW > 0) {
                    GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.NEON_BLUE,
                        fillW, 0, UITheme.NEON_GREEN
                    );
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, 6, 6));
                }

                // Border
                g2.setColor(UITheme.BORDER_GLASS);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 6, 6));
                g2.dispose();
            }
        };
        xpBar.setBorderPainted(false);
        xpBar.setOpaque(false);
        xpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        xpBar.setPreferredSize(new Dimension(110, 7));
        xpBar.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(xpBar);

        panel.add(Box.createVerticalStrut(3));

        xpLabel = new JLabel("XP: 0 / 100");
        xpLabel.setForeground(UITheme.FG_SECONDARY);
        xpLabel.setFont(UITheme.fontMono(Font.PLAIN, 9));
        xpLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(xpLabel);

        return panel;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void refresh() {
        int lvl   = profile.getCurrentLevel();
        int inLvl = profile.getXPInCurrentLevel();
        int span  = profile.getXPSpanOfCurrentLevel();
        int total = profile.getTotalXP();

        titleLabel.setText("Lv." + lvl + " · " + profile.getLevelTitle());
        xpBar.setMaximum(Math.max(span, 1));
        xpBar.setValue(Math.min(inLvl, span));

        if (lvl >= LevelConfig.MAX_LEVEL) {
            xpLabel.setText("XP: " + total + "  ✦ MASTER");
        } else {
            xpLabel.setText("XP: " + inLvl + " / " + span + " (need " + profile.getXPToNextLevel() + ")");
        }
        updateTooltip();
        repaint();
    }

    private void updateTooltip() {
        int lvl = profile.getCurrentLevel();
        if (lvl >= LevelConfig.MAX_LEVEL) {
            setToolTipText("⭐ Master Automator achieved — click to view career roadmap");
            return;
        }
        LevelConfig.LevelEntry next = LevelConfig.getEntry(lvl + 1);
        setToolTipText("<html><b>Next: Lv." + next.level + " — " + next.title + "</b><br>"
            + next.description + "<br><i>" + profile.getXPToNextLevel()
            + " XP to go · click to view the full roadmap</i></html>");
    }

    public void showXpGain(int amount) {
        if (amount <= 0) return;
        popupXpAmount = amount;
        popupAlpha = 1f;
        popupRise  = 0;
        if (popupTimer != null && popupTimer.isRunning()) popupTimer.stop();
        popupTimer = new Timer(30, null);
        popupTimer.addActionListener(e -> {
            popupRise  += 1;
            popupAlpha -= 0.045f;
            if (popupAlpha <= 0f) {
                popupAlpha = 0f;
                popupTimer.stop();
            }
            repaint();
        });
        popupTimer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (popupAlpha <= 0f) return;
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityHints(g2);
        g2.setFont(UITheme.font(Font.BOLD, 11));
        String text = "+" + popupXpAmount + " XP";
        int tx = 12;
        int ty = 18 - popupRise;

        g2.setColor(new Color(0, 0, 0, (int) (120 * popupAlpha)));
        g2.drawString(text, tx + 1, ty + 1);
        g2.setColor(new Color(52, 211, 153, (int) (255 * popupAlpha)));
        g2.drawString(text, tx, ty);
        g2.dispose();
    }

    public void triggerLevelUpFlash() {
        if (flashTimer != null && flashTimer.isRunning()) flashTimer.stop();
        flashAlpha = 0.9f;
        flashTimer = new Timer(35, null);
        flashTimer.addActionListener(e -> {
            flashAlpha -= 0.05f;
            if (flashAlpha <= 0f) {
                flashAlpha = 0f;
                flashTimer.stop();
            }
            repaint();
        });
        flashTimer.start();
    }
}
