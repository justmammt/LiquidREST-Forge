package wtf.justmammtlol.liquidrest.handlers.player;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import java.io.IOException;
import java.io.OutputStream;

import static wtf.justmammtlol.liquidrest.RestServer.*;

public class PlayerKillHandler implements HttpHandler {

    Gson gson = new Gson();

    static class Request {
        String player = null;
    }

    @Override
    /**
     * Handles the HTTP PATCH request to kill a player.
     *
     * @param exchange the HttpExchange object containing the request and response
     * @throws IOException if an I/O error occurs
     */
    public void handle(HttpExchange exchange) throws IOException {
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
                    if (player != null) {
                        // Kill the player and send a success response
                        player.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
                        LOGGER.info(player.getName().getString() + " killed by " + exchange.getRemoteAddress());
                        String response = "Successfully killed " + request.player;
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
                } else {
                    // Send a response indicating player must be specified
                    String response = "You must specify a player";
                    exchange.sendResponseHeaders(400, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    exchange.close();
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
            // Send a response indicating that this handler is PATCH only
            String response = "This handler is PATCH only.";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }
    }
}
