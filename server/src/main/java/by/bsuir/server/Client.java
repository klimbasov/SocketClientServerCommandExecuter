package by.bsuir.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static final String STOP_WORD = "stop";

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 8082);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("connection  established.");
            String input = null;
            Scanner scanner = new Scanner(System.in);
            while (!STOP_WORD.equals(input)) {
                input = scanner.nextLine();
                out.println(input);
                System.out.println(in.readLine());
            }
            System.out.println("connection closed");
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}