import http.HttpTaskServer;
import http.KVServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new KVServer().start();
        HttpTaskServer httpTaskServer = new HttpTaskServer("http://localhost:8078");
        httpTaskServer.start();
    }
}
