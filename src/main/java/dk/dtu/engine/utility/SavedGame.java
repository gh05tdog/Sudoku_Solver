/* (C)2024 */
package dk.dtu.engine.utility;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavedGame {

    private static final Logger logger = LoggerFactory.getLogger(SavedGame.class);
    private SavedGame() {
        throw new IllegalStateException("Utility class");
    }

    public static void saveGame(
            String dbUrl,
            String name,
            int[][] initialBoard,
            int[][] currentBoard,
            int time,
            int[] usedLifeLines,
            boolean lifeEnabled,
            int kSize,
            int nSize,
            int[][] cages,
            boolean isKillerSudoku,
            String notes) {
        String initialBoardString = serializeBoard(initialBoard);
        String currentBoardString = serializeBoard(currentBoard);
        String usedLifeLinesString = serializeIntArray(usedLifeLines);
        String insertGame =
                "INSERT INTO saved_games (name, initialBoard, currentBoard, time, usedLifeLines,"
                    + " lifeEnabled, kSize, nSize, cages, isKillerSudoku, notes) VALUES (?, ?, ?,"
                    + " ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement stmt = conn.prepareStatement(insertGame)) {
            stmt.setString(1, name);
            stmt.setString(2, initialBoardString);
            stmt.setString(3, currentBoardString);
            stmt.setInt(4, time);
            stmt.setString(5, usedLifeLinesString);
            stmt.setBoolean(6, lifeEnabled);
            stmt.setInt(7, kSize);
            stmt.setInt(8, nSize);
            stmt.setString(9, serializeBoard(cages));
            stmt.setBoolean(10, isKillerSudoku);
            stmt.setString(11, notes);
            stmt.executeUpdate();
            logger.info("Game saved successfully");
        } catch (SQLException e) {
            logger.error("Failed to save the game");
            logger.error(e.getMessage());
        }
    }

    public static List<SavedGameData> loadSavedGames(String dbUrl) {
        String selectGames =
                "SELECT name, initialBoard, currentBoard, time, usedLifeLines, lifeEnabled, kSize,"
                        + " nSize, cages, isKillerSudoku, notes FROM saved_games";
        List<SavedGameData> savedGames = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectGames)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String initialBoard = rs.getString("initialBoard");
                String currentBoard = rs.getString("currentBoard");
                int time = rs.getInt("time");
                int[] usedLifeLines = deserializeIntArray(rs.getString("usedLifeLines"));
                boolean lifeEnabled = rs.getBoolean("lifeEnabled");
                int kSize = rs.getInt("kSize");
                int nSize = rs.getInt("nSize");
                String cages = rs.getString("cages");
                boolean isKillerSudoku = rs.getBoolean("isKillerSudoku");
                String notes = rs.getString("notes");
                savedGames.add(
                        new SavedGameData(
                                name,
                                initialBoard,
                                currentBoard,
                                time,
                                usedLifeLines,
                                lifeEnabled,
                                kSize,
                                nSize,
                                cages,
                                isKillerSudoku,
                                notes));
            }
        } catch (SQLException e) {
            logger.error("Failed to load saved games");
            logger.error(e.getMessage());
        }

        return savedGames;
    }

    private static int[] deserializeIntArray(String arrayString) {
        // Remove square brackets if present
        arrayString = arrayString.replace("[", "").replace("]", "");
        return Arrays.stream(arrayString.split(","))
                .map(String::trim) // Trim whitespace from each element
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static String serializeIntArray(int[] array) {
        return Arrays.stream(array).mapToObj(String::valueOf).collect(Collectors.joining(","));
    }

    // Data class to hold the saved game data
    public static class SavedGameData {
        private final String name;
        private final String initialBoard;
        private final String currentBoard;
        private final int time;
        private final int[] usedLifeLines;
        private final boolean lifeEnabled;
        private final int kSize;
        private final int nSize;
        private final String cages;
        private final boolean isKillerSudoku;
        private final String notes;

        public SavedGameData(
                String name,
                String initialBoard,
                String currentBoard,
                int time,
                int[] usedLifeLines,
                boolean lifeEnabled,
                int kSize,
                int nSize,
                String cages,
                boolean isKillerSudoku,
                String notes) {
            this.name = name;
            this.initialBoard = initialBoard;
            this.currentBoard = currentBoard;
            this.time = time;
            this.usedLifeLines = usedLifeLines;
            this.lifeEnabled = lifeEnabled;
            this.kSize = kSize;
            this.nSize = nSize;
            this.cages = cages;
            this.isKillerSudoku = isKillerSudoku;
            this.notes = notes;
        }

        public String getName() {
            return name;
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

        public int[] getUsedLifeLines() {
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

        public String getNotes() {
            return notes;
        }

        @Override
        public String toString() {
            return "Name: "
                    + name
                    + ", Time: "
                    + time
                    + ", Used Life Lines: "
                    + Arrays.toString(usedLifeLines)
                    + ", Life Enabled: "
                    + lifeEnabled
                    + ", KSize: "
                    + kSize
                    + ", NSize: "
                    + nSize
                    + "\n"
                    + "isKillerSudoku:"
                    + isKillerSudoku;
        }
    }

    private static String serializeBoard(int[][] board) {
        StringBuilder builder = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                builder.append(cell).append(",");
            }
            builder.setLength(builder.length() - 1); // Remove last comma
            builder.append(";");
        }
        builder.setLength(builder.length() - 1); // Remove last semicolon
        return builder.toString();
    }
}
