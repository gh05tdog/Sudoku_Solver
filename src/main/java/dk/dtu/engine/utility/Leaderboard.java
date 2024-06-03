package dk.dtu.engine.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Leaderboard {

    private Leaderboard() {
        throw new IllegalStateException("Utility class");
    }
    private static final String DB_URL = "jdbc:sqlite:sudoku.db";

    private static final Logger logger = LoggerFactory.getLogger(Leaderboard.class);

    public static List<LeaderboardEntry> loadLeaderboard() {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String sql = "SELECT username, difficulty, time, timestamp FROM leaderboard ORDER BY time ";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                String difficulty = rs.getString("difficulty");
                int time = rs.getInt("time");
                String timestamp = rs.getString("timestamp");

                leaderboard.add(new LeaderboardEntry(username, difficulty, time, timestamp));
            }

        } catch (SQLException e) {
            logger.error("An error occurred while loading the leaderboard");
            logger.error(e.getMessage());
        }

        return leaderboard;
    }

    // Leaderboard entry class to represent each row in the leaderboard
    public static class LeaderboardEntry {
        private final String username;
        private final String difficulty;
        private final int time;
        private final String timestamp;

        public LeaderboardEntry(String username, String difficulty, int time, String timestamp) {
            this.username = username;
            this.difficulty = difficulty;
            this.time = time;
            this.timestamp = timestamp;
        }

        public String getUsername() {
            return username;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public int getTime() {
            return time;
        }

        public String getTimestamp() {
            return timestamp;
        }


        @Override
        public String toString() {
            return "LeaderboardEntry{" +
                    "username='" + username + '\'' +
                    ", difficulty='" + difficulty + '\'' +
                    ", time=" + time +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }
}
