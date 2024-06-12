package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerBroadCaster implements Runnable {
    private static final int BROADCAST_INTERVAL = 5000; // 5 seconds
    private static final int BROADCAST_PORT = 12346;
    private String serverAddress;
    private String message;

    public ServerBroadCaster(String serverAddress) {
        this.serverAddress = serverAddress;
        this.message = "SUDOKU_SERVER " + serverAddress;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), BROADCAST_PORT);
            while (true) {
                socket.send(packet);
                Thread.sleep(BROADCAST_INTERVAL);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}