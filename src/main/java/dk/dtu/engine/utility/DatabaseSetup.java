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

        String createLeaderboardTable = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "difficulty TEXT NOT NULL," +
                "time INTEGER NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createSavedGamesTable = "CREATE TABLE IF NOT EXISTS saved_games (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "initialBoard TEXT NOT NULL," +
                "currentBoard TEXT NOT NULL," +
                "time INTEGER NOT NULL," +
                "usedLifeLines INTEGER NOT NULL," +
                "lifeEnabled BOOLEAN NOT NULL," +
                "kSize INTEGER NOT NULL," +
                "nSize INTEGER NOT NULL," +
                "cages TEXT NOT NULL," +
                "isKillerSudoku BOOLEAN NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "notes TEXT," +
                "name TEXT NOT NULL," +
                "difficulty TEXT NOT NULL" +
                ");";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createLeaderboardTable);
            stmt.execute(createSavedGamesTable);
            logger.info("Database setup complete");
        } catch (SQLException e) {
            logger.error("Database setup failed");
            logger.error(e.getMessage());
        }
    }
}
