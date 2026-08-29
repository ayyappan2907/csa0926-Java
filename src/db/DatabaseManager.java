package db;

import java.sql.*;

/**
 * Module 3 — Database Manager.
 * Opens a SQLite connection via JDBC and initialises the schema.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:factory.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            initSchema();
            System.out.println("[DB] Connected to factory.db");
        } catch (Exception e) {
            System.err.println("[DB] Initialisation error: " + e.getMessage());
        }
    }

    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(
                "CREATE TABLE IF NOT EXISTS production_log (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  session_id TEXT    NOT NULL," +
                "  resource   TEXT    NOT NULL," +
                "  quantity   INTEGER NOT NULL," +
                "  timestamp  TEXT    NOT NULL" +
                ")"
            );
            st.execute(
                "CREATE TABLE IF NOT EXISTS session_summary (" +
                "  session_id     TEXT PRIMARY KEY," +
                "  start_time     TEXT," +
                "  end_time       TEXT," +
                "  total_produced INTEGER DEFAULT 0," +
                "  efficiency_pct REAL    DEFAULT 0.0" +
                ")"
            );
            st.execute(
                "CREATE TABLE IF NOT EXISTS blueprints (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  author TEXT," +
                "  width INTEGER," +
                "  height INTEGER," +
                "  placements_json TEXT NOT NULL," +
                "  created_at TEXT," +
                "  cached_items INTEGER," +
                "  cached_efficiency REAL" +
                ")"
            );
        }
    }

    public Connection getConnection() { return connection; }

    public boolean isConnected() {
        try { return connection != null && !connection.isClosed(); }
        catch (SQLException e) { return false; }
    }

    public void close() {
        try {
            if (isConnected()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertBlueprint(pattern.SavedBlueprint b) {
        String sql = "INSERT INTO blueprints (id, name, author, width, height, placements_json, created_at, cached_items, cached_efficiency) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, b.id);
            pstmt.setString(2, b.name);
            pstmt.setString(3, b.author);
            pstmt.setInt(4, b.width);
            pstmt.setInt(5, b.height);
            pstmt.setString(6, b.toJson());
            pstmt.setString(7, b.createdAt.toString());
            pstmt.setInt(8, b.cachedItems);
            pstmt.setDouble(9, b.cachedEfficiency);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public java.util.List<pattern.SavedBlueprint> getAllBlueprints() {
        java.util.List<pattern.SavedBlueprint> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM blueprints";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new pattern.SavedBlueprint(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("author"),
                    rs.getInt("width"),
                    rs.getInt("height"),
                    pattern.SavedBlueprint.fromJson(rs.getString("placements_json")),
                    java.time.Instant.parse(rs.getString("created_at")),
                    false,
                    rs.getInt("cached_items"),
                    rs.getDouble("cached_efficiency")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteBlueprint(String id) {
        String sql = "DELETE FROM blueprints WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
