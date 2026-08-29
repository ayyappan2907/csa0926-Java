package gui;

import controller.GameController;
import economy.ItemValue;
import economy.StorageSilo;
import recipe.Resource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Warehouse Tab — Sleek Glassmorphism Storage Dashboard.
 * 
 * Displays live inventory racks, rolling throughput, unit valuations,
 * and automated export telemetry with custom-rendered progress bars.
 */
public class WarehousePanel extends JPanel {

    private final GameController controller;
    private final JLabel walletLabel;
    private final JLabel rateLabel;
    private final JLabel exportLabel;
    
    private final Map<Resource, ResourceRackView> rackViews = new LinkedHashMap<>();
    private int sessionSold = 0;

    public WarehousePanel(GameController controller) {
        this.controller = controller;
        setBackground(UITheme.BG_VOID);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Header: Wallet & Revenue Rate ─────────────────────────────────────
        JPanel headerCard = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 10,
                    new Color(16, 22, 38, 230), UITheme.BORDER_GLASS, true);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerCard.setOpaque(false);
        headerCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        walletLabel = new JLabel("$0");
        walletLabel.setFont(UITheme.font(Font.BOLD, 18));
        walletLabel.setForeground(UITheme.NEON_GOLD);

        rateLabel = new JLabel("+$0.0/s");
        rateLabel.setFont(UITheme.font(Font.BOLD, 12));
        rateLabel.setForeground(UITheme.BRAND_VIVID_SKY_BLUE);

        JLabel walletTitle = new JLabel("TOTAL BALANCE");
        walletTitle.setFont(UITheme.font(Font.BOLD, 9));
        walletTitle.setForeground(UITheme.TEXT_SECONDARY_MUTED);

