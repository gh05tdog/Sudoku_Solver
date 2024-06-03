package dk.dtu.core;

import dk.dtu.engine.utility.DatabaseSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DatabaseSetupTest {
    private static final String DB_URL = "jdbc:sqlite:test_sudoku.db";

    @BeforeEach
    public void setUp() {
        // Set up the database before each test
        DatabaseSetup.setup(DB_URL);
    }

    @AfterEach
    public void tearDown() {
        // Clean up the database file after each test
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS game");
            stmt.execute("DROP TABLE IF EXISTS leaderboard");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLeaderboardTableCreation() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='leaderboard'")) {
            assertTrue(rs.next(), "Leaderboard table should be created");
        } catch (SQLException e) {
            fail("An error occurred while checking the leaderboard table: " + e.getMessage());
        }
    }
}
