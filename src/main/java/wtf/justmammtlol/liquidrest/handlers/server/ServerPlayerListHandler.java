package wtf.justmammtlol.liquidrest.handlers.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.OutputStream;

public class ServerPlayerListHandler implements HttpHandler {

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    Gson gson = new Gson();


    /**
     * Handles the HTTP request and sends the list of player names as the response.
     * Supports only GET requests.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Check if the request method is GET
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            // Get the list of player names and convert it to JSON
            String response = gson.toJson(server.getPlayerNames());

            // Set the response headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            // Send the response
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        } else {
            // Respond with an error message for non-GET requests
            String response = "This handler is GET only.";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }
    }
}
