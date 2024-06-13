package dk.dtu.engine.utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[256];
            System.out.println("Server listening on port " + PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                if ("DISCOVER_SERVER_REQUEST".equals(received)) {
                    String response = "DISCOVER_SERVER_RESPONSE";
                    byte[] responseData = response.getBytes();

                    DatagramPacket responsePacket = new DatagramPacket(
                            responseData, responseData.length, packet.getAddress(), packet.getPort()
                    );
                    socket.send(responsePacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}