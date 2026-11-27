package io.github.koppor.kodf;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// [impl->dsn~serve~1]
/** Serves the viewer page, d3 and the scan results as JSON. */
final class Serve {

    private Serve() {}

    static HttpServer start(Path db, InetSocketAddress address) throws IOException {
        HttpServer server = HttpServer.create(address, 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                switch (exchange.getRequestURI().getPath()) {
                    case "/" -> send(exchange, "text/html; charset=utf-8", resource("viewer.html"));
                    case "/d3.min.js" -> send(exchange, "text/javascript", d3());
                    case "/data.json" -> send(exchange, "application/json", json(db));
                    default -> exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                System.err.println(exchange.getRequestURI() + ": " + e);
            }
        });
        server.start();
        return server;
    }

    private static void send(HttpExchange exchange, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static byte[] resource(String name) throws IOException {
        try (InputStream in = Serve.class.getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException(name + " is missing");
            }
            return in.readAllBytes();
        }
    }

    /** d3 comes from its WebJar; its version is read from the WebJar, so that it is declared only in //DEPS. */
    private static byte[] d3() throws IOException {
        Properties pom = new Properties();
        pom.load(new ByteArrayInputStream(resource("/META-INF/maven/org.webjars.npm/d3/pom.properties")));
        return resource("/META-INF/resources/webjars/d3/" + pom.getProperty("version") + "/dist/d3.min.js");
    }

    /** {@code {"dirs": [[path, bytes of its own files], ...], "subsets": {path: [containing path, ...]}}} */
    static byte[] json(Path file) throws SQLException {
        StringBuilder json = new StringBuilder("{\"dirs\":[");
        try (Connection db = Db.open(file);
                Statement s = db.createStatement();
                ResultSet rs = s.executeQuery("SELECT dir, sum(max(size, 0)) FROM file GROUP BY dir ORDER BY dir")) {
            String separator = "";
            while (rs.next()) {
                json.append(separator).append('[');
                quote(json, rs.getString(1)).append(',').append(rs.getLong(2)).append(']');
                separator = ",";
            }
            json.append("],\"subsets\":{");
            separator = "";
            for (Map.Entry<Path, List<Path>> subset : Subsets.find(db).entrySet()) {
                quote(json.append(separator), subset.getKey().toString()).append(":[");
                String inner = "";
                for (Path superset : subset.getValue()) {
                    quote(json.append(inner), superset.toString());
                    inner = ",";
                }
                json.append(']');
                separator = ",";
            }
        }
        return json.append("}}").toString().getBytes(StandardCharsets.UTF_8);
    }

    private static StringBuilder quote(StringBuilder json, String s) {
        json.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"', '\\' -> json.append('\\').append(c);
                default -> {
                    if (c < 0x20) {
                        json.append("\\u%04x".formatted((int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        return json.append('"');
    }
}
