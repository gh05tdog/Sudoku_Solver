package dk.dtu.engine.utility;

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
    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private int clientCount = 0;
    private final int[][] initialBoard = {
            {3, 0, 9, 0, 0, 2, 5, 1, 6},
            {0, 7, 0, 0, 0, 9, 2, 4, 8},
            {1, 8, 2, 0, 0, 6, 9, 3, 0},
            {5, 3, 0, 6, 7, 0, 8, 2, 0},
            {0, 6, 7, 9, 0, 0, 0, 5, 3},
            {9, 0, 0, 3, 0, 0, 6, 7, 4},
            {0, 0, 0, 4, 5, 0, 3, 8, 1},
            {0, 0, 0, 0, 9, 1, 0, 6, 0},
            {8, 1, 4, 2, 0, 0, 7, 9, 5}
    };

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.put(clientSocket, writer);
                threadPool.execute(new ClientHandler(clientSocket));

                synchronized (lock) {
                    clientCount++;
                    broadcastMessage("A new player has connected!");
                    if (clientCount == 2) {
                        sendInitialBoard();
                        broadcastMessage("READY"); // Notify all clients that the game is ready to start
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    private void sendInitialBoard() {
        StringBuilder boardString = new StringBuilder("INITIAL_BOARD ");
        for (int[] row : initialBoard) {
            for (int num : row) {
                boardString.append(num).append(",");
            }
            boardString.deleteCharAt(boardString.length() - 1);
            boardString.append(";");
        }
        boardString.deleteCharAt(boardString.length() - 1);
        broadcastMessage(boardString.toString());
    }

    private void broadcastMessage(String message) {
        for (PrintWriter writer : clientWriters.values()) {
            writer.println(message);
        }
    }

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
                    processMessage(message);
                }
            } catch (IOException e) {
                logger.error("Error handling client message: {}", e.getMessage());

            } finally {
                try {
                    clientSocket.close();
                    synchronized (lock) {
                        clientCount--;
                        clientWriters.remove(clientSocket);
                    }
                } catch (IOException e) {
                    logger.error("Error closing client socket: {}", e.getMessage());
                }
            }
        }

        private void processMessage(String message) {
            String[] parts = message.split(" ");
            String command = parts[0];

            switch (command) {
                case "COMPLETED":
                    String playerName = parts[1];
                    announceWinner(playerName);
                    break;
                // Handle other commands if needed
            }
        }

        private void announceWinner(String winner) {
            broadcastMessage("WINNER " + winner);
            broadcastMessage("COMPLETED " + winner);
        }
    }
}
