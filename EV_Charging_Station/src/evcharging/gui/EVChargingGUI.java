package evcharging.gui;

import evcharging.collection.ChargingManager;
import evcharging.model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

/**
 * Styled Swing GUI for EV Charging Station Booking.
 * Modern Dark Slate Theme with Cyan and Indigo Accents.
 */
public class EVChargingGUI extends JFrame {

    // Primary Header & Footer
    private static final Color primaryDark  = new Color(15, 23, 42);    // #0F172A (Deep Slate)
    private static final Color textWhite    = new Color(248, 250, 252); // #F8FAFC

    // Backgrounds
    private static final Color bodyBg       = new Color(30, 41, 59);    // #1E293B (Dark Slate Panel)
    private static final Color outputBg     = new Color(51, 65, 85);    // #334155 (Console Box)
    private static final Color textColor    = new Color(226, 232, 240); // #E2E8F0 (Text/Labels)

    // Action Buttons
    private static final Color bookBtnColor = new Color(14, 165, 233);  // #0EA5E9 (Cyan/Sky Blue)
    private static final Color billBtnColor = new Color(99, 102, 241);  // #6366F1 (Indigo)

    private static final Font F_TITLE = new Font("SansSerif", Font.BOLD,  20);
    private static final Font F_SUB   = new Font("SansSerif", Font.BOLD,  13);
    private static final Font F_BODY  = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font F_SMALL = new Font("SansSerif", Font.PLAIN, 11);

    private ChargingManager manager;
    private JComboBox<String> stationBox;
    private JComboBox<String> slotBox;
    private JTextField vehicleField;
    private JTextField customerIdField;
    private JTextField customerNameField;
    private JTextField unitsField;
    private JTextArea outputArea;
    private int bookingCounter = 1001;

