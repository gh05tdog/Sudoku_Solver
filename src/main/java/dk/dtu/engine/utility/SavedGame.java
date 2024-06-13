package dk.dtu.engine.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavedGame {

    private static final Logger logger = LoggerFactory.getLogger(SavedGame.class);

    public static void saveGame(String dbUrl, int[][] initialBoard, int[][] currentBoard, int time, int usedLifeLines, boolean lifeEnabled, int kSize, int nSize, int[][] cages, boolean isKillerSudoku) {
        String initialBoardString = serializeBoard(initialBoard);
        String currentBoardString = serializeBoard(currentBoard);
        String insertGame = "INSERT INTO saved_games (initialBoard, currentBoard, time, usedLifeLines, lifeEnabled, kSize, nSize, cages, isKillerSudoku) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(insertGame)) {
            stmt.setString(1, initialBoardString);
            stmt.setString(2, currentBoardString);
            stmt.setInt(3, time);
            stmt.setInt(4, usedLifeLines);
            stmt.setBoolean(5, lifeEnabled);
            stmt.setInt(6, kSize);
            stmt.setInt(7, nSize);
            stmt.setString(8, serializeBoard(cages));
            stmt.setBoolean(9, isKillerSudoku);
            stmt.executeUpdate();
            logger.info("Game saved successfully");
        } catch (SQLException e) {
            logger.error("Failed to save the game");
            logger.error(e.getMessage());
        }
    }

    public static List<SavedGameData> loadSavedGames(String dbUrl) {
        String selectGames = "SELECT initialBoard, currentBoard, time, usedLifeLines, lifeEnabled, kSize, nSize, cages, isKillerSudoku FROM saved_games";
        List<SavedGameData> savedGames = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectGames)) {

            while (rs.next()) {
                String initialBoard = rs.getString("initialBoard");
                String currentBoard = rs.getString("currentBoard");
                int time = rs.getInt("time");
                int usedLifeLines = rs.getInt("usedLifeLines");
                boolean lifeEnabled = rs.getBoolean("lifeEnabled");
                int kSize = rs.getInt("kSize");
                int nSize = rs.getInt("nSize");
                String cages = rs.getString("cages");
                boolean isKillerSudoku = rs.getBoolean("isKillerSudoku");
                savedGames.add(new SavedGameData(initialBoard, currentBoard, time, usedLifeLines, lifeEnabled, kSize, nSize, cages, isKillerSudoku));
            }
        } catch (SQLException e) {
            logger.error("Failed to load saved games");
            logger.error(e.getMessage());
        }

        return savedGames;
    }

    // Data class to hold the saved game data
    public static class SavedGameData {
        private final String initialBoard;
        private final String currentBoard;
        private final int time;
        private final int usedLifeLines;
        private final boolean lifeEnabled;
        private final int kSize;
        private final int nSize;
        private final String cages;
        private final boolean isKillerSudoku;

        public SavedGameData(String initialBoard, String currentBoard, int time, int usedLifeLines, boolean lifeEnabled, int kSize, int nSize, String cages, boolean isKillerSudoku) {
            this.initialBoard = initialBoard;
            this.currentBoard = currentBoard;
            this.time = time;
            this.usedLifeLines = usedLifeLines;
            this.lifeEnabled = lifeEnabled;
            this.kSize = kSize;
            this.nSize = nSize;
            this.cages = cages;
            this.isKillerSudoku = isKillerSudoku;
        }

        public String getInitialBoard() {
            return initialBoard;
        }

        public String getCurrentBoard() {
            return currentBoard;
        }

        public int getTime() {
            return time;
        }

        public int getUsedLifeLines() {
            return usedLifeLines;
        }

        public boolean isLifeEnabled() {
            return lifeEnabled;
        }

        public int getKSize() {
            return kSize;
        }

        public int getNSize() {
            return nSize;
        }

        public String getCages() {
            return cages;
        }

        public boolean isKillerSudoku() {
            return isKillerSudoku;
        }

        @Override
        public String toString() {
            return "Time: " + time + ", Used Life Lines: " + usedLifeLines + ", Life Enabled: " + lifeEnabled + ", KSize: " + kSize + ", NSize: " + nSize + "\n" + "isKillerSudoku:" + isKillerSudoku;
        }
    }

    private static String serializeBoard(int[][] board) {
        StringBuilder builder = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                builder.append(cell).append(",");
            }
            builder.setLength(builder.length() - 1);  // Remove last comma
            builder.append(";");
        }
        builder.setLength(builder.length() - 1);  // Remove last semicolon
        return builder.toString();
    }

}
