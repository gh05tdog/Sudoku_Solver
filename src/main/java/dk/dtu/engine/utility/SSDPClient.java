package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SSDPClient {
    private static final String M_SEARCH_MESSAGE =
            "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: ssdp:all\r\n\r\n";
    private static final int SSDP_PORT = 1900;
    private static final String SSDP_IP = "239.255.255.250";
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public List<String> discover() {
        List<String> servers = new ArrayList<>();
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000);

            byte[] sendData = M_SEARCH_MESSAGE.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(SSDP_IP), SSDP_PORT);
            socket.send(sendPacket);
            System.out.println("SSDP discovery message sent.");

            executorService.execute(() -> {
                byte[] receiveData = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        socket.receive(receivePacket);
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("Received SSDP response: " + response);
                        if (response.contains("LOCATION")) {
                            String serverIp = extractIp(response);
                            if (serverIp != null && !servers.contains(serverIp)) {
                                servers.add(serverIp);
                                System.out.println("Discovered server: " + serverIp);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Socket timeout reached or error occurred: " + e.getMessage());
                        break; // Timeout reached, stop listening
                    }
                }
            });

            Thread.sleep(6000); // Wait for responses
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return servers;
    }

    private String extractIp(String response) {
        // Extract IP from LOCATION header
        String[] lines = response.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("LOCATION:")) {
                try {
                    URL url = new URL(line.split(" ", 2)[1].trim());
                    return url.getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}