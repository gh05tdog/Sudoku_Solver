package dk.dtu.engine.utility;

import dk.dtu.engine.core.WindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.SudokuGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private final String serverAddress;
    private final WindowManager windowManager;

    public GameClient(String serverAddress, WindowManager windowManager) {
        this.serverAddress = serverAddress;
        this.windowManager = windowManager;
    }

    public void start() throws IOException, Board.BoardNotCreatable {
        try (Socket socket = new Socket(serverAddress, 12345)) {
            System.out.println("Connected to server at " + serverAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            SudokuGame game = new SudokuGame(windowManager, 3, 3, 550 / 9); // Adjust as needed
            game.setNetworkOut(out); // Pass the network output stream to the game

            game.setNetworkGame(true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message); // Display the message in the console
                processNetworkMessage(message, game);
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        } finally {
            System.out.println("Connection closed");
        }


    }

    private void processNetworkMessage(String message, SudokuGame game) {
        String[] parts = message.split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "INITIAL_BOARD":
                int[][] board = stringToBoard(parts[1]);
                game.initializeCustom(board);
                break;
            case "WINNER":
                game.processNetworkMessage(message);
                break;
            // Handle other commands if needed
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
}
