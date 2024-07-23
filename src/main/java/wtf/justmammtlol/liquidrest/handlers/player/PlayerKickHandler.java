package wtf.justmammtlol.liquidrest.handlers.player;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.json.stream.JsonParsingException;
import java.io.IOException;
import java.io.OutputStream;

import static wtf.justmammtlol.liquidrest.RestServer.*;

public class PlayerKickHandler implements HttpHandler {

    Gson gson = new Gson();

    static class Request {
        String player = null;
    }

    /**
     * Handles the HTTP PATCH request to kick a player.
     *
     * @param exchange the HttpExchange object containing the request and response
     * @throws IOException if an I/O error occurs
     * @throws JsonParsingException if there is an error parsing JSON
     * @since 0.3.0.a-1.18.2
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException, JsonParsingException {

        // Check if the request method is PATCH
        if (exchange.getRequestMethod().equalsIgnoreCase("PATCH")) {
            // Check if there is a request body
            if (exchange.getRequestBody().available() != 0) {
                // Parse the request body into a Request object
                String requestBody = InputStreamToString(exchange.getRequestBody());
                Request request = gson.fromJson(requestBody, Request.class);

                // Check if the player is specified in the request
                if (request.player != null) {
                    // Get the player by name
                    ServerPlayer player = getPlayerByName(request.player);
                    // Check if the player exists
                    if (getPlayerByName(request.player) != null) {
                        // Kick the player and send a success response
                        assert player != null;
                        player.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.kicked"));
                        String response = "Successfully kicked " + request.player;
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        exchange.close();
                    } else {
                        // Send a response indicating player doesn't exist or is offline
                        String response = "Player doesn't exist or is offline";
                        exchange.sendResponseHeaders(409, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        exchange.close();
                    }

                }
            } else {
                // Send a response indicating player must be specified
                String response = "You must specify a player";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                exchange.close();
            }
        } else {
            // Send a response indicating handler is PATCH only
            String response = "This handler is PATCH only.";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }
    }
}