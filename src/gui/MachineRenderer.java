package gui;

import model.*;
import recipe.Resource;
import simulation.EfficiencyAnalyzer;

import java.awt.*;
import java.awt.geom.*;

/**
 * Machine Renderer — Premium Glassmorphism & Cyberpunk Design.
 * 
 * Features:
 *  - Frosted glass machine chassis with specular top sheen
 *  - Machine-specific glowing neon accent borders
 *  - Animated flowing belt chevrons (visualizing speed & flow direction)
 *  - Pulsing core glow for Extractors
 *  - Orbital energy arc for Processors and Combiners
 *  - Status beacon for Active, Starved, and Blocked states
 *  - Double-ring neon selection effect with smooth hover glow
 */
public class MachineRenderer {

    // ── Machine Accent Palette ────────────────────────────────────────────────
    public static final Color C_EXTRACTOR  = new Color(249, 115, 22);   // Warm Neon Amber/Orange
    public static final Color C_BELT       = new Color(250, 185, 45);   // Neon Gold
    public static final Color C_PROCESSOR  = new Color(56, 189, 248);   // Cyber Blue
    public static final Color C_COMBINER   = new Color(16, 185, 129);   // Emerald Mint

    // Grid Colors
    public static final Color BG_EMPTY       = new Color(14, 18, 30);
    public static final Color BG_EMPTY_HOVER = new Color(22, 30, 52);
    public static final Color GRID_LINE      = new Color(30, 42, 68);
    public static final Color GRID_DOT       = new Color(50, 70, 110, 80);

    public static void paintCell(Graphics2D g2, Cell cell, int x, int y, int w, int h,
                                 boolean selected, boolean hovered, EfficiencyAnalyzer.Status status,
                                 long animTick) {
        UITheme.applyQualityHints(g2);
        MachineType type = cell.getMachineType();

        // ── Empty Cell ────────────────────────────────────────────────────────
        if (type == MachineType.EMPTY) {
            paintEmptyCell(g2, x, y, w, h, hovered);
            return;
        }

        // ── Placed Machine Cell ───────────────────────────────────────────────
        Color base = machineColor(type);
        Color baseDark = new Color(
            Math.max(0, base.getRed() / 4),
            Math.max(0, base.getGreen() / 4),
            Math.max(0, base.getBlue() / 4),
            230
        );

        // 1. Drop shadow & chassis background
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(x + 2, y + 3, w - 4, h - 4, 12, 12);

        // Glass background gradient
        GradientPaint gp = new GradientPaint(
            x, y, new Color(20, 26, 44, 240),
            x, y + h, baseDark
        );
        g2.setPaint(gp);
        g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 12, 12);

        // 2. Specular top rim
        g2.setColor(new Color(255, 255, 255, hovered ? 45 : 28));
        g2.fillRoundRect(x + 3, y + 2, w - 6, (h - 4) / 3, 10, 10);

        // 3. Accent border
        g2.setColor(hovered ? base.brighter() : new Color(base.getRed(), base.getGreen(), base.getBlue(), 200));
        g2.setStroke(new BasicStroke(hovered ? 2.2f : 1.6f));
        g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        // 4. Machine type tag (top-left pill)
        paintMachineTag(g2, type, base, x + 6, y + 6);

        // 5. Machine-specific core graphics
        switch (type) {
            case EXTRACTOR:
                paintExtractorCore(g2, cell, x, y, w, h, animTick);
                break;
            case BELT:
            case CONVEYOR:
                paintBeltChevrons(g2, cell.getOutputDirection(), x, y, w, h, animTick);
                break;
            case PROCESSOR:
                paintProcessorCore(g2, cell, x, y, w, h, base, animTick);
                break;
            case COMBINER:
            case ADVANCED_COMBINER:
                paintCombinerCore(g2, cell, x, y, w, h, base, animTick);
                break;
            default:
                break;
        }

        // 6. Direction Arrow Indicator (bottom-right corner)
        paintDirectionArrow(g2, cell.getOutputDirection(), x + w - 16, y + h - 16, 12);

        // 7. Buffer gauge bar (bottom strip)
        paintBufferBar(g2, cell, x, y, w, h);

        // 8. Operational status indicator beacon
        paintStatusBeacon(g2, status, x + w - 10, y + 8, animTick);

