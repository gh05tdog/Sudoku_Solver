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
import java.util.ArrayList;
import java.util.List;

public class GameClient {
    private final WindowManager windowManager;
    private SudokuGame game;
    private boolean isGameStarted = false;
    private List<String> discoveredServers = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(GameClient.class);

    public GameClient(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void start(String serverAddress) throws IOException, Board.BoardNotCreatable {
        if (isGameStarted) return;
        isGameStarted = true;

        Socket socket = createSocket(serverAddress);
        logger.info("Connected to server at {}", serverAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Send connect signal
        out.println("CONNECT");

        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        game.setNetworkOut(out);
        game.setNetworkGame(true);

        String message;
        while ((message = in.readLine()) != null) {
            logger.info("Received message: {}", message); // Display the message in the console
            processNetworkMessage(message, game);
        }
    }

    protected Socket createSocket(String serverAddress) throws IOException {
        logger.info("Attempting to connect to server at {}", serverAddress);
        return new Socket(serverAddress, 12346);
    }

    public SudokuGame getGame() {
        return game;
    }

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
                game.initializeCustom(board);
                break;
            case "PROGRESS":
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

    public boolean testGameConnection(String serverAddress) {
        try (Socket ignored = createSocket(serverAddress)) {
            logger.info("Successfully connected to server at {}", serverAddress);
            return true;
        } catch (IOException e) {
            logger.error("Failed to connect to server at {}: {}", serverAddress, e.getMessage());
            return false;
        }
    }

    public List<String> getDiscoveredServers() {
        return discoveredServers;
    }

    public void discoverServers() {
        SSDPClient ssdpClient = new SSDPClient();
        discoveredServers = ssdpClient.discover();

        for (String server : discoveredServers) {
            if (server != null) {
                logger.info("Found server: {}", server);
            } else {
                logger.warn("Received a null server address in SSDP response");
            }
        }

        if (discoveredServers.isEmpty()) {
            logger.warn("No servers discovered.");
        }
    }
}