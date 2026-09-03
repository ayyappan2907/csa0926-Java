package evcharging.dao;

import evcharging.model.ChargingStation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC operations for charging stations. */
public class StationDAO {
    public void save(ChargingStation station) throws SQLException {
        String sql =
            "INSERT INTO charging_station " +
            "(station_id, station_name, location, " +
            "charger_type, rate_per_kwh) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, station.getStationId());
            ps.setString(2, station.getStationName());
            ps.setString(3, station.getLocation());
            ps.setString(4, station.getChargerType());
            ps.setDouble(5, station.getRatePerKwh());
            ps.executeUpdate();
        }
    }

    public List<ChargingStation> findAll() throws SQLException {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_station";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ChargingStation(
                    rs.getInt("station_id"),
                    rs.getString("station_name"),
                    rs.getString("location"),
                    rs.getString("charger_type"),
                    rs.getDouble("rate_per_kwh")
                ));
            }
        }
        return list;
    }

    public void updateRate(int stationId, double newRate) throws SQLException {
        String sql =
            "UPDATE charging_station " +
            "SET rate_per_kwh = ? " +
            "WHERE station_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newRate);
            ps.setInt(2, stationId);
            ps.executeUpdate();
        }
    }

    public void delete(int stationId) throws SQLException {
        String sql =
            "DELETE FROM charging_station WHERE station_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            ps.executeUpdate();
        }
    }
}