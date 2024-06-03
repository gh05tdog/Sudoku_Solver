package dk.dtu.engine.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class UpdateLeaderboard {
    private static final Logger logger = LoggerFactory.getLogger(UpdateLeaderboard.class);
    public static void addScore(String username, String difficulty, int time) {
        String url = "jdbc:sqlite:sudoku.db";
        String sql = "INSERT INTO leaderboard(username, difficulty, time) VALUES(?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, difficulty);
            stmt.setInt(3, time);
            stmt.executeUpdate();
            logger.info("Score added to leaderboard");
        } catch (SQLException e) {
            logger.error("An error occurred while adding score to leaderboard");
            logger.error(e.getMessage());
        }
    }
}