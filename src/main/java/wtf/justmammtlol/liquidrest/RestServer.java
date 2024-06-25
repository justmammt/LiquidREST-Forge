package wtf.justmammtlol.liquidrest;

import com.mojang.logging.LogUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


public class RestServer {
    static HttpServer server;

    static {
        try {
            server = HttpServer.create(new InetSocketAddress(LiquidRESTServerConfigs.WEBSERVER_PORT.get()), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public void main() {


        server.createContext("/", new RootHandler());
        server.createContext("/player/health", new PlayerHealthHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(LiquidRESTServerConfigs.WEBSERVER_FIXED_THREADS_COUNT.get()));
        server.start();

        LOGGER.info("LiquidREST is running on port " + LiquidRESTServerConfigs.WEBSERVER_PORT.get());

    }

    public static void stop() {
        server.stop(1);
        LOGGER.info("Stopped LiquidREST threads");
    }

    public static Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "It works!";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            exchange.close();
        }
    }

    class PlayerHealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            if (params.get("player") != null) {

                ServerPlayer player = getPlayerByName(params.get("player"));
                if (player != null) {
                    String response = String.valueOf(player.getHealth());
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    exchange.close();
                } else {
                    String response = "Player not found";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    exchange.close();
                }

            } else if (params.get("player") == null) {
                String response = "You must specify a player query";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                exchange.close();
            }
        }
    }

    private ServerPlayer getPlayerByName(String player) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(player);
    }
}
