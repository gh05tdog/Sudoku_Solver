package dk.dtu.game.core;

public class config {
    private static int k;
    private static int n;
    private static int cellSize;
    private static String difficulty;

    public config(int n, int k, int cellSize, String difficulty) {
        config.n = n;
        config.k = k;
        config.cellSize = cellSize;
        config.difficulty = difficulty;
    }

    public int getK() {
        return k;
    }

    public int getN() {
        return n;
    }
    public int getCellSize() {
        return cellSize;
    }
    public static String getDifficulty() {
        return difficulty;
    }



}
