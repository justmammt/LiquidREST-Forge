package wtf.justmammtlol.liquidrest.handlers.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ServerLogsHandler implements HttpHandler {
    /**
     * Handles the HTTP GET request to retrieve the content of the latest log file.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            Path path = Paths.get("logs/latest.log");
            if (Files.exists(path)) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    // Create a StringBuilder to store the content of the log file
                    StringBuilder builder = new StringBuilder();
                    String line;
                    // Read each line from the log file and append it to the StringBuilder
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append('\n');
                    }
                    // Convert the content to bytes for the response
                    byte[] response = builder.toString().getBytes();
                    // Send a 200 OK response with the length of the content
                    exchange.sendResponseHeaders(200, response.length);
                    // Write the response content to the output stream
                    exchange.getResponseBody().write(response);
                }
            } else {
                // Respond with a 404 status code if the log file does not exist
                exchange.sendResponseHeaders(404, 0);
            }
            exchange.close();
        } else {
            // Respond with a 405 status code and error message for non-GET requests
            String response = "This handler is GET only.";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }
    }
}