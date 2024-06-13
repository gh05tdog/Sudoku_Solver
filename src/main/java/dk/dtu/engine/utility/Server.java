package dk.dtu.engine.utility;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Server {
    private static final String CENTRAL_SERVER_URL = "http://localhost:8080/register";
    private static final String SERVER_NAME = "MyGameServer";
    private static final String SERVER_ADDRESS = NetworkUtils.getLocalIpAddress(); // Change to your server's address

    public static void main(String[] args) {
        try {
            registerServer(SERVER_NAME, SERVER_ADDRESS);
            System.out.println("Game server registered successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerServer(String name, String address) throws Exception {
        URL url = new URL(CENTRAL_SERVER_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject request = new JSONObject();
        request.put("name", name);
        request.put("address", address);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(request.toString().getBytes());
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Server registered successfully.");
        } else {
            System.out.println("Failed to register server.");
        }
    }
}