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
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        discoverServers();

        Socket socket = createSocket(serverAddress);
        System.out.println("Connected to server at " + serverAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Send connect signal
        out.println("CONNECT");

        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        game.setNetworkOut(out);
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
                System.out.println("Received initial board: " + parts[1]);
                int[][] board = stringToBoard(parts[1]);
                Config.setK(3);
                Config.setN(3);
                Config.setEnableEasyMode(false);
                Config.setEnableLives(false);
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

    public void discoverServers() {
        SSDPClient ssdpClient = new SSDPClient();
        List<String> servers = ssdpClient.discover();

        for (String server : servers) {
            System.out.println("Found server: " + server);
        }
    }
}

class SSDPClient {
    private static final String M_SEARCH_MESSAGE =
            "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: ssdp:all\r\n\r\n";
    private static final int SSDP_PORT = 1900;
    private static final String SSDP_IP = "239.255.255.250";
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public List<String> discover() {
        List<String> servers = new ArrayList<>();
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000);

            byte[] sendData = M_SEARCH_MESSAGE.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(SSDP_IP), SSDP_PORT);
            socket.send(sendPacket);

            executorService.execute(() -> {
                byte[] receiveData = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        socket.receive(receivePacket);
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (response.contains("LOCATION")) {
                            String serverIp = extractIp(response);
                            if (serverIp != null && !servers.contains(serverIp)) {
                                servers.add(serverIp);
                            }
                        }
                    } catch (IOException e) {
                        break; // Timeout reached, stop listening
                    }
                }
            });

            Thread.sleep(6000); // Wait for responses

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return servers;
    }

    private String extractIp(String response) {
        // Extract IP from LOCATION header
        String[] lines = response.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("LOCATION:")) {
                try {
                    URL url = new URL(line.split(" ", 2)[1]);
                    return url.getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}