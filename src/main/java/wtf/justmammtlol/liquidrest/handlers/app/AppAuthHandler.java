package wtf.justmammtlol.liquidrest.handlers.app;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AppAuthHandler implements HttpHandler {

    /**
     * Handles the HTTP GET request to authenticate in the app
     *
     * @param exchange The HTTP exchange object
     * @throws IOException if an I/O error occurs
     * @since 0.4.1b-1.18.2
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String response = "Authenticated";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.flush();
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