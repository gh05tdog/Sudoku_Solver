/* (C)2024 */
package dk.dtu.core.onlineTests;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.utility.GameClient;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.SudokuGame;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

class GameClientTest {
    private GameClient client;
    private ServerSocket serverSocket;
    private Socket clientSocket;

    @BeforeEach
    void setUp() throws Exception {
        WindowManager windowManager = Mockito.mock(WindowManager.class);

        // Start a real server socket to simulate the server
        serverSocket = new ServerSocket(12345);

        // Create a client that will connect to the local server
        client = new GameClient( windowManager);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    void testInitialBoardReceived() throws Exception {
        // Start the client in a separate thread to prevent blocking
        Thread clientThread =
                new Thread(
                        () -> {
                            try {
                                client.start("localhost");
                            } catch (IOException | Board.BoardNotCreatable e) {
                                e.printStackTrace();
                            }
                        });
        clientThread.start();

        // Accept the client connection on the server side
        clientSocket = serverSocket.accept();

        // Verify the socket interaction
        OutputStream outputStream = clientSocket.getOutputStream();
        clientSocket.getInputStream();

        // Simulate sending the INITIAL_BOARD message from the server
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println("INITIAL_BOARD 1,2,3,4,5,6,7,8,9;");

        // Allow some time for the client to process the message
        Thread.sleep(1000);

        // Stop the client thread
        clientThread.interrupt();

        // Verify that the initial board has been received and processed
        SudokuGame game = client.getGame();
        assertNotNull(game, "Game should be initialized");
        int[][] board = game.gameboard.getGameBoard();
        assertNotNull(board, "Board should be initialized");
        assertEquals(1, board[0][0], "First cell should be 1");
    }
}
