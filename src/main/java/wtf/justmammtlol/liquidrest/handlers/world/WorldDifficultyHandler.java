package wtf.justmammtlol.liquidrest.handlers.world;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.OutputStream;

public class WorldDifficultyHandler implements HttpHandler {

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    /**
     * Handles the HTTP request and sends the current world difficulty display name as the response.
     * Supports only GET requests.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            // Get the current world difficulty display name
            String response = server.overworld().getDifficulty().getDisplayName().getString();
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
