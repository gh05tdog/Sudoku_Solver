/* (C)2024 */
package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSDPClient {
    private static final String M_SEARCH_MESSAGE =
            """
                    M-SEARCH * HTTP/1.1\r
                    HOST: 239.255.255.250:1900\r
                    MAN: "ssdp:discover"\r
                    MX: 3\r
                    ST: ssdp:all\r
                    \r
                    """;
    private static final int SSDP_PORT = 1900;
    private static final String SSDP_IP = "239.255.255.250";
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final Logger logger = LoggerFactory.getLogger(SSDPClient.class);

    public List<String> discover() {
        List<String> servers = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {

            socket.setSoTimeout(5000);

            byte[] sendData = M_SEARCH_MESSAGE.getBytes();
            DatagramPacket sendPacket =
                    new DatagramPacket(
                            sendData, sendData.length, InetAddress.getByName(SSDP_IP), SSDP_PORT);
            socket.send(sendPacket);

            executorService.execute(
                    () -> {
                        byte[] receiveData = new byte[1024];
                        while (true) {
                            DatagramPacket receivePacket =
                                    new DatagramPacket(receiveData, receiveData.length);
                            try {
                                socket.receive(receivePacket);
                                String response =
                                        new String(
                                                receivePacket.getData(),
                                                0,
                                                receivePacket.getLength());
                                if (response.contains("LOCATION")) {
                                    String serverIp = extractIp(response);
                                    if (serverIp != null && !servers.contains(serverIp)) {
                                        servers.add(serverIp);
                                    }
                                }
                            } catch (IOException e) {
                                break; // Timeout reached, stop listening
                            }
                        }
                    });

            Thread.sleep(6000); // Wait for responses

        } catch (IOException | InterruptedException e) {
            logger.error("Failed to discover servers", e);
        }
        return servers;
    }

    private String extractIp(String response) {
        // Extract IP from LOCATION header
        String[] lines = response.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("LOCATION:")) {
                try {
                    URL url = new URL(line.split(" ", 2)[1]);
                    return url.getHost();
                } catch (MalformedURLException e) {
                    logger.error("Failed to extract IP from LOCATION header", e);
                }
            }
        }
        return null;
    }
}
