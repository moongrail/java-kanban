import http.HttpTaskServer;
import http.KVServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new KVServer().start();
        HttpTaskServer.run();
    }
}
