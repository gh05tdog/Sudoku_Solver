package dk.dtu.engine.utility;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private static final int PORT = 12345;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(4); // Thread pool to handle client connections
    public final ConcurrentHashMap<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>(); // Map to store client connections and their writers
    private final Object lock = new Object(); // Lock for synchronizing critical sections
    private int connectedPlayers = 0; // Counter for connected players
    public ServerSocket serverSocket;
    private boolean running = true; // Flag to control the server running state

    // Method to start the server
    public void start() {
        startServerSocket(); // Start the server socket
        acceptClients(); // Accept client connections
    }

    // Method to start the server socket
    protected void startServerSocket() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    // Method to accept client connections
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept(); // Accept a new client connection
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true); // Create a writer for the client
                clientWriters.put(clientSocket, writer); // Add the client to the map
                threadPool.execute(new ClientHandler(clientSocket)); // Handle the client in a new thread
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client: {}", e.getMessage());
                }
            }
        }
    }

    // Method to stop the server
    public void stop() {
        running = false;
        try {
            // Close all client connections
            for (Socket clientSocket : clientWriters.keySet()) {
                clientSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the server socket
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }
        threadPool.shutdown(); // Ensure the thread pool is also shut down
    }

    // Method to send the initial board to all clients
    public void sendInitialBoard() throws Board.BoardNotCreatable {
        StringBuilder boardString = new StringBuilder("INITIAL_BOARD ");

        Board board = new Board(3, 3); // Create a new board

        // Generate an initial board
        AlgorithmXSolver.createXSudoku(board);

        for (int[] row : board.getGameBoard()) {
            for (int num : row) {
                boardString.append(num).append(",");
            }
            boardString.deleteCharAt(boardString.length() - 1);
            boardString.append(";");
        }
        boardString.deleteCharAt(boardString.length() - 1);
        broadcastMessage(boardString.toString()); // Broadcast the initial board to all clients
    }

    // Method to broadcast a message to all clients
    private void broadcastMessage(String message) {
        for (PrintWriter writer : clientWriters.values()) {
            writer.println(message);
        }
    }

    // Inner class to handle individual client connections
    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    processMessage(message); // Process each message from the client
                }
            } catch (IOException e) {
                logger.error("Error handling client message: {}", e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    synchronized (lock) {
                        clientWriters.remove(clientSocket); // Remove the client from the map
                    }
                } catch (IOException e) {
                    logger.error("Error stopping client socket: {}", e.getMessage());
                }
            }
        }

        // Method to process a message from the client
        private void processMessage(String message) {
            String[] parts = message.split(" ");
            String command = parts[0];

            switch (command) {
                case "CONNECT" -> {
                    synchronized (lock) {
                        connectedPlayers++;
                        int totalPlayers = 2;
                        if (connectedPlayers == totalPlayers) {
                            try {
                                sendInitialBoard(); // Send the initial board when all players are connected
                                broadcastMessage("READY"); // Notify all clients that the game is ready to start
                            } catch (Board.BoardNotCreatable e) {
                                logger.error("Error creating board: {}", e.getMessage());
                            }
                        }
                    }
                }
                case "DISCONNECT" -> handleClientDisconnection();
                case "COMPLETED" -> {
                    String playerName = parts[1];
                    announceWinner(playerName); // Announce the winner when a client completes the game
                }
            }
        }

        // Method to handle client disconnection
        private void handleClientDisconnection() {
            try {
                clientSocket.close();
                synchronized (lock) {
                    clientWriters.remove(clientSocket); // Remove the client from the map
                    connectedPlayers--; // Decrement the connected players count
                }
            } catch (IOException e) {
                logger.error("Error closing client socket: {}", e.getMessage());
            }
        }

        // Method to announce the winner
        private void announceWinner(String winner) {
            broadcastMessage("WINNER " + winner);
            broadcastMessage("COMPLETED " + winner);
        }
    }
}
