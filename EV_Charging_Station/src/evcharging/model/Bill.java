package evcharging.model;

import java.time.LocalDateTime;

/** Calculates and stores the final charging bill. */
public class Bill {
    private int billId;
    private Booking booking;
    private double energyConsumed;
    private double ratePerKwh;
    private double chargingCost;
    private double bookingFee;
    private double tax;
    private double totalAmount;
    private LocalDateTime billDate;

    public Bill(int billId, Booking booking,
                double energyConsumed, double bookingFee,
                double taxRate) {
        this.billId = billId;
        this.booking = booking;
        this.energyConsumed = energyConsumed;
        this.ratePerKwh = booking.getStation().getRatePerKwh();
        this.bookingFee = bookingFee;
        this.chargingCost = energyConsumed * ratePerKwh;
        this.tax = (chargingCost + bookingFee) * taxRate;
        this.totalAmount = chargingCost + bookingFee + tax;
        this.billDate = LocalDateTime.now();
    }

    public int getBillId() { return billId; }
    public double getEnergyConsumed() { return energyConsumed; }
    public double getChargingCost() { return chargingCost; }
    public double getBookingFee() { return bookingFee; }
    public double getTax() { return tax; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getBillDate() { return billDate; }

    @Override
    public String toString() {
        return String.format(
            "Bill ID: %d%nCharging Cost: Rs.%.2f%n" +
            "Booking Fee: Rs.%.2f%nTax: Rs.%.2f%n" +
            "Total Amount: Rs.%.2f",
            billId, chargingCost, bookingFee, tax, totalAmount
        );
    }
}