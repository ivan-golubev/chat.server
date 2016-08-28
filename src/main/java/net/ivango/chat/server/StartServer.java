package net.ivango.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;

import java.net.SocketException;
import java.nio.channels.UnresolvedAddressException;

/**
 * Main class used to:
 * 1. validate the input console arguments
 * 2. start the server
 * */
public class StartServer {

    private static Logger logger = LoggerFactory.getLogger(StartServer.class);

    public StartServer(String host, int port) {

        try ( Server server = new Server(host, port) ){

            /* trap the INT (CTRL+C) signal */
            Signal.handle(
                    new Signal("INT"),
                    signal -> {
                        try {
                            server.close();
                        } catch (Exception e) {
                            logger.warn("Error in signal handler", e);
                        } finally {
                            logger.info("Server stopped.");
                            System.exit(0);
                        }
                    }
            );

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
        } catch (Exception e) {
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
