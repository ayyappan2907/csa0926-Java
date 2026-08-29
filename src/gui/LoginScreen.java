package gui;

import auth.GoogleAuthService;
import auth.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.Consumer;

/**
 * Full-window splash and Google sign-in screen shown before the game.
 *
 * Features:
 *  - Animated cosmic gear background with smooth rotational kinetics
 *  - Frosted glassmorphism authentication card with specular reflections
 *  - Google Fonts (Inter) typography
 *  - Polished Google authentication button with interactive feedback
 */
public class LoginScreen extends JFrame {

    private static final Color GOOGLE_BG = new Color(255, 255, 255);
    private static final Color GOOGLE_FG = new Color(60, 64, 67);

    private final Consumer<UserSession> onSuccess;
    private JLabel  statusLabel;
    private JButton googleBtn;
    private JPanel  spinnerPanel;
    private boolean authRunning = false;

    // Background animation
    private final Timer animTimer;
    private float gearAngle = 0f;
    private final BackgroundPanel bgPanel;

    public LoginScreen(Consumer<UserSession> onSuccess) {
        this.onSuccess = onSuccess;

        setTitle("Factory Auto Chain — Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(840, 580);
        setLocationRelativeTo(null);
        setBackground(UITheme.BG_VOID);

        bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new GridBagLayout());
        setContentPane(bgPanel);

        buildUI();

        // 60 FPS animation timer
        animTimer = new Timer(16, e -> {
            gearAngle += 0.4f;
            bgPanel.repaint();
        });
        animTimer.start();

        addDragSupport();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;

        // Glass card container
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                UITheme.drawGlassPanel(g2, 0, 0, getWidth(), getHeight(), 24,
                    new Color(18, 25, 48, 235), UITheme.BORDER_GLASS, true);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(48, 60, 44, 60));
        card.setPreferredSize(new Dimension(460, 440));

        // Animated Gear Icon with Glow
        JLabel gearLbl = new JLabel("⚙") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                for (int r = 50; r >= 30; r -= 5) {
                    g2.setColor(new Color(250, 185, 45, (50 - r) * 3));
                    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        gearLbl.setFont(UITheme.fontEmoji(Font.PLAIN, 56));
        gearLbl.setForeground(UITheme.NEON_GOLD);
        gearLbl.setAlignmentX(CENTER_ALIGNMENT);
        card.add(gearLbl);

        card.add(Box.createVerticalStrut(12));

        // App Title
        JLabel title = new JLabel("FACTORY AUTO CHAIN");
        title.setFont(UITheme.font(Font.BOLD, 24));
        title.setForeground(UITheme.NEON_GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Build · Automate · Scale");
        subtitle.setFont(UITheme.font(Font.PLAIN, 13));
        subtitle.setForeground(UITheme.FG_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(28));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_GLASS);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);

        card.add(Box.createVerticalStrut(28));

        // Google Sign-in Button
        googleBtn = buildGoogleButton();
        card.add(googleBtn);

        card.add(Box.createVerticalStrut(18));

        // Status & Spinner
        spinnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        spinnerPanel.setOpaque(false);
        spinnerPanel.setVisible(false);

        SpinnerIcon spinner = new SpinnerIcon();
        JLabel spinLbl = new JLabel(spinner);
        spinnerPanel.add(spinLbl);

        statusLabel = new JLabel("Connecting...");
        statusLabel.setFont(UITheme.font(Font.PLAIN, 12));
        statusLabel.setForeground(UITheme.NEON_BLUE_LIGHT);
        spinnerPanel.add(statusLabel);

        Timer spinTimer = new Timer(60, e -> {
            spinner.advance();
            spinLbl.repaint();
        });
        spinTimer.start();

        card.add(spinnerPanel);

        card.add(Box.createVerticalStrut(8));

        JLabel privacy = new JLabel("Fast, safe OAuth sign-in to sync your profile & unlocks");
        privacy.setFont(UITheme.font(Font.PLAIN, 10));
        privacy.setForeground(UITheme.FG_MUTED);
        privacy.setAlignmentX(CENTER_ALIGNMENT);
        card.add(privacy);

        bgPanel.add(card, gbc);

        // Frameless Close Button (top-right)
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(UITheme.font(Font.PLAIN, 14));
        closeBtn.setForeground(UITheme.FG_MUTED);
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        closeBtn.setToolTipText("Exit");