        JLabel rateTitle = new JLabel("FLOW RATE");
        rateTitle.setFont(UITheme.font(Font.BOLD, 9));
        rateTitle.setForeground(UITheme.TEXT_SECONDARY_MUTED);
        rateTitle.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel leftCol = new JPanel(new GridLayout(2, 1, 0, 2));
        leftCol.setOpaque(false);
        leftCol.add(walletTitle);
        leftCol.add(walletLabel);

        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 2));
        rightCol.setOpaque(false);
        rightCol.add(rateTitle);
        rightCol.add(rateLabel);

        headerCard.add(leftCol, BorderLayout.WEST);
        headerCard.add(rightCol, BorderLayout.EAST);
        add(headerCard);
        add(Box.createVerticalStrut(10));

        // ── Resource Racks ────────────────────────────────────────────────────
        Resource[] resList = {
            Resource.IRON_ORE, Resource.COAL, Resource.IRON_PLATE,
            Resource.STEEL, Resource.CIRCUIT, Resource.GEAR, Resource.COMPUTER
        };

        for (Resource r : resList) {
            ResourceRackView view = new ResourceRackView(r);
            rackViews.put(r, view);
            add(view);
            add(Box.createVerticalStrut(6));
        }

        add(Box.createVerticalGlue());

        // ── Footer: Auto-Sell Status ──────────────────────────────────────────
        JPanel footerCard = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 8,
                    new Color(14, 18, 30, 210), UITheme.BORDER_GLASS, false);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        footerCard.setOpaque(false);
        footerCard.setBorder(new EmptyBorder(8, 10, 8, 10));
        footerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel autoSellLbl = new JLabel("● Auto-Sell: ACTIVE");
        autoSellLbl.setFont(UITheme.font(Font.BOLD, 10));
        autoSellLbl.setForeground(UITheme.NEON_GREEN);

        exportLabel = new JLabel("0 items sold");
        exportLabel.setFont(UITheme.font(Font.PLAIN, 10));
        exportLabel.setForeground(UITheme.FG_SECONDARY);
        exportLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        footerCard.add(autoSellLbl, BorderLayout.WEST);
        footerCard.add(exportLabel, BorderLayout.EAST);
        add(footerCard);
    }

    public void refresh(int sessionSoldTotal) {
        this.sessionSold = sessionSoldTotal;
        walletLabel.setText(controller.getWallet().formatBalance());

        Map<String, StorageSilo> silos = controller.getSilos();
        double totalCashRate = 0;

        for (Resource r : rackViews.keySet()) {
            int cap = 0;
            int stock = 0;
            double thru = 0;

            for (StorageSilo silo : silos.values()) {
                cap += silo.getCapacity(r);
                stock += silo.getStock(r);
                thru += silo.throughputPerSecond();
            }
            if (cap == 0) cap = 1000;

            ResourceRackView view = rackViews.get(r);
            view.updateMetrics(stock, cap, thru);
            totalCashRate += thru * ItemValue.cashValue(r);
        }

        rateLabel.setText(String.format("+$%.1f/s", totalCashRate));
        exportLabel.setText(String.format("%,d items sold", sessionSold));
    }

    // ── Custom Resource Rack Card View ────────────────────────────────────────

    private static class ResourceRackView extends JPanel {
        private final Resource resource;
        private int stock = 0;
        private int capacity = 1000;
        private double throughput = 0.0;

        private final JLabel nameLabel;
        private final JLabel valLabel;
        private final JLabel stockLabel;
        private final JLabel flowLabel;
        private final CustomFillBar fillBar;

        public ResourceRackView(Resource resource) {
            this.resource = resource;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(6, 8, 6, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

            // Top Row: [Emoji + Name] on left, [$X ea] on right
            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            nameLabel = new JLabel(resource.getEmoji() + " " + resource.getDisplayName());
            nameLabel.setFont(UITheme.font(Font.BOLD, 11));
            nameLabel.setForeground(UITheme.FG_PRIMARY);

            long val = ItemValue.cashValue(resource);
            valLabel = new JLabel("$" + val + " ea");
            valLabel.setFont(UITheme.font(Font.BOLD, 10));
            valLabel.setForeground(UITheme.NEON_GOLD);

            topRow.add(nameLabel, BorderLayout.WEST);
            topRow.add(valLabel, BorderLayout.EAST);
            add(topRow);
            add(Box.createVerticalStrut(4));

            // Middle: Custom Rounded Progress Fill Bar
            fillBar = new CustomFillBar(resource.getColor());
            fillBar.setPreferredSize(new Dimension(100, 6));
            fillBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
            add(fillBar);
            add(Box.createVerticalStrut(4));

            // Bottom Row: [Stock: X / Cap] on left, [Flow: Y/s] on right
            JPanel bottomRow = new JPanel(new BorderLayout());
            bottomRow.setOpaque(false);

            stockLabel = new JLabel("0 / 1,000");
            stockLabel.setFont(UITheme.font(Font.PLAIN, 9));
            stockLabel.setForeground(UITheme.TEXT_SECONDARY_MUTED);

            flowLabel = new JLabel("0.0/s");
            flowLabel.setFont(UITheme.font(Font.PLAIN, 9));
            flowLabel.setForeground(UITheme.BRAND_VIVID_SKY_BLUE);

            bottomRow.add(stockLabel, BorderLayout.WEST);
            bottomRow.add(flowLabel, BorderLayout.EAST);
            add(bottomRow);
        }

        public void updateMetrics(int stock, int capacity, double throughput) {
            this.stock = stock;
            this.capacity = Math.max(1, capacity);
            this.throughput = throughput;

            stockLabel.setText(String.format("%,d / %,d", stock, capacity));
            flowLabel.setText(String.format("%.1f/s", throughput));
            fillBar.setValue((float) stock / capacity);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);
            boolean nearFull = (float) stock / capacity >= 0.9f;
            Color border = nearFull ? UITheme.NEON_RED : UITheme.BORDER_GLASS;
            Color fill = nearFull ? new Color(38, 16, 20, 180) : new Color(16, 22, 38, 160);
            UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 8, fill, border, false);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Custom Sleek Progress Fill Bar ────────────────────────────────────────

    private static class CustomFillBar extends JComponent {
        private final Color accentColor;
        private float fraction = 0f;

        public CustomFillBar(Color accentColor) {
            this.accentColor = accentColor;
            setOpaque(false);
        }

        public void setValue(float fraction) {
            this.fraction = Math.max(0f, Math.min(1f, fraction));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();
            int arc = Math.min(w, h);

            // Dark Track Background
            g2.setColor(new Color(8, 12, 22, 220));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.setColor(new Color(40, 55, 85, 120));
            g2.setStroke(new BasicStroke(0.8f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 0.5f, h - 0.5f, arc, arc));

            // Colored Gradient Fill
            if (fraction > 0.005f) {
                int fillW = Math.max(arc, (int) (w * fraction));
                GradientPaint gp = new GradientPaint(
                    0, 0, accentColor,
                    fillW, 0, new Color(
                        Math.min(255, accentColor.getRed() + 40),
                        Math.min(255, accentColor.getGreen() + 40),
                        Math.min(255, accentColor.getBlue() + 40)
                    )
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, arc, arc));

                // Specular sheen
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h / 2f, arc, arc));
            }

            g2.dispose();
        }
    }
}
