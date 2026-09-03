package evcharging.app;

import evcharging.collection.ChargingManager;
import evcharging.concurrent.BookingProcessor;
import evcharging.gui.EVChargingGUI;
import evcharging.model.*;
import javax.swing.*;

/**
 * Main application for EV Charging Station
 * Slot Booking and Billing System.
 * SIMATS Engineering - CSA0926 Java Programming.
 */
public class EVChargingApp {
    public static void main(String[] args) {
        ChargingManager manager = new ChargingManager();

        Customer customer = new Customer(
            "U101", "Customer One",
            "customer@gmail.com", "pw"
        );

        Vehicle vehicle = new Vehicle(
            1, "AP01AB1234", "Electric Car", customer
        );

        ChargingStation station1 = new ChargingStation(
            1, "Green Charge Station", "Kadapa",    "Fast Charger", 12.00);
        ChargingStation station2 = new ChargingStation(
            2, "EV Power Station",     "Chennai",   "DC Charger",   15.00);
        ChargingStation station3 = new ChargingStation(
            3, "Smart EV Station",     "Bangalore", "AC Charger",   10.00);

        manager.addStation(station1);
        manager.addStation(station2);
        manager.addStation(station3);

        ChargingSlot slot1 = new ChargingSlot(1, station1, "09:00 AM - 10:00 AM");
        ChargingSlot slot2 = new ChargingSlot(2, station1, "10:00 AM - 11:00 AM");
        ChargingSlot slot3 = new ChargingSlot(3, station1, "11:00 AM - 12:00 PM");
        ChargingSlot slot4 = new ChargingSlot(4, station2, "09:00 AM - 10:00 AM");
        ChargingSlot slot5 = new ChargingSlot(5, station2, "10:00 AM - 11:00 AM");
        ChargingSlot slot6 = new ChargingSlot(6, station3, "09:00 AM - 10:00 AM");

        manager.addSlot(slot1);
        manager.addSlot(slot2);
        manager.addSlot(slot3);
        manager.addSlot(slot4);
        manager.addSlot(slot5);
        manager.addSlot(slot6);

        BookingProcessor task = new BookingProcessor(
            manager, customer, vehicle, 1, 1, 1001
        );
        Thread thread = new Thread(task);
        thread.start();

        SwingUtilities.invokeLater(() -> {
            EVChargingGUI gui = new EVChargingGUI(manager);
            gui.setVisible(true);
        });
    }
}