package dk.dtu.engine.utility;

import dk.dtu.engine.core.WindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.SudokuGame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* The GameClient class is responsible for connecting to the game server and starting the game.
 * It listens for messages from the server and processes them accordingly.
 */
public class GameClient {
    private final WindowManager windowManager;
    private SudokuGame game;
    private boolean isGameStarted = false;
    private List<String> discoveredServers = new ArrayList<>();

    // Logger for logging messages
    private static final Logger logger = LoggerFactory.getLogger(GameClient.class);

    public GameClient(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void start(String serverAddress) throws IOException, Board.BoardNotCreatable {
        if (isGameStarted) return;
        isGameStarted = true;

        // Create a socket connection to the server
        Socket socket = createSocket(serverAddress);
        logger.info("Connected to server at {}", serverAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Send connect signal to the server
        out.println("CONNECT");

        // Initialize the Sudoku game with given parameters
        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        game.setNetworkOut(out);
        game.setNetworkGame(true);

        // Listen for messages from the server
        String message;
        while ((message = in.readLine()) != null) {
            logger.info("Received message: {}", message); // Display the message in the console
            processNetworkMessage(message, game);
        }
    }


    // Method to create a socket connection to the server
    protected Socket createSocket(String serverAddress) throws IOException {
        return new Socket(serverAddress, 12345); // Create a socket with the server address and port
    }

    // Getter for the game instance
    public SudokuGame getGame() {
        return game;
    }

    // Method to process messages received from the server
    private void processNetworkMessage(String message, SudokuGame game) {
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        logger.info("Command: {}", command);
        switch (command) {

            case "INITIAL_BOARD":
                logger.info("Received initial board: {}", parts[1]);
                int[][] board = stringToBoard(parts[1]);
                Config.setK(3);
                Config.setN(3);
                Config.setEnableEasyMode(false);
                Config.setEnableLives(false);
                game.initializeCustom(board); // Initialize the game with the custom board
                break;
            case "PROGRESS", "WINNER":
                game.processNetworkMessage(message); // Process winner message
                break;
        }
    }

    // Method to convert a board string to a 2D integer array
    private int[][] stringToBoard(String boardString) {
        String[] rows = boardString.split(";"); // Split the string into rows
        int[][] board =
                new int[rows.length][rows[0].split(",").length]; // Initialize the board array
        for (int i = 0; i < rows.length; i++) {
            String[] nums = rows[i].split(","); // Split the row into numbers
            for (int j = 0; j < nums.length; j++) {
                board[i][j] =
                        Integer.parseInt(nums[j]); // Parse each number and assign it to the board
            }
        }
        return board;
    }

    public boolean testGameConnection(String serverAddress) {
        try (Socket ignored = createSocket(serverAddress)) {
            return true; // Return true if the connection is successful
        } catch (IOException e) {
            logger.error("Failed to connect to server at {}: {}", serverAddress, e.getMessage());
            return false; // Return false if the connection fails
        }
    }

    public List<String> getDiscoveredServers() {
        return discoveredServers;
    }

    public void discoverServers() {
        SSDPClient ssdpClient = new SSDPClient();
        discoveredServers = ssdpClient.discover();

        for (String server : discoveredServers) {
            logger.info("Found server: {}", server);
        }
    }
}