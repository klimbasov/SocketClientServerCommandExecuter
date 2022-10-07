package by.bsuir.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

@SpringBootApplication
@Slf4j
public class Main {
//    public static final int PORT = 8082;
//
//    public static void main(String[] args) throws IOException {
//        try (ServerSocket socket = new ServerSocket(PORT)) {
//
//            log.info("connection  established.");
//
//            showSocketState(client);
//            while (true) {
//                String line = in.readLine();
//                if (line == null) {
//                    System.out.println("after null msg received");
//                    showSocketState(client);
//                    break;
//                }
//                if (line.equals("stop")) {
//                    break;
//                }
//                System.out.println(line);
//                out.println("received: " + line);
//            }
//            client.close();
//            System.out.println("after socket is closed programmatically");
//            showSocketState(client);
//            System.out.println("connection terminated");
//        } catch (Exception e) {
//            System.out.println(e.getClass().getName() + " : " + e.getMessage());
//        }
//    }
//
//    private static void showSocketState(Socket client) throws SocketException {
//        String msg =
//                "KeepAlive: " + client.getKeepAlive() + "\n" +
//                        "is closed: " + client.isClosed() + "\n" +
//                        "is connected: " + client.isConnected() + "\n" +
//                        "is input shutdown: " + client.isInputShutdown() + "\n" +
//                        "is output shutdown: " + client.isOutputShutdown();
//        System.out.println(msg);
//    }
}
