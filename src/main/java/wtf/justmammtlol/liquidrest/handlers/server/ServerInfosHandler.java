package wtf.justmammtlol.liquidrest.handlers.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.OutputStream;

public class ServerInfosHandler implements HttpHandler {

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    public class Infos {
        String version = server.getServerVersion();
        Integer port = server.getPort();
        String motd = server.getMotd();
        Float avgTick = server.getAverageTickTime();
        Integer playerCount = server.getPlayerCount();
        Integer maxPlayerCount = server.getMaxPlayers();
        Long usedMemoryInMB = Math.subtractExact(Runtime.getRuntime().maxMemory(), Runtime.getRuntime().freeMemory()) / 1_000_000;
        Long maxMemoryInMB = Runtime.getRuntime().maxMemory() / 1_000_000;

    }

    Gson gson = new Gson();

    /**
     *
     * @param exchange the exchange containing the request from the
     *                 client and used to send the response
     * @throws IOException if an I/O error occurs
     * @since 0.4.0b-1.18.2
     */

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            Infos infos = new Infos();
            String response = gson.toJson(infos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        } else {
            String response = "This handler is GET only.";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }

    }
}
