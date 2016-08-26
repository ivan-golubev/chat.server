package net.ivango.chat.server;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;

public class StartServer {

    public StartServer(String host, int port) throws ParseException {
        try {
            Server server = new Server(host, port);

            server.init();

            while ( !Thread.interrupted() ) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (UnresolvedAddressException ua) {
            System.err.println("Cannot resolve the specified address: " + host + ":" + port);
            System.exit(1);
        } catch (SocketException se) {
            System.err.println("Specified port is in use already: " + host + ":" + port);
            System.exit(1);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Terminating the server...");
        }
    }

    public static void main(String[] args) throws ParseException {
        if (args.length != 2) {
            System.out.println("Usage: java -jar ChatServer.jar <host> <port>");
        } else {
            try {
                String host = args[0];
                int port = Integer.valueOf(args[1]);
                new StartServer(host, port);

            } catch(NumberFormatException ne) {
                    System.err.println("Failed to parse the port...");
                }
            }
    }
}
