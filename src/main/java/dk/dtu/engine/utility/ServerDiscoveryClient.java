package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerDiscoveryClient implements Runnable {
    private static final int BROADCAST_PORT = 12346;
    private List<String> discoveredServers = new ArrayList<>();

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(BROADCAST_PORT, InetAddress.getByName("0.0.0.0"))) {
            socket.setBroadcast(true);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("SUDOKU_SERVER")) {
                    String serverAddress = message.split(" ")[1];
                    if (!discoveredServers.contains(serverAddress)) {
                        discoveredServers.add(serverAddress);
                        System.out.println("Discovered server: " + serverAddress);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getDiscoveredServers() {
        return new ArrayList<>(discoveredServers); // Return a copy of the list
    }
}