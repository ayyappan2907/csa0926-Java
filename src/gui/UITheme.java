package gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.io.InputStream;

/**
 * UITheme — Central Design System for Factory Auto Chain.
 * 
 * Provides:
 *  - Google Fonts (Inter) loading and fallback typography
 *  - Premium glassmorphism rendering (translucency, specular highlights, neon glow)
 *  - Cyberpunk & Sci-Fi gradient palette
 *  - Custom GlassScrollBarUI and GlassComboBoxUI
 *  - Micro-animation interpolation and rendering hints
 */
public final class UITheme {

    private UITheme() {}

    // ── Font Family ───────────────────────────────────────────────────────────
    private static Font FONT_INTER_BASE = null;
    private static String FONT_FAMILY = "Segoe UI";

    public static void init() {
        try {
            // Attempt to load Inter TTF from file or classpath
            File fontFile = new File("resources/fonts/Inter-Variable.ttf");
            if (fontFile.exists()) {
                FONT_INTER_BASE = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FONT_INTER_BASE);
                FONT_FAMILY = FONT_INTER_BASE.getFamily();
                System.out.println("[THEME] Google Fonts Inter loaded successfully: " + FONT_FAMILY);
            } else {
                InputStream is = UITheme.class.getResourceAsStream("/fonts/Inter-Variable.ttf");
                if (is != null) {
                    FONT_INTER_BASE = Font.createFont(Font.TRUETYPE_FONT, is);
                    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FONT_INTER_BASE);
                    FONT_FAMILY = FONT_INTER_BASE.getFamily();
                }
            }
        } catch (Exception e) {
            System.out.println("[THEME] Inter font not found on disk, using system fallback: " + e.getMessage());
            FONT_FAMILY = "Segoe UI";
        }

        // Apply dark look and feel defaults
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background",             BG_BASE);
            UIManager.put("ScrollPane.background",        BG_BASE);
            UIManager.put("Viewport.background",          BG_BASE);
            UIManager.put("OptionPane.background",        BG_CARD_SOLID);
            UIManager.put("OptionPane.messageForeground", FG_PRIMARY);
            UIManager.put("ToolTip.background",           BG_CARD_SOLID);
            UIManager.put("ToolTip.foreground",           FG_PRIMARY);
            UIManager.put("ToolTip.border",               BorderFactory.createLineBorder(BORDER_GLASS, 1));
        } catch (Exception ignored) {}
    }

    public static Font font(int style, float size) {
        if (FONT_INTER_BASE != null) {
            return FONT_INTER_BASE.deriveFont(style, size);
        }
        return new Font(FONT_FAMILY, style, (int) size);
    }

    public static Font fontEmoji(int style, float size) {
        return new Font("Segoe UI Emoji", style, (int) size);
    }

    public static Font fontMono(int style, float size) {
        return new Font("Consolas", style, (int) size);
    }

    // ── Color Palette ─────────────────────────────────────────────────────────
    // Primary & Brand Colors
    public static final Color BRAND_PRIMARY_BLUE     = new Color(0x00, 0x77, 0xFF); // #0077FF - Main CTA buttons, brand logo, key links
    public static final Color BRAND_PRIMARY_BLUE_ALT = new Color(0x00, 0x84, 0xFF); // #0084FF - Alternative primary blue
    public static final Color BRAND_VIVID_SKY_BLUE   = new Color(0x00, 0xB7, 0xFE); // #00B7FE - Hero gradient banner, active highlights, background accents

    // Accent & Data Colors
    public static final Color ACCENT_WARNING_ORANGE  = new Color(0xF9, 0x91, 0x1E); // #F9911E - Notification badges, callouts, chart data points
    public static final Color ACCENT_LIGHT_BLUE_TINT = new Color(0xEA, 0xF3, 0xFF); // #EAF3FF - Active navigation item backgrounds, pill tags

    // Neutrals & Backgrounds
    public static final Color NEUTRAL_BASE_WHITE     = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF - Primary card backgrounds, main content canvas
    public static final Color NEUTRAL_OFF_WHITE      = new Color(0xF4, 0xF7, 0xFb); // #F4F7FB - Sidebar background, search input fill, page backdrop
    public static final Color NEUTRAL_BORDER_GRAY    = new Color(0xE5, 0xEC, 0xF2); // #E5ECF2 - Card borders, dividers, tag outlines

    // Typography
    public static final Color TEXT_PRIMARY_DARK      = new Color(0x1A, 0x1D, 0x24); // #1A1D24 - High-contrast dark tone for headings & titles
    public static final Color TEXT_SECONDARY_MUTED   = new Color(0x8F, 0x9C, 0xAE); // #8F9CAE - Mid-tone gray for inactive nav, metadata, subtitles

    // Dark void background layers
    public static final Color BG_VOID         = new Color(8, 11, 20);
    public static final Color BG_BASE         = new Color(12, 16, 28);
    public static final Color BG_CARD_SOLID    = new Color(18, 24, 42);
    public static final Color BG_CARD_ALT      = new Color(24, 32, 54);

    // Glassmorphic translucent colors
    public static final Color GLASS_BG         = new Color(18, 26, 46, 200);
    public static final Color GLASS_BG_HOVER   = new Color(28, 38, 68, 230);
    public static final Color GLASS_BG_ACTIVE  = new Color(35, 48, 85, 245);
    public static final Color GLASS_CARD       = new Color(22, 30, 52, 180);
    public static final Color GLASS_CARD_HOVER = new Color(30, 42, 72, 220);

    // Glass borders & specular highlights
    public static final Color BORDER_GLASS     = new Color(60, 90, 150, 90);
    public static final Color BORDER_GLOW      = new Color(0x00, 0x77, 0xFF, 180);
    public static final Color SPECULAR_TOP     = new Color(255, 255, 255, 40);
    public static final Color SPECULAR_INNER   = new Color(255, 255, 255, 18);

    // Neon Accent Colors
    public static final Color NEON_GOLD        = new Color(250, 185, 45);
    public static final Color NEON_GOLD_LIGHT  = new Color(255, 215, 105);
    public static final Color NEON_GOLD_GLOW   = new Color(250, 185, 45, 80);

    public static final Color NEON_BLUE        = BRAND_VIVID_SKY_BLUE;
    public static final Color NEON_BLUE_LIGHT  = new Color(125, 211, 252);
    public static final Color NEON_BLUE_GLOW   = new Color(0x00, 0xB7, 0xFE, 90);

    public static final Color NEON_GREEN       = new Color(16, 185, 129);
    public static final Color NEON_GREEN_LIGHT = new Color(52, 211, 153);
    public static final Color NEON_GREEN_GLOW  = new Color(16, 185, 129, 80);

    public static final Color NEON_PURPLE      = new Color(168, 85, 247);
    public static final Color NEON_PURPLE_LIGHT= new Color(192, 132, 252);

    public static final Color NEON_RED         = new Color(239, 68, 68);
    public static final Color NEON_ORANGE      = ACCENT_WARNING_ORANGE;

    // Typography Colors
    public static final Color FG_PRIMARY       = new Color(225, 235, 250);
    public static final Color FG_SECONDARY     = TEXT_SECONDARY_MUTED;
    public static final Color FG_MUTED         = new Color(90, 105, 140);
    public static final Color FG_LOCKED        = new Color(75, 85, 110);

    // ── Rendering Hints ───────────────────────────────────────────────────────
    public static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ── Glassmorphism Drawing Helpers ─────────────────────────────────────────

    public static void drawGlassPanel(Graphics2D g2, int x, int y, int w, int h, int radius,
                                      Color fill, Color border, boolean withSpecular) {
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(x, y, w, h, radius, radius);

        // Ambient drop shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Float(x + 1, y + 2, w, h, radius, radius));

        // Glass background with vertical gradient
        GradientPaint gp = new GradientPaint(
            x, y, fill,
            x, y + h, new Color(
                Math.max(0, fill.getRed() - 10),
                Math.max(0, fill.getGreen() - 10),
                Math.max(0, fill.getBlue() - 10),
                fill.getAlpha()
            )
        );
        g2.setPaint(gp);
        g2.fill(shape);

        // Top specular highlight line
        if (withSpecular && h > 10) {
            g2.setColor(SPECULAR_TOP);
            g2.fill(new RoundRectangle2D.Float(x + 2, y + 1, w - 4, 3, radius, radius));
        }

        // Frosted border
        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1f, h - 1f, radius, radius));
        }
    }

    public static void drawNeonGlow(Graphics2D g2, Shape shape, Color glowColor, int radius) {
        for (int i = radius; i >= 1; i--) {
            float alpha = (float) (glowColor.getAlpha() / 255.0) * (1.0f - (float) i / (radius + 1));
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (alpha * 255)));
            g2.setStroke(new BasicStroke(i * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(shape);
        }
    }

    public static void drawPillBadge(Graphics2D g2, String text, int x, int y, Color bg, Color fg, Font font) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int padX = 8, padY = 3;
        int bw = tw + padX * 2;
        int bh = th + padY * 2;

        RoundRectangle2D.Float pill = new RoundRectangle2D.Float(x, y - fm.getAscent() - padY, bw, bh, bh, bh);
        g2.setColor(bg);
        g2.fill(pill);
        g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 220));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(pill);

        g2.setColor(fg);
        g2.drawString(text, x + padX, y);
    }

    public static void drawHeroGradientBanner(Graphics2D g2, int x, int y, int w, int h, int radius) {
        GradientPaint grad = new GradientPaint(
            x, y, BRAND_PRIMARY_BLUE,
            x + w, y + h, BRAND_VIVID_SKY_BLUE
        );
        g2.setPaint(grad);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
    }

    public static void drawPrimaryCTAButton(Graphics2D g2, int x, int y, int w, int h, int radius, boolean hover, boolean pressed) {
        Color base = pressed ? BRAND_PRIMARY_BLUE : (hover ? BRAND_PRIMARY_BLUE_ALT : BRAND_PRIMARY_BLUE);
        GradientPaint grad = new GradientPaint(
            x, y, base,
            x, y + h, new Color(Math.max(0, base.getRed() - 25), Math.max(0, base.getGreen() - 25), base.getBlue())
        );
        g2.setPaint(grad);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));

        if (hover) {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1f, h - 1f, radius, radius));
        }
    }

    public static void drawWarningAlertBadge(Graphics2D g2, String text, int x, int y) {
        drawPillBadge(g2, text, x, y, ACCENT_WARNING_ORANGE, NEUTRAL_BASE_WHITE, font(Font.BOLD, 10));
    }

    // ── Custom Glass ScrollBar UI ─────────────────────────────────────────────

    public static class GlassScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(80, 120, 200, 90);
            this.thumbHighlightColor = new Color(120, 170, 255, 180);
            this.trackColor = new Color(10, 14, 25, 120);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            g2.setColor(new Color(12, 16, 28, 80));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);

            boolean hovered = isThumbRollover();
            boolean dragging = isDragging;

            Color cThumb = dragging ? new Color(100, 160, 255, 200)
                         : hovered  ? new Color(90, 145, 240, 160)
                                    : new Color(70, 110, 190, 100);

            int arc = Math.min(thumbBounds.width, thumbBounds.height);
            int shrink = 2;
            int x = thumbBounds.x + shrink;
            int y = thumbBounds.y + shrink;
            int w = Math.max(thumbBounds.width - shrink * 2, 4);
            int h = Math.max(thumbBounds.height - shrink * 2, 4);

            RoundRectangle2D.Float r = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
            g2.setColor(cThumb);
            g2.fill(r);

            if (hovered || dragging) {
                g2.setColor(new Color(140, 190, 255, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(r);
            }
            g2.dispose();
        }
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setOpaque(false);
        sp.setBackground(BG_BASE);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(BG_BASE);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new GlassScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new GlassScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
    }

    // ── Custom Glass ComboBox UI ──────────────────────────────────────────────

    public static class GlassComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    applyQualityHints(g2);
                    int w = getWidth(), h = getHeight();
                    // Draw custom down chevron
                    g2.setColor(comboBox.isEnabled() ? NEON_BLUE : FG_MUTED);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = w / 2;
                    int cy = h / 2;
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(24, 24));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Handled in paint
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            int w = c.getWidth(), h = c.getHeight();
            boolean enabled = c.isEnabled();

            Color fill = enabled ? GLASS_CARD : new Color(16, 21, 35, 140);
            Color border = enabled ? (c.hasFocus() ? NEON_BLUE : BORDER_GLASS) : new Color(40, 50, 75);

            drawGlassPanel(g2, 0, 0, w, h, 8, fill, border, enabled);
            g2.dispose();

            super.paint(g, c);
        }
    }
}
