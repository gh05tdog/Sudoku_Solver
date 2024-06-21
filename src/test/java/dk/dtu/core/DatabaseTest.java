package dk.dtu.core;

import dk.dtu.engine.utility.DatabaseSetup;
import dk.dtu.engine.utility.Leaderboard;
import dk.dtu.engine.utility.SavedGame;
import dk.dtu.engine.utility.UpdateLeaderboard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

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
            stmt.execute("DROP TABLE IF EXISTS saved_games");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void loadLeaderboard() {
        // Test that the leaderboard can be loaded
        // Arrange
        DatabaseSetup.setup(DB_URL);
        // Act
        List<Leaderboard.LeaderboardEntry> leaderboard = Leaderboard.loadLeaderboard("jdbc:sqlite:test_sudoku.db");
        // Assert
        assertTrue(leaderboard.isEmpty(), "Leaderboard should be empty");
    }

    @Test
    void loadLeaderboardWithEntries() {
        // Test that the leaderboard can be loaded with entries
        // Arrange
        DatabaseSetup.setup(DB_URL);
        // Act
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO leaderboard (username, difficulty, time, sizeN, sizeK) VALUES ('user1', 'easy', 100, 3, 3)");
            stmt.execute("INSERT INTO leaderboard (username, difficulty, time, sizeN, sizeK) VALUES ('user2', 'medium', 200, 3, 3)");
            stmt.execute("INSERT INTO leaderboard (username, difficulty, time, sizeN, sizeK) VALUES ('user3', 'hard', 300, 3, 3)");
        } catch (SQLException e) {
            fail("An error occurred while inserting leaderboard entries: " + e.getMessage());
        }
        List<Leaderboard.LeaderboardEntry> leaderboard = Leaderboard.loadLeaderboard("jdbc:sqlite:test_sudoku.db");
        // Assert
        assertEquals(3, leaderboard.size(), "Leaderboard should contain 3 entries");
    }

    @Test
    void updateLeaderboard(){
        // Test that the leaderboard can be updated
        DatabaseSetup.setup(DB_URL);

        UpdateLeaderboard.addScore(DB_URL, "TEST", "easy", 100, 3, 3);
        List<Leaderboard.LeaderboardEntry> leaderboard = Leaderboard.loadLeaderboard("jdbc:sqlite:test_sudoku.db");

        assertEquals(1, leaderboard.size(), "Leaderboard should contain 1 entry");
        assertEquals("TEST", leaderboard.getFirst().getUsername(), "Username should be TEST");
        assertEquals("easy", leaderboard.getFirst().getDifficulty(), "Difficulty should be easy");
    }

    @Test
    void updateSavedGames(){
        // Test that the saved games can be updated
        DatabaseSetup.setup(DB_URL);

        int[][] board = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        int[] usedLifeLines = {0, 0, 0};
        int[][] cages = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        SavedGame.saveGame(
                DB_URL,
                "test_game",
                board,
                board,
                100,
                usedLifeLines,
                true,
                3,
                3,
                cages,
                false,
                "notes",
                "easy"
        );

        List<SavedGame.SavedGameData> savedGames = SavedGame.loadSavedGames(DB_URL);

        assertEquals(1, savedGames.size(), "There should be 1 saved game");
        assertEquals("test_game", savedGames.getFirst().getName(), "The saved game name should be 'test_game'");
        assertEquals(100, savedGames.getFirst().getTime(), "The time should be 100");
    }
}
