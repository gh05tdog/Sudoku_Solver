package dk.dtu.engine.utility;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private static final int PORT = 12345;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    public final ConcurrentHashMap<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private int clientCount = 0;
    public ServerSocket serverSocket;
    private boolean running = true;

    public void start() {
        startServerSocket();
        acceptClients();
    }

    protected void startServerSocket() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    private void acceptClients() {
        while (running) {
            try {
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
            } catch (IOException | Board.BoardNotCreatable e) {
                if (running) {
                    logger.error("Error accepting client: {}", e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }
    }

    public void sendInitialBoard() throws Board.BoardNotCreatable {
        StringBuilder boardString = new StringBuilder("INITIAL_BOARD ");

        Board board = new Board(3, 3);

        // Generate a initial board
        AlgorithmXSolver.createXSudoku(board);

        for (int[] row : board.getGameBoard()) {
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

            if (command.equals("COMPLETED")) {
                String playerName = parts[1];
                announceWinner(playerName);
                // Handle other commands if needed
            }
        }

        private void announceWinner(String winner) {
            broadcastMessage("WINNER " + winner);
            broadcastMessage("COMPLETED " + winner);
        }
    }
}