    public EVChargingGUI(ChargingManager manager) {
        this.manager = manager;
        setTitle("EV Charging Station - SIMATS Engineering");
        setSize(860, 640);
        setMinimumSize(new Dimension(700, 540));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bodyBg);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primaryDark);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(outputBg);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel icon = new JLabel("EV");
        icon.setFont(new Font("SansSerif", Font.BOLD, 28));
        icon.setForeground(bookBtnColor);
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        JPanel textBox = new JPanel(new GridLayout(2, 1, 0, 2));
        textBox.setOpaque(false);
        JLabel title = new JLabel("EV Charging Station Slot Booking & Billing");
        title.setFont(F_TITLE);
        title.setForeground(textWhite);
        JLabel sub = new JLabel("SIMATS Engineering  |  CSA0926 Java Programming");
        sub.setFont(F_SMALL);
        sub.setForeground(textColor);
        textBox.add(title);
        textBox.add(sub);

        header.add(icon, BorderLayout.WEST);
        header.add(textBox, BorderLayout.CENTER);
        return header;
    }

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(),
            buildOutputPanel()
        );
        split.setDividerLocation(380);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setDividerSize(4);
        split.setBackground(bodyBg);
        return split;
    }

    private JScrollPane buildFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(bodyBg);
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        form.add(sectionLabel("Customer Details"));
        form.add(Box.createVerticalStrut(6));
        customerIdField   = styledField("e.g. U101");
        customerNameField = styledField("e.g. Customer One");
        form.add(labeledRow("Customer ID",   customerIdField));
        form.add(Box.createVerticalStrut(6));
        form.add(labeledRow("Customer Name", customerNameField));

        form.add(Box.createVerticalStrut(14));
        form.add(sectionLabel("Vehicle Details"));
        form.add(Box.createVerticalStrut(6));
        vehicleField = styledField("e.g. AP01AB1234");
        form.add(labeledRow("Vehicle Number", vehicleField));

        form.add(Box.createVerticalStrut(14));
        form.add(sectionLabel("Booking Details"));
        form.add(Box.createVerticalStrut(6));

        stationBox = new JComboBox<>();
        styleCombo(stationBox);
        for (ChargingStation s : manager.listStations()) {
            stationBox.addItem(s.getStationId() + " - " + s.getStationName() + " (" + s.getLocation() + ")");
        }
        form.add(labeledRow("Station", stationBox));
        form.add(Box.createVerticalStrut(6));

        slotBox = new JComboBox<>();
        styleCombo(slotBox);
        refreshSlots();
        form.add(labeledRow("Charging Slot", slotBox));

        form.add(Box.createVerticalStrut(14));
        form.add(sectionLabel("Billing"));
        form.add(Box.createVerticalStrut(6));
        unitsField = styledField("e.g. 15.5");
        form.add(labeledRow("Units (kWh)", unitsField));

        form.add(Box.createVerticalStrut(18));

        JButton bookBtn  = styledButton("Book Slot",       bookBtnColor, textWhite);
        JButton showBtn  = styledButton("Show Stations",   billBtnColor, textWhite);
        JButton billBtn  = styledButton("Calculate Bill",  billBtnColor, textWhite);
        JButton clearBtn = styledButton("Clear Output",    outputBg,     textColor);

        bookBtn.addActionListener(e  -> handleBook());
        showBtn.addActionListener(e  -> handleShowStations());
        billBtn.addActionListener(e  -> handleBill());
        clearBtn.addActionListener(e -> outputArea.setText(""));

        JPanel btns = new JPanel(new GridLayout(2, 2, 8, 8));
        btns.setOpaque(false);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        btns.add(bookBtn);
        btns.add(showBtn);
        btns.add(billBtn);
        btns.add(clearBtn);
        form.add(btns);
        form.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(bodyBg);
        return sp;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bodyBg);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));

        panel.add(sectionLabel("System Output"), BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBackground(outputBg);
        outputArea.setForeground(textColor);
        outputArea.setCaretColor(textWhite);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outputArea.setText("Welcome to EV Charging Station System\n" +
                           "------------------------------------\n" +
                           "Fill in the form and click Book Slot.\n");

        JScrollPane sp = new JScrollPane(outputArea);
        sp.setBorder(new LineBorder(outputBg.brighter(), 1, true));
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(primaryDark);
        footer.setPreferredSize(new Dimension(0, 28));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, outputBg),
            BorderFactory.createEmptyBorder(4, 16, 4, 16)
        ));
        JLabel status = new JLabel("SIMATS Engineering  |  EV Charging System  |  Java Assignment");
        status.setForeground(textColor);
        status.setFont(F_SMALL);
        footer.add(status, BorderLayout.WEST);
        JLabel date = new JLabel(LocalDate.now().toString());
        date.setForeground(textColor);
        date.setFont(F_SMALL);
        footer.add(date, BorderLayout.EAST);
        return footer;
    }

    private void handleShowStations() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n== Charging Stations ==\n");
        List<ChargingStation> list = manager.listStations();
        if (list.isEmpty()) {
            sb.append("  No stations registered.\n");
        } else {
            for (ChargingStation s : list) {
                sb.append(String.format(
                    "  [%d] %-22s %-12s %-14s Rs.%.2f/kWh%n",
                    s.getStationId(), s.getStationName(),
                    s.getLocation(), s.getChargerType(), s.getRatePerKwh()));
            }
        }
        sb.append("\n== Available Slots ==\n");
        List<ChargingSlot> slots = manager.listAvailableSlots();
        if (slots.isEmpty()) {
            sb.append("  No available slots.\n");
        } else {
            for (ChargingSlot sl : slots) {
                sb.append(String.format(
                    "  Slot %d | %-22s | Station %d%n",
                    sl.getSlotId(), sl.getSlotTime(),
                    sl.getStation().getStationId()));
            }
        }
        appendOutput(sb.toString());
    }

    private void handleBook() {
        String custId   = customerIdField.getText().trim();
        String custName = customerNameField.getText().trim();
        String vehNum   = vehicleField.getText().trim();

        if (custId.isEmpty() || custName.isEmpty() || vehNum.isEmpty()) {
            appendOutput("\n[ERROR] Please fill in all customer and vehicle fields.\n");
            return;
        }
        int stationIdx = stationBox.getSelectedIndex();
        int slotIdx    = slotBox.getSelectedIndex();
        if (stationIdx < 0 || slotIdx < 0) {
            appendOutput("\n[ERROR] Please select a station and slot.\n");
            return;
        }

        List<ChargingStation> stations = manager.listStations();
        List<ChargingSlot>    avSlots  = manager.listAvailableSlots();

        if (stationIdx >= stations.size() || slotIdx >= avSlots.size()) {
            appendOutput("\n[ERROR] Invalid selection.\n");
            return;
        }

        ChargingStation station  = stations.get(stationIdx);
        ChargingSlot    slot     = avSlots.get(slotIdx);
        Customer        customer = new Customer(custId, custName, custId + "@ev.in", "pw");
        Vehicle         vehicle  = new Vehicle(1, vehNum, "Electric Car", customer);

        int bid = bookingCounter++;
        evcharging.concurrent.BookingProcessor proc =
            new evcharging.concurrent.BookingProcessor(
                manager, customer, vehicle,
                station.getStationId(), slot.getSlotId(), bid);
        Thread t = new Thread(proc);
        t.start();
        try { t.join(2000); } catch (InterruptedException ignored) {}

        appendOutput(String.format(
            "%n== Booking Confirmation ==%n" +
            "  Booking ID  : %d%n" +
            "  Customer    : %s (%s)%n" +
            "  Vehicle     : %s%n" +
            "  Station     : %s%n" +
            "  Slot        : %s%n" +
            "  Date        : %s%n" +
            "  Status      : BOOKED [OK]%n",
            bid, custName, custId, vehNum,
            station.getStationName(), slot.getSlotTime(), LocalDate.now()));

        refreshSlots();
    }

    private void handleBill() {
        String unitsStr = unitsField.getText().trim();
        if (unitsStr.isEmpty()) {
            appendOutput("\n[ERROR] Enter energy consumed (kWh) to calculate bill.\n");
            return;
        }
        try {
            double units      = Double.parseDouble(unitsStr);
            int stationIdx    = stationBox.getSelectedIndex();
            List<ChargingStation> stations = manager.listStations();
            if (stationIdx < 0 || stationIdx >= stations.size()) {
                appendOutput("\n[ERROR] Select a station first.\n");
                return;
            }
            ChargingStation station = stations.get(stationIdx);
            double rate       = station.getRatePerKwh();
            double chargeCost = units * rate;
            double bookingFee = 30.00;
            double tax        = (chargeCost + bookingFee) * 0.18;
            double total      = chargeCost + bookingFee + tax;

            appendOutput(String.format(
                "%n== Bill Summary ==%n" +
                "  Station         : %s%n" +
                "  Rate            : Rs.%.2f/kWh%n" +
                "  Units Consumed  : %.2f kWh%n" +
                "  Charging Cost   : Rs.%.2f%n" +
                "  Booking Fee     : Rs.%.2f%n" +
                "  GST (18%%)       : Rs.%.2f%n" +
                "  ----------------------------------%n" +
                "  TOTAL PAYABLE   : Rs.%.2f%n" +
                "  Payment Status  : PAID [OK]%n",
                station.getStationName(), rate, units,
                chargeCost, bookingFee, tax, total));
        } catch (NumberFormatException ex) {
            appendOutput("\n[ERROR] Enter a valid numeric value for units.\n");
        }
    }

    private void appendOutput(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void refreshSlots() {
        slotBox.removeAllItems();
        for (ChargingSlot sl : manager.listAvailableSlots()) {
            slotBox.addItem("Slot " + sl.getSlotId() + " | " +
                sl.getSlotTime() + " | Stn" + sl.getStation().getStationId());
        }
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_SUB);
        lbl.setForeground(bookBtnColor);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, outputBg));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return lbl;
    }

    private JPanel labeledRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(F_BODY);
        lbl.setForeground(textColor);
        lbl.setPreferredSize(new Dimension(120, 28));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(F_BODY);
        tf.setForeground(textWhite);
        tf.setCaretColor(textWhite);
        tf.setBackground(outputBg);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(outputBg.brighter(), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        tf.setToolTipText(placeholder);
        return tf;
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(F_BODY);
        box.setBackground(outputBg);
        box.setForeground(textWhite);
        box.setBorder(new LineBorder(outputBg.brighter(), 1, true));
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        return btn;
    }
}