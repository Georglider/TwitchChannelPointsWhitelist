package georglider.twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * @author Georglider (Georglider#3660)
 * <p> Main creation on 10.01.2023 at 20:46
 **/

public class Application {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        boolean cli = args.length > 0;

        int port = Integer.parseInt(dialog("Please provide port which would be used for current session", cli));
        String clientId = dialog("Please paste your clientId from Twitch here!", cli);

        String token = startServer(port, clientId, cli);
        System.out.println("\n" + token);
        if (!cli) {
            int a = JOptionPane.showConfirmDialog(null,
                    String.format("Here's your token %s, would you like to copy it?", token),
                    "Would you like to copy your token?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null);
            if (a == 0) {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(token), null);
            }
        }
    }

    public static String startServer(int port, String clientId, Boolean cli) throws IOException, InterruptedException, URISyntaxException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        String s = new InetSocketAddress(port).toString();
        LoggedInHandler loggedInHandler = new LoggedInHandler();
        StartHandler startHandler = new StartHandler(s, clientId);
        server.createContext("/", startHandler);
        server.createContext("/token", loggedInHandler);
        server.setExecutor(null); // creates a default executor
        server.start();

        if (!cli && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI("http://localhost:" + port));
        } else {
            System.out.printf("Go to the http://localhost:%d", port);
        }

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
                String response = """
                        <html>

                        <body>
                            <h1>Something went wrong, please try again!</h1>
                        </body>

                        </html>""";
                t.sendResponseHeaders(401, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                this.token = t.getRequestURI().toString().substring(20, 50);

                String response = String.format("""
                        <html>

                        <body>
                            <h1>Authenticated!</h1>
                            <h2>This is your token -> %s</h2>
                        </body>

                        </html>""", this.token);
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
            String response = String.format("""
                    <html>

                    <body>
                        <a href=%s>Login with Twitch</a>
                    </body>

                    <script>
                    if (location.href.indexOf('#') != -1) {location.replace(location.href.replace('#', "token/"))}</script>

                    </html>""", ("https://id.twitch.tv/oauth2/authorize?client_id=" + clientId +
                    "&redirect_uri=" + server + "&response_type=token" +
                    "&scope=channel:manage:redemptions channel:read:redemptions"));
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static String dialog(String message, Boolean cliMode) {
        if (cliMode) {
            System.out.println(message);

            return scanner.nextLine();
        }
        return JOptionPane.showInputDialog(message);
    }

}
