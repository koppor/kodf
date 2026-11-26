package io.github.koppor.kodf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServeTest {

    @TempDir
    Path tmp;

    // [utest->dsn~serve~1]
    @Test
    void servesPageD3AndResults() throws Exception {
        Files.createDirectories(tmp.resolve("data/a"));
        Files.createDirectories(tmp.resolve("data/b\"quoted"));
        Files.writeString(tmp.resolve("data/a/x"), "xx");
        Files.writeString(tmp.resolve("data/b\"quoted/x"), "xx");
        Path db = tmp.resolve("kodf.db");
        try (Connection c = Db.open(db)) {
            new Scanner(c, 1).scan(List.of(tmp.resolve("data")));
        }

        HttpServer server = Serve.start(db, new InetSocketAddress("127.0.0.1", 0));
        try (HttpClient client = HttpClient.newHttpClient()) {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            assertTrue(get(client, base + "/").contains("d3.stratify()"));
            assertTrue(get(client, base + "/d3.min.js").contains("d3js.org"));
            String a = tmp.resolve("data/a").toString();
            String b = tmp.resolve("data/b\\\"quoted").toString();
            assertEquals(
                    "{\"dirs\":[[\"" + a + "\",2],[\"" + b + "\",2]],\"subsets\":{\"" + a + "\":[\"" + b + "\"],\"" + b
                            + "\":[\"" + a + "\"]}}",
                    get(client, base + "/data.json"));
            assertEquals(
                    404,
                    client.send(
                                    HttpRequest.newBuilder(URI.create(base + "/x"))
                                            .build(),
                                    HttpResponse.BodyHandlers.discarding())
                            .statusCode());
        } finally {
            server.stop(0);
        }
    }

    private static String get(HttpClient client, String uri) throws Exception {
        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder(URI.create(uri)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), uri);
        return response.body();
    }
}
