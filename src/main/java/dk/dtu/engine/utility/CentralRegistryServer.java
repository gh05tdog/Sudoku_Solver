package dk.dtu.engine.utility;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CentralRegistryServer {
    private static final int PORT = 8080;
    private static final Map<String, String> servers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/register", new RegisterHandler());
        server.createContext("/discover", new DiscoverHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Central registry server started on port " + PORT);
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
                String serverName = request.getString("name");
                String serverAddress = request.getString("address");
                servers.put(serverName, serverAddress);
                String response = "Server registered";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class DiscoverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                JSONArray response = new JSONArray();
                for (Map.Entry<String, String> entry : servers.entrySet()) {
                    JSONObject server = new JSONObject();
                    server.put("name", entry.getKey());
                    server.put("address", entry.getValue());
                    response.put(server);
                }
                byte[] responseBytes = response.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}