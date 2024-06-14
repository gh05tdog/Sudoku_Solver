package dk.dtu.engine.utility;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SSDPServer {
    private static final String SSDP_ALIVE_MESSAGE =
            "NOTIFY * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "CACHE-CONTROL: max-age=1800\r\n" +
                    "LOCATION: http://%s:%d/description.xml\r\n" +
                    "NT: upnp:rootdevice\r\n" +
                    "NTS: ssdp:alive\r\n" +
                    "SERVER: SSDPServer/1.0 UPnP/1.1\r\n" +
                    "USN: uuid:your-unique-id::upnp:rootdevice\r\n\r\n";
    private static final String SSDP_RESPONSE_MESSAGE =
            "HTTP/1.1 200 OK\r\n" +
                    "CACHE-CONTROL: max-age=1800\r\n" +
                    "DATE: %s\r\n" +
                    "EXT:\r\n" +
                    "LOCATION: http://%s:%d/description.xml\r\n" +
                    "SERVER: SSDPServer/1.0 UPnP/1.1\r\n" +
                    "ST: upnp:rootdevice\r\n" +
                    "USN: uuid:your-unique-id::upnp:rootdevice\r\n\r\n";
    private static final int SSDP_PORT = 1900;
    private static final String SSDP_IP = "239.255.255.250";
    private final String localIpAddress;
    private final int localPort;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SSDPServer(String localIpAddress, int localPort) {
        this.localIpAddress = localIpAddress;
        this.localPort = localPort;
    }

    public void start() {
        startAnnouncement();
        startResponseHandler();
    }

    private void startAnnouncement() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String message = String.format(SSDP_ALIVE_MESSAGE, localIpAddress, localPort);
                DatagramSocket socket = new DatagramSocket();
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(SSDP_IP), SSDP_PORT);
                socket.send(packet);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 15, TimeUnit.MINUTES); // SSDP alive messages are sent every 15 minutes
    }

    private void startResponseHandler() {
        new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(SSDP_PORT)) {
                InetAddress group = InetAddress.getByName(SSDP_IP);
                socket.joinGroup(new InetSocketAddress(group, SSDP_PORT), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

                byte[] buf = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (message.startsWith("M-SEARCH")) {
                        String response = String.format(SSDP_RESPONSE_MESSAGE, new java.util.Date().toString(), localIpAddress, localPort);
                        DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort());
                        socket.send(responsePacket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}