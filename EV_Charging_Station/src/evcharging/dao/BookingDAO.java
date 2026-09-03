package evcharging.dao;

import evcharging.model.Booking;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** JDBC operations for bookings. */
public class BookingDAO {
    public void saveBooking(Booking booking) throws SQLException {
        String sql =
            "INSERT INTO bookings " +
            "(booking_id, user_id, vehicle_id, " +
            "station_id, slot_id, booking_date, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, booking.getBookingId());
            ps.setString(2, booking.getCustomer().getUserId());
            ps.setInt(3, booking.getVehicle().getVehicleId());
            ps.setInt(4, booking.getStation().getStationId());
            ps.setInt(5, booking.getSlot().getSlotId());
            ps.setDate(6, java.sql.Date.valueOf(booking.getBookingDate()));
            ps.setString(7, booking.getStatus().name());
            ps.executeUpdate();
        }
    }
}