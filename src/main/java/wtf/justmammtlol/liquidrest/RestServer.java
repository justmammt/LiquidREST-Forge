package wtf.justmammtlol.liquidrest;

import com.mojang.logging.LogUtils;

import com.sun.net.httpserver.*;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import javax.json.stream.JsonParsingException;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.Executors.newFixedThreadPool;


public class RestServer {
    static HttpServer server;
    static Gson gson = new Gson();

    static {
        try {
            server = HttpServer.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public void main() throws IOException {


        server.createContext("/", new RootHandler());
        server.createContext("/player/health", new PlayerHealthHandler());
        server.createContext("/player/kick", new PlayerKickHandler())
                .setAuthenticator(new BasicAuthenticator("PlayerKickHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(LiquidRESTServerConfigs.WEBSERVER_BASICAUTH_USER.get()) && pwd.equals(LiquidRESTServerConfigs.WEBSERVER_BASICAUTH_PASS.get());
                    }
                });

        server.setExecutor(newFixedThreadPool(4));
        server.bind(new InetSocketAddress(LiquidRESTServerConfigs.WEBSERVER_PORT.get()), -1);
        server.start();

        LOGGER.info("LiquidREST is running on  " + server.getAddress());

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

                String response;
                if (player != null) {
                    response = String.valueOf(player.getHealth());
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                } else {
                    response = "Player not found";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                }
                exchange.close();

            } else if (params.get("player") == null) {
                String response = "You must specify a player query";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                exchange.close();
            }
        }
    }

    class PlayerKickHandler implements HttpHandler {
        class Request {
            String player = null;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException, JsonParsingException {
            String requestBody = InputStreamToString(exchange.getRequestBody());
            if (exchange.getRequestMethod().equalsIgnoreCase("PATCH")) {
                Request request = gson.fromJson(requestBody, Request.class);

                if (request.player != null) {
                    ServerPlayer player = getPlayerByName(request.player);
                    if (getPlayerByName(request.player) != null) {
                        player.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.kicked"));
                        String response = "Successfully kicked " + request.player;
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        exchange.close();
                    } else {
                        String response = "Player doesn't exist or is offline";
                        exchange.sendResponseHeaders(409, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        exchange.close();
                    }

                } else {
                    String response = "You must specify a player";
                    exchange.sendResponseHeaders(409, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    exchange.close();
                }
            } else {

                String response = "This handler is PATCH only.";
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                exchange.close();
            }
        }
    }


    // Start of reusable functions
    @Nullable
    public ServerPlayer getPlayerByName(String player) {
        for (ServerPlayer serverplayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (serverplayer.getGameProfile().getName().equalsIgnoreCase(player)) {
                return serverplayer;
            }
        }
        return null;
    }


    private static String InputStreamToString(InputStream istr) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = istr.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

}
