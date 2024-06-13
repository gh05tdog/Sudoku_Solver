/* (C)2024 */
package dk.dtu.game.core;

import java.util.prefs.Preferences;

/**
 * The Config class is a utility class that stores the configuration of the game.
 * It contains information about the board size, cell size, and difficulty. Along with the gamerules.
 * It uses preferences to store values even when closing the game
 */

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


    // Game rules:
    public static void setEnableLives(boolean enableLives) {
        // Store the value in the system preferences
        Preferences pref = Preferences.userRoot();
        pref.put("enableLives", String.valueOf(enableLives));
    }

    public static boolean getEnableLives() {
        Preferences pref = Preferences.userRoot();
        String enableLives = pref.get("enableLives", String.valueOf(true));
        return Boolean.parseBoolean(enableLives);
    }

    public static void setEnableTimer(boolean enableTimer) {
        // Store the value in the system preferences
        Preferences pref = Preferences.userRoot();
        pref.put("enableTimer", String.valueOf(enableTimer));
    }

    public static boolean getEnableTimer() {
        Preferences pref = Preferences.userRoot();
        String enableTimer = pref.get("enableTimer", String.valueOf(true));
        return Boolean.parseBoolean(enableTimer);
    }

    public static void setEnableEasyMode(boolean enableEasyMode) {
        // Store the value in the system preferences
        Preferences pref = Preferences.userRoot();
        pref.put("enableEasyMode", String.valueOf(enableEasyMode));
    }

    public static boolean getEnableEasyMode() {
        Preferences pref = Preferences.userRoot();
        String enableEasyMode = pref.get("enableEasyMode", String.valueOf(true));
        return Boolean.parseBoolean(enableEasyMode);
    }

    public static void setNumberOfLives(int numberOfLives) {
        // Store the value in the system preferences
        Preferences pref = Preferences.userRoot();
        pref.put("numberOfLives", String.valueOf(numberOfLives));
    }

    public static int getNumberOfLives() {
        Preferences pref = Preferences.userRoot();
        String numberOfLives = pref.get("numberOfLives", String.valueOf(3));
        return Integer.parseInt(numberOfLives);
    }
}
