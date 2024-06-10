/* (C)2024 */
package dk.dtu.engine.utility;

import dk.dtu.engine.core.WindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.SudokuGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private final String serverAddress;
    private final WindowManager windowManager;
    private SudokuGame game;
    private boolean isGameStarted = false;

    // Implement logger
    private static final Logger logger = LoggerFactory.getLogger(GameClient.class);

    public GameClient(String serverAddress, WindowManager windowManager) {
        this.serverAddress = serverAddress;
        this.windowManager = windowManager;
    }

    public void start() throws IOException, Board.BoardNotCreatable {
        if (isGameStarted) return;
        isGameStarted = true;

        Socket socket = createSocket(serverAddress);
        System.out.println("Connected to server at " + serverAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Send connect signal
        out.println("CONNECT");

        game = new SudokuGame(windowManager, 3, 3, 550 / 9); // Adjust as needed
        game.setNetworkOut(out); // Pass the network output stream to the game
        game.setNetworkGame(true);

        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received message: " + message); // Display the message in the console
            processNetworkMessage(message, game);
        }
    }

    protected Socket createSocket(String serverAddress) throws IOException {
        return new Socket(serverAddress, 12345);
    }

    public SudokuGame getGame() {
        return game;
    }

    private void processNetworkMessage(String message, SudokuGame game) {
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        Config.setEnableLives(false);
        switch (command) {
            case "INITIAL_BOARD":
                int[][] board = stringToBoard(parts[1]);
                game.initializeCustom(board);
                break;
            case "WINNER":
                game.processNetworkMessage(message);
                break;
        }
    }

    private int[][] stringToBoard(String boardString) {
        String[] rows = boardString.split(";");
        int[][] board = new int[rows.length][rows[0].split(",").length];
        for (int i = 0; i < rows.length; i++) {
            String[] nums = rows[i].split(",");
            for (int j = 0; j < nums.length; j++) {
                board[i][j] = Integer.parseInt(nums[j]);
            }
        }
        return board;
    }

    public boolean testGameConnection() {
        try (Socket ignored = createSocket(serverAddress)) {
            return true;
        } catch (IOException e) {
            logger.error("Failed to connect to server at {}: {}", serverAddress, e.getMessage());
            return false;
        }
    }
}

