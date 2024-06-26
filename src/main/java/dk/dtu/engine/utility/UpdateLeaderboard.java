/* (C)2024 */
package dk.dtu.engine.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for updating the leaderboard with new scores, whenever a game is finished.
 *
 */
public class UpdateLeaderboard {

    private UpdateLeaderboard() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger logger = LoggerFactory.getLogger(UpdateLeaderboard.class);

    public static void addScore(String url, String username, String difficulty, int time, int sizeN, int sizeK) {
        String sql = "INSERT INTO leaderboard(username, difficulty, time, sizeN, sizeK) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, difficulty);
            stmt.setInt(3, time);
            stmt.setInt(4, sizeN);
            stmt.setInt(5, sizeK);
            stmt.executeUpdate();
            logger.info("Score added to leaderboard");
        } catch (SQLException e) {
            logger.error("An error occurred while adding score to leaderboard");
            logger.error(e.getMessage());
        }
    }
}
