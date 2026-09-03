package evcharging.dao;

import evcharging.model.Bill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** JDBC operations for bills. */
public class BillDAO {
    public void saveBill(Bill bill) throws SQLException {
        String sql =
            "INSERT INTO bills " +
            "(bill_id, booking_id, units_consumed, " +
            "rate_per_kwh, charging_cost, booking_fee, " +
            "tax, total_amount, payment_status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bill.getBillId());
            ps.setDouble(3, bill.getEnergyConsumed());
            ps.setDouble(5, bill.getChargingCost());
            ps.setDouble(6, bill.getBookingFee());
            ps.setDouble(7, bill.getTax());
            ps.setDouble(8, bill.getTotalAmount());
            ps.setString(9, "PAID");
            ps.executeUpdate();
        }
    }
}