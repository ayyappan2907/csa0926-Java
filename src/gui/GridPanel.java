package gui;

import controller.GameController;
import model.*;
import recipe.Resource;
import simulation.EfficiencyAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Grid Panel — Interactive Factory Canvas with Smooth Micro-Animations.
 *
 * Features:
 *  - Dynamic centering & scaling to eliminate empty gaps
 *  - Left-click to place or overwrite machine with active tool & direction
 *  - Left-drag across grid for continuous conveyor belt painting
 *  - Right-click / Double-click / Delete key to demolish machine at cell
 *  - 60 FPS Micro-animations: Flowing belt chevrons, pulsing cores, placement shockwaves
 */
public class GridPanel extends JPanel implements MouseListener, MouseMotionListener {

    private final GameController controller;
    private final SidePanel sidePanel;

    private int hoveredRow = -1;
    private int hoveredCol = -1;
    private int lastDraggedRow = -1;
    private int lastDraggedCol = -1;

    // Animation tick counter
    private long animTick = 0;

    // Placement pulse ripples: {row, col, age}
    private final List<int[]> placementPulses = new ArrayList<>();
    private static final int PULSE_LIFESPAN = 8;

    // Delivery particles moving into empty space / warehouse
    private final List<DeliveryParticle> deliveryParticles = new ArrayList<>();

    public static class DeliveryParticle {
        float startX, startY;
        float curX, curY;
        float targetX, targetY;
        Resource resource;
        long value;
        int age;
        int maxAge;

        public DeliveryParticle(float startX, float startY, float targetX, float targetY, Resource resource, long value, int maxAge) {
            this.startX = startX;
            this.startY = startY;
            this.curX = startX;
            this.curY = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.resource = resource;
            this.value = value;
            this.age = 0;
            this.maxAge = maxAge;
        }

        public boolean step() {
            age++;
            float t = Math.min(1.0f, (float) age / maxAge);
            float ease = 1.0f - (float) Math.pow(1.0f - t, 2.5);
            curX = startX + (targetX - startX) * ease;
            curY = startY + (targetY - startY) * ease - (float) Math.sin(t * Math.PI) * 14f;
            return age >= maxAge;
        }
    }

    public GridPanel(GameController controller, SidePanel sidePanel) {
        this.controller = controller;
        this.sidePanel  = sidePanel;

        Grid g = controller.getGrid();
        setPreferredSize(new Dimension(g.getCols() * 56 + 24, g.getRows() * 56 + 24));
        setBackground(UITheme.BG_VOID);

        addMouseListener(this);
        addMouseMotionListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);

        // Hook delivery callback from simulation to spawn particles across empty space
        controller.setDeliveryCallback((row, col, dir, res) -> spawnDeliveryParticle(row, col, dir, res));

