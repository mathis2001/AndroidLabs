package org.northstar.banklab;

import android.content.res.AssetManager;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class LabHttpServer {
    static final int PORT = 39991;
    static final String ORIGIN = "http://127.0.0.1:" + PORT;
    static final String STATEMENT_URL = ORIGIN + "/secure/statement";

    private final ServerSocket server;
    private final ExecutorService workers = Executors.newCachedThreadPool();
    private final AssetManager assets;

    LabHttpServer(AssetManager assets) throws IOException {
        this.assets = assets;
        server = new ServerSocket(PORT, 16, InetAddress.getByName("127.0.0.1"));
        Thread acceptor = new Thread(this::accept, "bank-lab-http");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void accept() {
        while (!server.isClosed()) {
            try {
                Socket client = server.accept();
                workers.execute(() -> serve(client));
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private void serve(Socket client) {
        try (Socket socket = client) {
            socket.setSoTimeout(4000);
            InputStream input = new BufferedInputStream(socket.getInputStream());
            String request = readLine(input);
            if (request == null) return;
            String[] parts = request.split(" ");
            if (parts.length < 2 || !"GET".equals(parts[0])) {
                respond(socket, "405 Method Not Allowed", "text/plain", "GET only".getBytes(StandardCharsets.UTF_8), null);
                return;
            }
            String header;
            while ((header = readLine(input)) != null && !header.isEmpty()) { /* consume headers */ }
            Uri url = Uri.parse(parts[1]);
            String path;
            try {
                path = java.net.URI.create(url.getPath()).normalize().getPath();
            } catch (IllegalArgumentException e) {
                respond(socket, "400 Bad Request", "text/plain", "Invalid path".getBytes(StandardCharsets.UTF_8), null);
                return;
            }
            if ("/partners/offer".equals(path)) {
                String destination = url.getQueryParameter("link");
                if (destination == null || !(destination.startsWith("http://") || destination.startsWith("https://"))) {
                    respond(socket, "400 Bad Request", "text/plain", "Missing destination".getBytes(StandardCharsets.UTF_8), null);
                } else {
                    respond(socket, "302 Found", "text/plain", new byte[0], destination.replace("\r", "").replace("\n", ""));
                }
            } else if ("/rewards/overview".equals(path) || "/".equals(path)) {
                asset(socket, "promo.html");
            } else if ("/secure/statement".equals(path)) {
                asset(socket, "statement.html");
            } else {
                respond(socket, "404 Not Found", "text/plain", "Not found".getBytes(StandardCharsets.UTF_8), null);
            }
        } catch (IOException ignored) {
        }
    }

    private void asset(Socket socket, String name) throws IOException {
        try (InputStream input = assets.open(name); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
            respond(socket, "200 OK", "text/html; charset=utf-8", bytes.toByteArray(), null);
        }
    }

    private static String readLine(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int c;
        while ((c = input.read()) != -1 && c != '\n') {
            if (c != '\r') bytes.write(c);
            if (bytes.size() > 8192) throw new IOException("Request line too long");
        }
        if (c == -1 && bytes.size() == 0) return null;
        return bytes.toString("ISO-8859-1");
    }

    private static void respond(Socket socket, String status, String type, byte[] body, String location) throws IOException {
        OutputStream output = socket.getOutputStream();
        String headers = "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + type + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                (location == null ? "" : "Location: " + location + "\r\n") +
                "Cache-Control: no-store\r\nConnection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.ISO_8859_1));
        output.write(body);
        output.flush();
    }
}