        JLayeredPane lp = getLayeredPane();
        lp.setLayout(null);
        closeBtn.setBounds(getWidth() - 44, 12, 32, 32);
        lp.add(closeBtn, JLayeredPane.POPUP_LAYER);
    }

    private JButton buildGoogleButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                boolean isHover = getModel().isRollover();

                g2.setColor(isHover ? new Color(245, 245, 245) : GOOGLE_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                g2.setColor(new Color(0, 0, 0, 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 48));
        btn.setMaximumSize(new Dimension(300, 48));
        btn.setAlignmentX(CENTER_ALIGNMENT);

        JLabel googleG = new JLabel(buildGoogleGHtml());
        googleG.setFont(UITheme.font(Font.BOLD, 18));

        JLabel text = new JLabel("Sign in with Google");
        text.setFont(UITheme.font(Font.BOLD, 14));
        text.setForeground(GOOGLE_FG);

        btn.add(googleG);
        btn.add(text);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });

        btn.addActionListener(e -> startAuth());
        return btn;
    }

    private String buildGoogleGHtml() {
        return "<html>"
            + "<span style='color:#4285F4'>G</span>"
            + "<span style='color:#EA4335'>o</span>"
            + "<span style='color:#FBBC05'>o</span>"
            + "<span style='color:#4285F4'>g</span>"
            + "<span style='color:#34A853'>l</span>"
            + "<span style='color:#EA4335'>e</span>"
            + "</html>";
    }

    private void startAuth() {
        if (authRunning) return;
        authRunning = true;

        googleBtn.setEnabled(false);
        spinnerPanel.setVisible(true);
        setStatus("Opening browser for OAuth sign-in...");

        Thread authThread = new Thread(() -> {
            GoogleAuthService service = new GoogleAuthService();
            UserSession session = service.authenticate(msg ->
                SwingUtilities.invokeLater(() -> setStatus(msg)));

            SwingUtilities.invokeLater(() -> {
                if (session != null) {
                    onAuthSuccess(session);
                } else {
                    onAuthFailed();
                }
            });
        }, "google-auth-thread");
        authThread.setDaemon(true);
        authThread.start();
    }

    private void onAuthSuccess(UserSession session) {
        setStatus("✅ Welcome, " + session.getGivenName() + "! Initializing factory...");
        spinnerPanel.setVisible(false);

        Timer delay = new Timer(1000, e -> {
            animTimer.stop();
            dispose();
            onSuccess.accept(session);
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void onAuthFailed() {
        authRunning = false;
        spinnerPanel.setVisible(false);
        setStatus("❌ Sign-in failed. Please try again.");
        googleBtn.setEnabled(true);
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
        spinnerPanel.setVisible(true);
    }

    private void addDragSupport() {
        final int[] startX = {0};
        final int[] startY = {0};
        bgPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                startX[0] = e.getX();
                startY[0] = e.getY();
            }
        });
        bgPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - startX[0],
                            loc.y + e.getY() - startY[0]);
            }
        });
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            GradientPaint gp = new GradientPaint(
                0, 0, UITheme.BG_VOID,
                0, getHeight(), new Color(14, 18, 36)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            drawDecorGear(g2, -60, -60, 180, (float) Math.toRadians(gearAngle * 0.3));
            drawDecorGear(g2, getWidth() - 100, getHeight() - 120, 220, (float) Math.toRadians(-gearAngle * 0.2));
            drawDecorGear(g2, getWidth() / 2 - 20, -30, 80, (float) Math.toRadians(gearAngle * 0.6));

            g2.setColor(new Color(255, 255, 255, 6));
            g2.setStroke(new BasicStroke(0.5f));
            int spacing = 40;
            for (int x = 0; x < getWidth(); x += spacing) g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += spacing) g2.drawLine(0, y, getWidth(), y);

            g2.dispose();
        }

        private void drawDecorGear(Graphics2D g2, int cx, int cy, int radius, float angle) {
            g2.setColor(new Color(250, 185, 45, 18));
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.translate(cx, cy);
            g3.rotate(angle);
            int teeth = 12;
            int innerR = (int) (radius * 0.65);
            int toothH = (int) (radius * 0.22);
            Polygon gear = buildGearShape(radius, innerR, teeth, toothH);
            g3.fill(gear);
            g3.setColor(new Color(250, 185, 45, 28));
            g3.setStroke(new BasicStroke(1.5f));
            g3.draw(gear);
            g3.fillOval(-innerR / 3, -innerR / 3, innerR * 2 / 3, innerR * 2 / 3);
            g3.dispose();
        }

        private Polygon buildGearShape(int outerR, int innerR, int teeth, int toothH) {
            Polygon p = new Polygon();
            double step = 2 * Math.PI / teeth;
            double halfTooth = step * 0.28;
            for (int i = 0; i < teeth; i++) {
                double base = i * step;
                p.addPoint((int) (innerR * Math.cos(base - halfTooth)),
                           (int) (innerR * Math.sin(base - halfTooth)));
                p.addPoint((int) ((innerR + toothH) * Math.cos(base - halfTooth * 0.5)),
                           (int) ((innerR + toothH) * Math.sin(base - halfTooth * 0.5)));
                p.addPoint((int) ((innerR + toothH) * Math.cos(base + halfTooth * 0.5)),
                           (int) ((innerR + toothH) * Math.sin(base + halfTooth * 0.5)));
                p.addPoint((int) (innerR * Math.cos(base + halfTooth)),
                           (int) (innerR * Math.sin(base + halfTooth)));
            }
            return p;
        }
    }

    private static class SpinnerIcon implements Icon {
        private int angle = 0;
        void advance() { angle = (angle + 30) % 360; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);
            g2.translate(x + 8, y + 8);
            g2.rotate(Math.toRadians(angle));
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 8; i++) {
                float alpha = 0.15f + 0.85f * i / 8f;
                g2.setColor(new Color(56, 189, 248, (int) (alpha * 255)));
                g2.drawLine(0, -5, 0, -8);
                g2.rotate(Math.toRadians(45));
            }
            g2.dispose();
        }

        @Override public int getIconWidth()  { return 16; }
        @Override public int getIconHeight() { return 16; }
    }
}
