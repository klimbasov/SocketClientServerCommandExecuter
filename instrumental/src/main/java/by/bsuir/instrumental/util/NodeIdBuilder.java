package by.bsuir.instrumental.util;

import java.net.Socket;

public class NodeIdBuilder {
    private static final String SERVER_ID = "0.0.0.0";

    public static String buildSocketIdClient(Socket socket) {
        return socket.getLocalSocketAddress().toString().substring(1).replace(':', '.');
    }

    public static String buildSocketIdServer(Socket socket) {
        return socket.getRemoteSocketAddress().toString().substring(1).replace(':', '.');
    }

    public static String getServerId() {
        return SERVER_ID;
    }
}
