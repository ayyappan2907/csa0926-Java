package db;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Module 3 — Production DAO.
 * All JDBC insert / query operations against factory.db.
 */
public class ProductionDAO {

    private final DatabaseManager dbManager;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProductionDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // ── Session ────────────────────────────────────────────────────────────

    /** Creates a row in session_summary for the given session UUID. */
    public void initSession(String sessionId) {
        String sql = "INSERT OR IGNORE INTO session_summary (session_id, start_time, total_produced) VALUES (?,?,0)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setString(2, now());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateSessionSummary(String sessionId, int totalProduced, double efficiencyPct) {
        String sql = "UPDATE session_summary SET end_time=?, total_produced=?, efficiency_pct=? WHERE session_id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, now());
            ps.setInt(2, totalProduced);
            ps.setDouble(3, efficiencyPct);
            ps.setString(4, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Production log ──────────────────────────────────────────────────────

    /** Logs a batch production event. */
    public void logProduction(String sessionId, String resource, int quantity) {
        String sql = "INSERT INTO production_log (session_id, resource, quantity, timestamp) VALUES (?,?,?,?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setString(2, resource);
            ps.setInt(3, quantity);
            ps.setString(4, now());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    /** Returns total quantity produced per resource type for the given session. */
    public Map<String, Integer> getProductionTotals(String sessionId) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        String sql = "SELECT resource, SUM(quantity) as total FROM production_log " +
                     "WHERE session_id=? GROUP BY resource ORDER BY total DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                totals.put(rs.getString("resource"), rs.getInt("total"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return totals;
    }

    /**
     * Returns the last N sessions as string arrays:
     * [shortId, startTime, endTime, totalProduced, efficiency%]
     */
    public List<String[]> getRecentSessions(int limit) {
        List<String[]> sessions = new ArrayList<>();
        String sql = "SELECT session_id, start_time, end_time, total_produced, efficiency_pct " +
                     "FROM session_summary ORDER BY start_time DESC LIMIT ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String sid = rs.getString("session_id");
                sessions.add(new String[]{
                    sid.length() >= 8 ? sid.substring(0, 8) + "…" : sid,
                    rs.getString("start_time"),
                    rs.getString("end_time") != null ? rs.getString("end_time") : "Active",
                    String.valueOf(rs.getInt("total_produced")),
                    String.format("%.1f%%", rs.getDouble("efficiency_pct"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    /** Returns the grand total across ALL sessions. */
    public int getGrandTotal() {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM production_log";
        try (Statement st = dbManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private String now() { return LocalDateTime.now().format(FMT); }
}
