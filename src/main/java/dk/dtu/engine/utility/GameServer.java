/* (C)2024 */
package dk.dtu.engine.utility;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GameServer class is responsible for starting the game server and accepting client connections.
 * It listens for messages from clients and processes them accordingly.
 */
public class  GameServer {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
    private static final int PORT = 12345;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    public final ConcurrentMap<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private int connectedPlayers = 0;
    public ServerSocket serverSocket;
    private boolean running = true;
    public static String LOCAL_IP_ADDRESS;
    JDialog serverDialog;

    static {
        try {
            LOCAL_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        startServerSocket();
        startSSDPServer();
        SwingUtilities.invokeLater(this::showServerDialog); // Ensure dialog is created on EDT
        acceptClients();
    }

    protected void startServerSocket() {
        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("Server started on port {}", serverSocket.getLocalPort());
        } catch (IOException e) {
            logger.error("Error starting server: {}", e.getMessage());
        }
    }

    private void startSSDPServer() {
        try {
            String localIp = getLocalIpAddress();
            SSDPServer ssdpServer = new SSDPServer(localIp, PORT);
            ssdpServer.start();
        } catch (IOException e) {
            logger.error("Error starting SSDP server: {}", e.getMessage());
        }
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.put(clientSocket, writer);
                threadPool.execute(new ClientHandler(clientSocket));

            } catch (IOException e) {
                logger.error("Error accepting client: {}", e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            // Close all client connections
            for (Socket clientSocket : clientWriters.keySet()) {
                clientSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }
        threadPool.shutdown(); // Ensure the thread pool is also shut down
    }

    // Send the initial board to all clients
    public void sendInitialBoard() throws Board.BoardNotCreatable {

        if (serverDialog != null) { // Close the dialog when a client connects
            serverDialog.dispose();
            serverDialog = null;
        }

        StringBuilder boardString = new StringBuilder("INITIAL_BOARD ");

        Board board = new Board(3, 3);

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
        broadcastMessage(boardString.toString());
    }

    private void broadcastMessage(String message) {
        for (PrintWriter writer : clientWriters.values()) {
            writer.println(message);
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream())) ) {
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
                        clientWriters.remove(clientSocket);
                    }
                } catch (IOException e) {
                    closingSocketErr(e);
                }
            }
        }

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
                                sendInitialBoard();
                                broadcastMessage(
                                        "READY"); // Notify all clients that the game is ready to
                                // start
                            } catch (Board.BoardNotCreatable e) {
                                logger.error("Error creating board: {}", e.getMessage());
                            }
                        }
                    }
                }
                case "DISCONNECT" -> handleClientDisconnection();
                case "COMPLETED" -> {
                    String playerName = parts[1];
                    announceWinner(playerName);
                }
                case "PROGRESS" ->
                        broadcastMessage(message); // Forward the progress message to all clients
            }
        }

        private void handleClientDisconnection() {
            try {
                clientSocket.close();
                synchronized (lock) {
                    clientWriters.remove(clientSocket);
                    connectedPlayers--;
                }
            } catch (IOException e) {
                closingSocketErr(e);
            }
        }

        private void announceWinner(String winner) {
            broadcastMessage("WINNER " + winner);
            broadcastMessage("COMPLETED " + winner);
        }
    }

    private String getLocalIpAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                    return address.getHostAddress();
                }
            }
        }
        throw new SocketException("No non-loopback IPv4 address found.");
    }

    public static void closingSocketErr(IOException e) {
        logger.error("Error closing client socket: {}", e.getMessage());
    }

    private void showServerDialog() {
        serverDialog = new JDialog();
        serverDialog.setTitle("Server Started");
        serverDialog.setModal(true);
        serverDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JLabel messageLabel =
                new JLabel(
                        "Server started on IP: "
                                + LOCAL_IP_ADDRESS
                                + ". Waiting for clients to join.");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        serverDialog.add(messageLabel);
        serverDialog.setSize(500, 150);
        serverDialog.setLocationRelativeTo(null);
        serverDialog.setVisible(true);
    }
}
