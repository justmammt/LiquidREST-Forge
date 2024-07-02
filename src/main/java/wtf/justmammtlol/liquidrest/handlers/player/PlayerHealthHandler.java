package wtf.justmammtlol.liquidrest.handlers.player;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static wtf.justmammtlol.liquidrest.RestServer.getPlayerByName;
import static wtf.justmammtlol.liquidrest.RestServer.queryToMap;

public class PlayerHealthHandler implements HttpHandler {

    /**
     * This method handles the HTTP request and response for the PlayerHealthHandler.
     * It checks the request parameters and returns the player's health if the player is found,
     * otherwise it returns a "Player not found" message.
     *
     * @param exchange The HttpExchange object representing the HTTP request and response.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the query parameters from the request URI
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());

        // Check if the "player" parameter is present
        if (params.get("player") != null) {
            // Get the player by name
            ServerPlayer player = getPlayerByName(params.get("player"));

            // Create the response message
            String response;
            if (player != null) {
                // Player found, return the health
                response = String.valueOf(player.getHealth());
                exchange.sendResponseHeaders(200, response.length());
            } else {
                // Player not found
                response = "Player not found";
                exchange.sendResponseHeaders(404, response.length());
            }

            // Write the response message to the response body
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        } else {
            // "player" parameter is not present
            String response = "You must specify a player query";
            exchange.sendResponseHeaders(400, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }
    }


}
