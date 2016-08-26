package net.ivango.chat.server;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class used to:
 * 1. validate the input console arguments
 * 2. start the server
 * */
public class StartServer {

    private static Logger logger = LoggerFactory.getLogger(StartServer.class);

    public StartServer(String host, int port) {
        try {
            Server server = new Server(host, port);

            server.init();

            while ( !Thread.interrupted() ) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (UnresolvedAddressException ua) {
            logger.error("Cannot resolve the specified address: " + host + ":" + port);
            System.exit(1);
        } catch (SocketException se) {
            logger.error("Specified port is in use already: " + host + ":" + port);
            System.exit(1);
        } catch (InterruptedException | IOException e) {
            logger.error("Other server error", e);
        } finally {
            logger.info("Terminating the server...");
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.info("Usage: java -jar chat.server-*.jar <host> <port>");
        } else {
            try {
                String host = args[0];
                int port = Integer.valueOf(args[1]);
                new StartServer(host, port);

            } catch(NumberFormatException ne) {
                    logger.error("Failed to parse the port...");
                }
            }
    }
}
