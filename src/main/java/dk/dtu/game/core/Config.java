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


    //Game rules
    private static boolean enableLives = true;
    private static boolean enableTimer = true;
    private static boolean enableEasyMode = true;
    //Game rules:
    public static void setEnableLives(boolean enableLives) {
        Config.enableLives = enableLives;
    }

    public static boolean getEnableLives() {
        return enableLives;
    }

    public static void setEnableTimer(boolean enableTimer) {
        Config.enableTimer = enableTimer;
    }

    public static boolean getEnableTimer() {
        return enableTimer;
    }

    public static void setEnableEasyMode(boolean enableEasyMode) {
        Config.enableEasyMode = enableEasyMode;
    }
    public static boolean getEnableEasyMode() {
        return enableEasyMode;
    }



}
