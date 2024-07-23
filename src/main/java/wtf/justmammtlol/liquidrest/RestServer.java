package wtf.justmammtlol.liquidrest;

import com.mojang.logging.LogUtils;

import com.sun.net.httpserver.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import wtf.justmammtlol.liquidrest.handlers.player.PlayerHealthHandler;
import wtf.justmammtlol.liquidrest.handlers.player.PlayerKickHandler;
import wtf.justmammtlol.liquidrest.handlers.player.PlayerKillHandler;
import wtf.justmammtlol.liquidrest.handlers.server.ServerInfosHandler;
import wtf.justmammtlol.liquidrest.handlers.server.ServerLogsHandler;
import wtf.justmammtlol.liquidrest.handlers.server.ServerPlayerListHandler;
import wtf.justmammtlol.liquidrest.handlers.world.WorldDifficultyHandler;
import wtf.justmammtlol.liquidrest.handlers.app.AppAuthHandler;

import javax.annotation.Nullable;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class RestServer {
    /**
     * The HttpServer instance for the REST server.
     */
    static HttpServer server;

    /**
     * The username for basic authentication.
     */
    static String username = LiquidRESTServerConfigs.WEBSERVER_BASICAUTH_USER.get();

    /**
     * The password for basic authentication.
     */
    static String password = LiquidRESTServerConfigs.WEBSERVER_BASICAUTH_PASS.get();

    /**
     * This static block initializes the server upon class loading.
     * It throws a RuntimeException if the server creation fails.
     */
    static {
        try {
            server = HttpServer.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The logger for this class.
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    public void main() throws IOException {


        // Set up the root handler
        server.createContext("/", new RootHandler());

        // Set up player handlers
        server.createContext("/player/health", new PlayerHealthHandler());
        server.createContext("/player/kick", new PlayerKickHandler()) // Handles kicking a player
                .setAuthenticator(new BasicAuthenticator("PlayerKickHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(username) && pwd.equals(password);
                    }
                });
        server.createContext("/player/kill", new PlayerKillHandler()) // Handles killing a player
                .setAuthenticator(new BasicAuthenticator("PlayerKillHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(username) && pwd.equals(password);
                    }
                });

        // Set up server handlers
        server.createContext("/server/player/list", new ServerPlayerListHandler()) // Handles listing players on the server
                .setAuthenticator(new BasicAuthenticator("ServerPlayerListHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(username) && pwd.equals(password);
                    }
                });
        server.createContext("/server/infos", new ServerInfosHandler()) // Handles server information
                .setAuthenticator(new BasicAuthenticator("ServerInfosHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(username) && pwd.equals(password);
                    }
                });
        server.createContext("/server/logs", new ServerLogsHandler()) // Handles server logs
                .setAuthenticator(new BasicAuthenticator("ServerLogsHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(username) && pwd.equals(password);
                    }
                });
        // Set up world handlers
        server.createContext("/world/difficulty", new WorldDifficultyHandler()); // Handles world difficulty

        // Set up app handlers
        server.createContext("/app/auth", new AppAuthHandler())
                .setAuthenticator(new BasicAuthenticator("AppAuthHandler") {
                    @Override
                    public boolean checkCredentials(String user, String pwd) {
                        return user.equals(username) && pwd.equals(password);
                    }
                });

        // Start server
        server.setExecutor(newFixedThreadPool(LiquidRESTServerConfigs.WEBSERVER_FIXED_THREADS_COUNT.get()));
        server.bind(new InetSocketAddress(LiquidRESTServerConfigs.WEBSERVER_PORT.get()), -1);
        server.start();

        LOGGER.info("LiquidREST is running on  " + server.getAddress());

    }

    public static void stop() {
        server.stop(1);
        LOGGER.info("Stopped LiquidREST threads");
    }

    /**
     * This method takes a query string and converts it into a map of key-value pairs.
     *
     * @param query The query string to convert.
     * @return A map of key-value pairs, or null if the input is null.
     */
    public static Map<String, String> queryToMap(String query) {
        // If the input is null, return null
        if (query == null) {
            return null;
        }

        // Create a new HashMap to store the key-value pairs
        Map<String, String> result = new HashMap<>();

        // Split the query string into individual parameters
        for (String param : query.split("&")) {
            // Split each parameter into a key-value pair
            String[] entry = param.split("=");

            // If the entry has more than one element, add it to the map
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                // If the entry only has one element, add it to the map with an empty string as the value
                result.put(entry[0], "");
            }
        }

        // Return the resulting map
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


    // Start of reusable functions

    /**
     * This method retrieves a ServerPlayer by name from the current server.
     *
     * @param player The name of the player to retrieve.
     * @return The ServerPlayer object if found, or null if not found.
     */
    @Nullable
    public static ServerPlayer getPlayerByName(String player) {
        // Get the list of players from the current server
        for (ServerPlayer serverplayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            // Check if the player's name matches the requested name
            if (serverplayer.getGameProfile().getName().equalsIgnoreCase(player)) {
                // Return the player if found
                return serverplayer;
            }
        }
        // Return null if the player was not found
        return null;
    }

    /**
     * This method converts an InputStream to a String.
     *
     * @param istr The InputStream to convert.
     * @return The converted String.
     * @throws IOException If an I/O error occurs.
     */
    public static String InputStreamToString(InputStream istr) throws IOException {
        // Create a buffer to hold the bytes read from the stream
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        // Create a buffer to read the stream in chunks
        byte[] buffer = new byte[1024];

        // Read the stream in chunks until the end of the stream is reached
        for (int length; (length = istr.read(buffer)) != -1; ) {
            // Write the chunk of bytes to the buffer
            result.write(buffer, 0, length);
        }

        // Convert the buffer to a String using the UTF-8 encoding
        return result.toString(StandardCharsets.UTF_8);
    }
}