        // 9. Outer selection / hover glows
        if (selected) {
            // Neon pulse selection aura
            g2.setColor(new Color(250, 185, 45, 60));
            g2.setStroke(new BasicStroke(5f));
            g2.drawRoundRect(x, y, w, h, 14, 14);

            g2.setColor(UITheme.NEON_GOLD);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 12, 12);
            g2.setStroke(new BasicStroke(1f));
        } else if (hovered) {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 80));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 12, 12);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private static void paintEmptyCell(Graphics2D g2, int x, int y, int w, int h, boolean hovered) {
        g2.setColor(hovered ? BG_EMPTY_HOVER : BG_EMPTY);
        g2.fillRect(x + 1, y + 1, w - 2, h - 2);

        // Subtle grid center dot
        g2.setColor(GRID_DOT);
        g2.fillOval(x + w / 2 - 1, y + h / 2 - 1, 3, 3);

        if (hovered) {
            // Guide crosshairs on hover
            g2.setColor(new Color(56, 189, 248, 90));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{3, 3}, 0));
            g2.drawLine(x + w / 2, y + 4, x + w / 2, y + h - 4);
            g2.drawLine(x + 4, y + h / 2, x + w - 4, y + h / 2);
            g2.setStroke(new BasicStroke(1f));

            // "+" placement hint
            g2.setColor(new Color(56, 189, 248, 160));
            g2.setFont(UITheme.font(Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            String plus = "+";
            g2.drawString(plus, x + (w - fm.stringWidth(plus)) / 2,
                          y + (h + fm.getAscent() - fm.getDescent()) / 2);
        }

        g2.setColor(GRID_LINE);
        g2.drawRect(x, y, w, h);
    }

    private static void paintMachineTag(Graphics2D g2, MachineType type, Color accent, int tx, int ty) {
        String label = shortLabel(type);
        g2.setFont(UITheme.font(Font.BOLD, 8));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);

        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
        g2.fillRoundRect(tx - 1, ty - 1, tw + 6, 11, 4, 4);

        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 230));
        g2.drawString(label, tx + 2, ty + 8);
    }

    private static void paintExtractorCore(Graphics2D g2, Cell cell, int x, int y, int w, int h, long animTick) {
        Resource res = cell.getExtractorResource();
        Color rc = res != null ? res.getColor() : new Color(180, 180, 180);
        int cx = x + w / 2;
        int cy = y + h / 2;

        // Animated pulse radius
        double pulse = 0.5 + 0.5 * Math.sin(animTick * 0.2);
        int glowR = (int) (12 + pulse * 4);

        // Core glow
        for (int r = glowR; r >= 6; r -= 2) {
            float alpha = 0.05f * (glowR - r);
            g2.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), (int) (alpha * 255)));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Core jewel
        g2.setColor(rc);
        g2.fillOval(cx - 7, cy - 7, 14, 14);

        // Specular point
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillOval(cx - 4, cy - 5, 4, 4);

        // Resource short name
        g2.setFont(UITheme.font(Font.BOLD, 8));
        g2.setColor(UITheme.FG_PRIMARY);
        String name = res != null ? res.getDisplayName() : "";
        if (name.length() > 5) name = name.substring(0, 5);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(name, cx - fm.stringWidth(name) / 2, cy + 18);
    }

    private static void paintBeltChevrons(Graphics2D g2, Direction dir, int x, int y, int w, int h, long animTick) {
        int cx = x + w / 2;
        int cy = y + h / 2;

        // Flow phase (0.0 to 1.0)
        float phase = (float) ((animTick % 8) / 8.0);

        // Center roller icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        FontMetrics fm = g2.getFontMetrics();
        String icon = "➡️";
        if (dir == Direction.NORTH) icon = "⬆️";
        else if (dir == Direction.SOUTH) icon = "⬇️";
        else if (dir == Direction.WEST) icon = "⬅️";

        g2.setColor(new Color(255, 255, 255, 180));
        g2.drawString(icon, cx - fm.stringWidth(icon) / 2, cy + 6);

        // Moving chevron flow lines
        g2.setColor(new Color(C_BELT.getRed(), C_BELT.getGreen(), C_BELT.getBlue(), 120));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int[] delta = dir.getDelta(); // [dr, dc] -> [dy, dx]
        int dx = delta[1], dy = delta[0];

        for (int i = -1; i <= 1; i++) {
            float off = (i + phase) * 10;
            int px = cx + (int) (dx * off);
            int py = cy + (int) (dy * off);
            if (px > x + 6 && px < x + w - 6 && py > y + 6 && py < y + h - 6) {
                g2.fillOval(px - 2, py - 2, 4, 4);
            }
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private static void paintProcessorCore(Graphics2D g2, Cell cell, int x, int y, int w, int h, Color base, long animTick) {
        int cx = x + w / 2;
        int cy = y + h / 2;

        // Machine icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        String icon = "⚙️";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawString(icon, cx - fm.stringWidth(icon) / 2, cy + 6);

        // Animated processing energy arc
        int prog = cell.getProcessingProgress();
        int arcR = 19;
        int ax = cx - arcR, ay = cy - arcR;

        // Track ring
        g2.setColor(new Color(255, 255, 255, 25));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(ax, ay, arcR * 2, arcR * 2, 0, 360);

        // Progress arc
        if (prog > 0) {
            g2.setColor(UITheme.NEON_BLUE);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int sweep = -(int) (360.0 * prog / 6.0);
            g2.drawArc(ax, ay, arcR * 2, arcR * 2, 90, sweep);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private static void paintCombinerCore(Graphics2D g2, Cell cell, int x, int y, int w, int h, Color base, long animTick) {
        int cx = x + w / 2;
        int cy = y + h / 2;

        // Machine icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        String icon = "🔬";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawString(icon, cx - fm.stringWidth(icon) / 2, cy + 6);

        // Double orbital rings
        int prog = cell.getProcessingProgress();
        int arcR = 19;
        int ax = cx - arcR, ay = cy - arcR;

        g2.setColor(new Color(255, 255, 255, 25));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(ax, ay, arcR * 2, arcR * 2, 0, 360);

        if (prog > 0) {
            g2.setColor(UITheme.NEON_GREEN);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int sweep = -(int) (360.0 * prog / 6.0);
            g2.drawArc(ax, ay, arcR * 2, arcR * 2, 90, sweep);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private static void paintDirectionArrow(Graphics2D g2, Direction dir, int x, int y, int size) {
        int cx = x + size / 2;
        int cy = y + size / 2;

        g2.setColor(new Color(15, 20, 35, 180));
        g2.fillOval(x - 2, y - 2, size + 4, size + 4);

        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw clean vector arrow
        switch (dir) {
            case NORTH:
                g2.drawLine(cx, cy + 4, cx, cy - 4);
                g2.drawLine(cx - 3, cy - 1, cx, cy - 4);
                g2.drawLine(cx + 3, cy - 1, cx, cy - 4);
                break;
            case SOUTH:
                g2.drawLine(cx, cy - 4, cx, cy + 4);
                g2.drawLine(cx - 3, cy + 1, cx, cy + 4);
                g2.drawLine(cx + 3, cy + 1, cx, cy + 4);
                break;
            case WEST:
                g2.drawLine(cx + 4, cy, cx - 4, cy);
                g2.drawLine(cx - 1, cy - 3, cx - 4, cy);
                g2.drawLine(cx - 1, cy + 3, cx - 4, cy);
                break;
            case EAST:
            default:
                g2.drawLine(cx - 4, cy, cx + 4, cy);
                g2.drawLine(cx + 1, cy - 3, cx + 4, cy);
                g2.drawLine(cx + 1, cy + 3, cx + 4, cy);
                break;
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private static void paintBufferBar(Graphics2D g2, Cell cell, int x, int y, int w, int h) {
        int bufSz  = cell.getBufferSize();
        int maxBuf = Cell.getMaxBuffer();

        int barX = x + 6;
        int barY = y + h - 7;
        int barW = w - 12;
        int barH = 3;

        // Dark track
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(barX, barY, barW, barH, 2, 2);

        if (bufSz > 0) {
            int fillW = (int) (barW * ((double) bufSz / maxBuf));
            Color bufCol = bufSz >= maxBuf
                ? UITheme.NEON_RED
                : UITheme.NEON_GOLD;

            g2.setColor(bufCol);
            g2.fillRoundRect(barX, barY, fillW, barH, 2, 2);
        }
    }

    private static void paintStatusBeacon(Graphics2D g2, EfficiencyAnalyzer.Status status, int cx, int cy, long animTick) {
        if (status == null || status == EfficiencyAnalyzer.Status.IDLE) return;

        Color c;
        switch (status) {
            case ACTIVE:  c = UITheme.NEON_GREEN; break;
            case STARVED: c = UITheme.NEON_GOLD;  break;
            case BLOCKED: c = UITheme.NEON_RED;   break;
            default: return;
        }

        int r = 4;
        if (status != EfficiencyAnalyzer.Status.ACTIVE) {
            double pulse = 0.5 + 0.5 * Math.sin(animTick * 0.35);
            int glowR = (int) (r + 2 + pulse * 3);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
            g2.fillOval(cx - glowR, cy - glowR, glowR * 2, glowR * 2);
        }

        g2.setColor(c);
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 1, cy - 2, 2, 2);
    }

    public static void paintResourceBubble(Graphics2D g2, Resource res, int cx, int cy, int radius) {
        Color c = res.getColor();

        // Glow
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
        g2.fillOval(cx - radius - 2, cy - radius - 2, (radius + 2) * 2, (radius + 2) * 2);

        // Core orb
        GradientPaint gp = new GradientPaint(
            cx - radius, cy - radius, c.brighter(),
            cx + radius, cy + radius, c.darker()
        );
        g2.setPaint(gp);
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Highlight glint
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(cx - radius / 2, cy - radius / 2, radius, radius / 2);
    }

    public static Color machineColor(MachineType type) {
        switch (type) {
            case EXTRACTOR: return C_EXTRACTOR;
            case BELT:      return C_BELT;
            case PROCESSOR: return C_PROCESSOR;
            case COMBINER:  return C_COMBINER;
            case STORAGE_SILO: return new Color(130, 200, 130);
            case EXPORT_TERMINAL: return new Color(230, 140, 40);
            case ADVANCED_COMBINER: return new Color(160, 60, 210);
            case CONVEYOR: return new Color(240, 200, 50);
            default:        return Color.GRAY;
        }
    }

    private static String shortLabel(MachineType type) {
        switch (type) {
            case EXTRACTOR: return "EXTRACTOR";
            case BELT:      return "CONVEYOR";
            case PROCESSOR: return "PROCESSOR";
            case COMBINER:  return "COMBINER";
            case STORAGE_SILO: return "SILO";
            case EXPORT_TERMINAL: return "EXPORT";
            case ADVANCED_COMBINER: return "ADV. COMB.";
            case CONVEYOR: return "FAST CONV.";
            default:        return "";
        }
    }
}
