package dk.dtu.engine.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetup.class);

    // Private constructor to prevent instantiation
    private DatabaseSetup() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void setup(String dbUrl) {

        String createGameTable = "CREATE TABLE IF NOT EXISTS game (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "state TEXT NOT NULL," +
                "difficulty TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createLeaderboardTable = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "difficulty TEXT NOT NULL," +
                "time INTEGER NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createGameTable);
            stmt.execute(createLeaderboardTable);
            logger.info("Database setup complete");
        } catch (SQLException e) {
            logger.error("Database setup failed");
            logger.error(e.getMessage());
        }
    }
}
