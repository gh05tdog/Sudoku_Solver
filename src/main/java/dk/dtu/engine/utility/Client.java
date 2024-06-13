package dk.dtu.engine.utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final String CENTRAL_SERVER_URL = "http://localhost:8080/discover";

    public static void main(String[] args) {
        try {
            List<String> servers = discoverServers();
            for (String server : servers) {
                System.out.println("Found server: " + server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> discoverServers() throws Exception {
        List<String> servers = new ArrayList<>();

        URL url = new URL(CENTRAL_SERVER_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject server = jsonArray.getJSONObject(i);
                String serverName = server.getString("name");
                String serverAddress = server.getString("address");
                servers.add(serverName + " - " + serverAddress);
            }
        } else {
            System.out.println("Failed to discover servers.");
        }

        return servers;
    }
}