        // 60 FPS Micro-Animation Timer (~16ms)
        Timer animTimer = new Timer(16, e -> {
            animTick++;
            for (Iterator<int[]> it = placementPulses.iterator(); it.hasNext(); ) {
                int[] p = it.next();
                p[2]++;
                if (p[2] > PULSE_LIFESPAN) it.remove();
            }
            synchronized (deliveryParticles) {
                for (Iterator<DeliveryParticle> it = deliveryParticles.iterator(); it.hasNext(); ) {
                    DeliveryParticle dp = it.next();
                    if (dp.step()) it.remove();
                }
            }
            repaint();
        });
        animTimer.start();
    }

    public void spawnDeliveryParticle(int row, int col, Direction dir, Resource res) {
        int cellSize = getCellSize();
        int startX = getStartX(cellSize);
        int startY = getStartY(cellSize);

        float sx = startX + col * cellSize + cellSize / 2f;
        float sy = startY + row * cellSize + cellSize / 2f;

        int[] delta = (dir != null) ? dir.getDelta() : new int[]{0, 1};
        int dy = delta[0];
        int dx = delta[1];
        float tx = sx + dx * cellSize * 1.6f + cellSize * 0.8f;
        float ty = sy + dy * cellSize * 1.6f;

        long val = economy.ItemValue.cashValue(res);
        synchronized (deliveryParticles) {
            if (deliveryParticles.size() < 35) {
                deliveryParticles.add(new DeliveryParticle(sx, sy, tx, ty, res, val, 38));
            }
        }
    }

    // ── Dynamic Layout Calculations ───────────────────────────────────────────

    private int getCellSize() {
        int availW = Math.max(getWidth() - 20, 100);
        int availH = Math.max(getHeight() - 20, 100);
        int byW = availW / controller.getGrid().getCols();
        int byH = availH / controller.getGrid().getRows();
        return Math.max(38, Math.min(62, Math.min(byW, byH)));
    }

    private int getStartX(int cellSize) {
        int gridW = controller.getGrid().getCols() * cellSize;
        return Math.max(10, (getWidth() - gridW) / 2);
    }

    private int getStartY(int cellSize) {
        int gridH = controller.getGrid().getRows() * cellSize;
        return Math.max(10, (getHeight() - gridH) / 2);
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityHints(g2);

        Grid grid = controller.getGrid();
        int cellSize = getCellSize();
        int startX = getStartX(cellSize);
        int startY = getStartY(cellSize);

        // 1. Cosmic Void Background Gradient
        GradientPaint bgGrad = new GradientPaint(
            0, 0, UITheme.BG_VOID,
            0, getHeight(), new Color(6, 8, 16)
        );
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 2. Factory Grid Frame (Glass canvas container)
        int gridW = grid.getCols() * cellSize;
        int gridH = grid.getRows() * cellSize;
        UITheme.drawGlassPanel(g2, startX - 2, startY - 2, gridW + 4, gridH + 4, 12,
            new Color(12, 16, 28, 180), UITheme.BORDER_GLASS, true);

        // 3. Grid Cells
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;
                Cell cell = grid.getCell(r, c);
                boolean selected = (r == controller.getSelectedRow() && c == controller.getSelectedCol());
                boolean hovered  = (r == hoveredRow && c == hoveredCol);

                EfficiencyAnalyzer.Status status = (cell.getMachineType() != MachineType.EMPTY)
                    ? EfficiencyAnalyzer.statusOf(cell, controller.getRecipeBook())
                    : null;

                MachineRenderer.paintCell(g2, cell, x, y, cellSize, cellSize,
                                         selected, hovered, status, animTick);
            }
        }

        // 4. Resource Bubbles floating on machines / belts
        paintResourceBubbles(g2, grid, startX, startY, cellSize);

        // 5. Placement shockwave ripples
        paintPlacementPulses(g2, startX, startY, cellSize);

        // 6. Flying delivery items moving to Warehouse across empty space
        paintDeliveryParticles(g2);

        g2.dispose();
    }

    private void paintDeliveryParticles(Graphics2D g2) {
        synchronized (deliveryParticles) {
            for (DeliveryParticle dp : deliveryParticles) {
                float t = (float) dp.age / dp.maxAge; // 0..1
                int alpha = Math.max(0, Math.min(255, (int) (255 * (1.0f - t * 0.85f))));

                Color resColor = dp.resource.getColor();
                // Glowing aura
                g2.setColor(new Color(resColor.getRed(), resColor.getGreen(), resColor.getBlue(), (int) (alpha * 0.35f)));
                g2.fillOval((int) dp.curX - 11, (int) dp.curY - 11, 22, 22);

                // Resource orb
                g2.setColor(new Color(resColor.getRed(), resColor.getGreen(), resColor.getBlue(), alpha));
                g2.fillOval((int) dp.curX - 6, (int) dp.curY - 6, 12, 12);
                g2.setColor(new Color(255, 255, 255, alpha));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval((int) dp.curX - 6, (int) dp.curY - 6, 12, 12);

                // Floating label
                String text = (dp.value > 0 ? "+$" + dp.value + " " : "") + dp.resource.getDisplayName() + " ➔ 🏪 Warehouse";
                g2.setFont(UITheme.font(Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int tx = (int) dp.curX - tw / 2;
                int ty = (int) dp.curY - 12;

                g2.setColor(new Color(10, 14, 26, (int) (alpha * 0.88f)));
                g2.fillRoundRect(tx - 4, ty - 10, tw + 8, 14, 6, 6);
                g2.setColor(new Color(UITheme.NEON_GOLD.getRed(), UITheme.NEON_GOLD.getGreen(), UITheme.NEON_GOLD.getBlue(), alpha));
                g2.drawString(text, tx, ty);
            }
        }
    }

    private void paintPlacementPulses(Graphics2D g2, int startX, int startY, int cellSize) {
        for (int[] p : placementPulses) {
            float t = p[2] / (float) PULSE_LIFESPAN; // 0..1
            int cx = startX + p[1] * cellSize + cellSize / 2;
            int cy = startY + p[0] * cellSize + cellSize / 2;

            int r = (int) (cellSize * 0.25f + t * cellSize * 0.65f);
            int alpha = Math.max(0, (int) (220 * (1f - t)));

            g2.setColor(new Color(250, 185, 45, alpha));
            g2.setStroke(new BasicStroke(2.5f * (1f - t * 0.5f)));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            int r2 = (int) (r * 0.6f);
            g2.setColor(new Color(56, 189, 248, (int) (alpha * 0.7f)));
            g2.drawOval(cx - r2, cy - r2, r2 * 2, r2 * 2);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void paintResourceBubbles(Graphics2D g2, Grid grid, int startX, int startY, int cellSize) {
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                Cell cell = grid.getCell(r, c);
                if (cell.getMachineType() == MachineType.EMPTY) continue;
                ArrayDeque<Resource> snap = cell.getBufferSnapshot();
                if (snap.isEmpty()) continue;

                int cx = startX + c * cellSize + cellSize / 2;
                int cy = startY + r * cellSize + cellSize / 2;

                int idx = 0;
                for (Resource res : snap) {
                    if (idx > 2) break;
                    double angle = (animTick * 0.08 + idx * Math.PI * 2.0 / 3.0);
                    int ox = (int) (Math.cos(angle) * 8);
                    int oy = (int) (Math.sin(angle) * 5);
                    MachineRenderer.paintResourceBubble(g2, res, cx + ox, cy + oy, 5);
                    idx++;
                }
            }
        }
    }

    // ── Mouse Events ──────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        int[] rc = pixelToCell(e.getX(), e.getY());
        if (rc == null) return;
        int row = rc[0], col = rc[1];

        // Right-Click or Double-Click: Demolish / Remove
        if (SwingUtilities.isRightMouseButton(e) || e.getClickCount() == 2) {
            controller.removeMachine(row, col);
            if (controller.getSelectedRow() == row && controller.getSelectedCol() == col) {
                controller.clearSelection();
            }
            repaint();
            return;
        }

        // Left-Click: Place / Overwrite machine with current tool & direction
        placeSelectedMachine(row, col);
        repaint();
    }

    private void placeSelectedMachine(int row, int col) {
        MachineType selected = sidePanel.getSelectedMachineType();
        if (selected != MachineType.EMPTY) {
            boolean placed = controller.placeMachine(row, col, selected);
            if (!placed) {
                long price = controller.getShop().currentPrice(selected);
                if (controller.getWallet().getBalance() < price) {
                    JOptionPane.showMessageDialog(this, "Not enough cash! Need $" + price, "Purchase Failed", JOptionPane.WARNING_MESSAGE);
                }
                return; // Do not update direction or selection if placement failed
            }
            controller.setOutputDirection(row, col, sidePanel.getSelectedDirection());
            if (selected == MachineType.EXTRACTOR) {
                controller.setExtractorResource(row, col, sidePanel.getSelectedResource());
            }
            controller.setSelected(row, col);
            placementPulses.add(new int[]{row, col, 0});
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int[] rc = pixelToCell(e.getX(), e.getY());
        if (rc != null) {
            lastDraggedRow = rc[0];
            lastDraggedCol = rc[1];
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        int[] rc = pixelToCell(e.getX(), e.getY());
        if (rc == null) return;
        int row = rc[0], col = rc[1];

        if (row != lastDraggedRow || col != lastDraggedCol) {
            if (sidePanel.getSelectedMachineType() == MachineType.BELT && lastDraggedRow >= 0) {
                int dr = row - lastDraggedRow;
                int dc = col - lastDraggedCol;
                if (Math.abs(dc) >= Math.abs(dr)) {
                    sidePanel.setDirection(dc >= 0 ? Direction.EAST : Direction.WEST);
                } else {
                    sidePanel.setDirection(dr >= 0 ? Direction.SOUTH : Direction.NORTH);
                }
            }
            placeSelectedMachine(row, col);
            lastDraggedRow = row;
            lastDraggedCol = col;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int[] rc = pixelToCell(e.getX(), e.getY());
        if (rc == null) {
            hoveredRow = -1;
            hoveredCol = -1;
        } else {
            hoveredRow = rc[0];
            hoveredCol = rc[1];
        }
        repaint();
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int[] rc = pixelToCell(e.getX(), e.getY());
        if (rc == null) return null;
        Cell cell = controller.getGrid().getCell(rc[0], rc[1]);
        if (cell == null || cell.getMachineType() == MachineType.EMPTY) return null;

        EfficiencyAnalyzer.Status status = EfficiencyAnalyzer.statusOf(cell, controller.getRecipeBook());
        String name = cell.getMachineType().getDisplayName();
        String dirArrow = cell.getOutputDirection().getArrow();

        switch (status) {
            case ACTIVE:
                return cell.getMachineType() == MachineType.EXTRACTOR
                    ? "<html><b>" + name + "</b> (" + dirArrow + ")<br>⛏ Extracting " + cell.getExtractorResource().getDisplayName() + "</html>"
                    : "<html><b>" + name + "</b> (" + dirArrow + ")<br>⚡ Active</html>";
            case STARVED:
                return "<html><b>" + name + "</b> (" + dirArrow + ")<br>⏳ <font color='#F59E0B'>Waiting on inputs</font></html>";
            case BLOCKED:
                return "<html><b>" + name + "</b> (" + dirArrow + ")<br>🚫 <font color='#EF4444'>Downstream blocked</font></html>";
            case IDLE:
                return "<html><b>" + name + "</b> (" + dirArrow + ")<br>⏸ Idle</html>";
            default:
                return name;
        }
    }

    @Override public void mouseReleased(MouseEvent e) { lastDraggedRow = -1; lastDraggedCol = -1; }
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   { hoveredRow = -1; hoveredCol = -1; repaint(); }

    private int[] pixelToCell(int px, int py) {
        int cellSize = getCellSize();
        int startX = getStartX(cellSize);
        int startY = getStartY(cellSize);

        int col = (px - startX) / cellSize;
        int row = (py - startY) / cellSize;
        if (px >= startX && py >= startY && controller.getGrid().isValid(row, col)) {
            return new int[]{row, col};
        }
        return null;
    }
}
