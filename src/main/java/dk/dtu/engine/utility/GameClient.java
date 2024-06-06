package dk.dtu.engine.utility;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.SudokuGame;
import dk.dtu.engine.core.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private final String serverAddress;
    private final WindowManager windowManager;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameClient(String serverAddress, WindowManager windowManager) {
        this.serverAddress = serverAddress;
        this.windowManager = windowManager;
    }

    public void start() throws IOException, Board.BoardNotCreatable {
        socket = new Socket(serverAddress, 12345);
        System.out.println("Connected to server at " + serverAddress);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received message: " + message); // Display the message in the console
            processNetworkMessage(message);
        }
    }

    private void processNetworkMessage(String message) throws Board.BoardNotCreatable {
        String[] parts = message.split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "INITIAL_BOARD":
                int[][] board = stringToBoard(parts[1]);
                // Print the board to the console
                for (int[] row : board) {
                    for (int num : row) {
                        System.out.print(num + " ");
                    }
                    System.out.println();
                }
                SudokuGame game = new SudokuGame(windowManager, 3, 3, 550 / 9); // Adjust as needed
                game.initializeCustom(board);
                break;
            case "READY":
                System.out.println("Server is ready. Starting game...");
                break;
            case "WINNER":
                String winner = parts[1];
                announceWinner(winner);
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

    private void announceWinner(String winner) {
        System.out.println("The winner is: " + winner);
    }
}
