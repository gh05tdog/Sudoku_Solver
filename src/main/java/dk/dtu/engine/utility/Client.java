package dk.dtu.engine.utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final int BROADCAST_PORT = 8888;
    private static final int TIMEOUT = 2000; // 2 seconds

    public static void main(String[] args) {
        List<String> servers = discoverServers();
        for (String server : servers) {
            System.out.println("Found server: " + server);
        }
    }

    public static List<String> discoverServers() {
        List<String> servers = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT);

            String message = "DISCOVER_SERVER_REQUEST";
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, InetAddress.getByName("255.255.255.255"), BROADCAST_PORT
            );
            socket.send(packet);

            byte[] recvBuf = new byte[256];
            while (true) {
                DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                try {
                    socket.receive(recvPacket);
                    String received = new String(recvPacket.getData(), 0, recvPacket.getLength());
                    if ("DISCOVER_SERVER_RESPONSE".equals(received)) {
                        String serverAddress = recvPacket.getAddress().getHostAddress();
                        if (!servers.contains(serverAddress)) {
                            servers.add(serverAddress);
                        }
                    }
                } catch (Exception e) {
                    break; // Timeout reached, stop listening
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }
}