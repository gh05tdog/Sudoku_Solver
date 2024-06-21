/* (C)2024 */
package dk.dtu.core.onlineTests;

import static org.mockito.Mockito.*;

import dk.dtu.engine.utility.GameServer;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

class GameServerTest {
    private GameServer server;
    private Thread serverThread;

    @BeforeEach
    void setUp() {
        server =
                Mockito.spy(
                        new GameServer() {
                            @Override
                            protected void startServerSocket() {
                                try {
                                    ServerSocket serverSocket = new ServerSocket(0); // Use any available port
                                    System.out.println(
                                            "Server started on port "
                                                    + serverSocket.getLocalPort());
                                } catch (IOException e) {
                                    System.out.println("Error starting server: " + e.getMessage());
                                }
                            }
                        });

        serverThread = new Thread(() -> server.start());
        serverThread.start();

        try {
            // Allow some time for the server to start
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        server.stop();
        serverThread.interrupt();
        try {
            if (server.getServerSocket() != null && !server.getServerSocket().isClosed()) {
                server.getServerSocket().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testInitialBoardSent() throws Exception {
        Socket mockSocket = Mockito.mock(Socket.class);
        OutputStream mockOutputStream = new ByteArrayOutputStream();
        PrintWriter mockWriter = new PrintWriter(mockOutputStream, true);

        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.isConnected()).thenReturn(true);

        Board board = new Board(3, 3);
        Config.setDifficulty("medium");
        AlgorithmXSolver.createXSudoku(board);

        server.clientWriters.put(mockSocket, mockWriter);
        server.sendInitialBoard();

        String output = mockOutputStream.toString();
        Assertions.assertTrue(output.contains("INITIAL_BOARD"));
    }
}
