/* (C)2024 */
package dk.dtu.engine.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for loading the leaderboard from the database.
 * It gets the strings from the database and creates a list of LeaderboardEntry objects.
 */
public class Leaderboard {

    private static final Logger logger = LoggerFactory.getLogger(Leaderboard.class);

    private Leaderboard() {
        throw new IllegalStateException("Utility class");
    }

    public static List<LeaderboardEntry> loadLeaderboard(String dbUrl) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String sql = "SELECT username, difficulty, time, timestamp, sizeN, sizeK FROM leaderboard ORDER BY time ";

        try (Connection conn = DriverManager.getConnection(dbUrl);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                String difficulty = rs.getString("difficulty");
                int time = rs.getInt("time");
                String timestamp = rs.getString("timestamp");
                int sizeN = rs.getInt("sizeN");
                int sizeK = rs.getInt("sizeK");

                leaderboard.add(new LeaderboardEntry(username, difficulty, time, timestamp, sizeN, sizeK));
            }

        } catch (SQLException e) {
            logger.error("An error occurred while loading the leaderboard");
            logger.error(e.getMessage());
        }

        return leaderboard;
    }

    // Leaderboard entry class to represent each row in the leaderboard
    public record LeaderboardEntry(String username, String difficulty, int time, String timestamp, int sizeN, int sizeK) {

        @Override
        public String toString() {
            return "LeaderboardEntry{"
                    + "username='"
                    + username
                    + '\''
                    + ", difficulty='"
                    + difficulty
                    + '\''
                    + ", time="
                    + time
                    + ", timestamp='"
                    + timestamp
                    + '\''
                    + ", sizeN="
                    + sizeN
                    + ", sizeK="
                    + sizeK
                    + '}';
        }
    }
}
