package georglider.twitch.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TwitchAPIServer {

    public static String startServer(int port, String clientId) throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        String s = new InetSocketAddress(port).toString();
        LoggedInHandler loggedInHandler = new LoggedInHandler();
        StartHandler startHandler = new StartHandler(s, clientId);
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Please setup your user_access_token via " + startHandler.server);
        server.createContext("/", startHandler);
        server.createContext("/token", loggedInHandler);
        server.setExecutor(null); // creates a default executor
        server.start();

        while (loggedInHandler.token.equals("")) {
            Thread.sleep(1000);
        }
        server.stop(10);
        return loggedInHandler.token;
    }

    static class LoggedInHandler implements HttpHandler {
        String token = "";

        public LoggedInHandler() {
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            if (t.getRequestURI().toString().length() < 50) {
                String response = "<html>\n" +
                        "\n" +
                        "<body>\n" +
                        "    <h1>Something went wrong, please try again!</h1>\n" +
                        "</body>\n" +
                        "\n" +
                        "</html>";
                t.sendResponseHeaders(401, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                this.token = t.getRequestURI().toString().substring(20, 50);

                String response = "<html>\n" +
                        "\n" +
                        "<body>\n" +
                        "    <h1>Authenticated!</h1>\n" +
                        "    <h2>Please return back to your server</h2>\n" +
                        "</body>\n" +
                        "\n" +
                        "</html>";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class StartHandler implements HttpHandler {
        private final String clientId;
        private final String server;

        public StartHandler(String server, String clientId) {
            this.server = new StringBuilder(server.replace("0.0.0.0/0.0.0.0", "localhost")).insert(0, "http://").toString();
            this.clientId = clientId;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = String.format("<html>\n" +
                    "\n" +
                    "<body>\n" +
                    "    <a href=%s>Login with Twitch</a>\n" +
                    "</body>\n" +
                    "\n" +
                    "<script>\n" +
                    "if (location.href.indexOf('#') != -1) {location.replace(location.href.replace('#', \"token/\"))}" +
                    "</script>\n" +
                    "\n" +
                    "</html>", ("https://id.twitch.tv/oauth2/authorize?client_id=" + clientId +
                    "&redirect_uri=" + server + "&response_type=token" +
                    "&scope=channel:manage:redemptions channel:read:redemptions"));
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
