package dk.dtu.game.core;

public class Config {
    private static int k;
    private static int n;
    private static int cellSize;
    private static String difficulty;

    private Config() {
        throw new IllegalStateException("Utility class");
    }


    public static int getK() {
        return k;
    }

    public static int getN() {
        return n;
    }
    public static int getCellSize() {
        return cellSize;
    }
    public static String getDifficulty() {
        return difficulty;
    }

    public static void setN(int n) {
        Config.n = n;
    }
    public static void setK(int k) {
        Config.k = k;
    }
    public static void setCellSize(int cellSize) {
        Config.cellSize = cellSize;
    }
    public static void setDifficulty(String difficulty) {
        Config.difficulty = difficulty;
    }



}
