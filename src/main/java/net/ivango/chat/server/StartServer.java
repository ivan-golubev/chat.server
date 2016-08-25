package net.ivango.chat.server;

import java.io.IOException;

public class StartServer {

    public static final int PORT = 8989;

    public static void main(String[] args)  {
        Server server = new Server(PORT);

        try {
            server.init();

            while ( !Thread.interrupted() ) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Terminating the server");
        }
    }
}
