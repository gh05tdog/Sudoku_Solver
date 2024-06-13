package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSender {
    public static void main(String[] args) {
        String multicastAddress = "239.255.255.250";
        int port = 1900;
        String message = "Multicast test message";

        try {
            InetAddress group = InetAddress.getByName(multicastAddress);
            MulticastSocket socket = new MulticastSocket();
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, port);
            socket.send(packet);
            socket.close();
            System.out.println("Multicast message sent.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}