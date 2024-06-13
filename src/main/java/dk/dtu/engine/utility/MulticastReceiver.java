package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver {
    public static void main(String[] args) {
        String multicastAddress = "239.255.255.250";
        int port = 1900;

        try {
            InetAddress group = InetAddress.getByName(multicastAddress);
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Waiting for multicast message...");
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received multicast message: " + message);

            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}