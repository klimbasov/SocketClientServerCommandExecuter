package by.bsuir.instrumental.util;

import java.net.Socket;

public class NodeIdBuilder {
    private static final String SERVER_ID = "0.0.0.0";

    public static String buildSocketIdClient(Socket socket) {
        return socket.getLocalAddress().toString().substring(1);
    }

    public static String buildSocketIdServer(Socket socket) {
        return socket.getInetAddress().toString().substring(1);
    }

    public static String getServerId() {
        return SERVER_ID;
    }
}
