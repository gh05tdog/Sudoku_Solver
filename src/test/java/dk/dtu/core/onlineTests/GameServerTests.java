package dk.dtu.core.onlineTests;

import dk.dtu.engine.utility.GameServer;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.mockito.Mockito.*;

class GameServerTest {
    private GameServer server;
    private Thread serverThread;

    @BeforeEach
    void setUp() {
        server = Mockito.spy(new GameServer() {
            @Override
            protected void startServerSocket() {
                try {
                    serverSocket = new ServerSocket(0); // Use any available port
                    System.out.println("Server started on port " + serverSocket.getLocalPort());
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
            if (server.serverSocket != null && !server.serverSocket.isClosed()) {
                server.serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testClientConnection() {
        try (Socket clientSocket = new Socket("localhost", server.serverSocket.getLocalPort())){

            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();

            // Send a test message
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("TEST_MESSAGE");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();

            Assertions.assertNotNull(response);

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
        AlgorithmXSolver.createXSudoku(board);

        server.clientWriters.put(mockSocket, mockWriter);
        server.sendInitialBoard();

        String output = mockOutputStream.toString();
        Assertions.assertTrue(output.contains("INITIAL_BOARD"));
    }
}
