package net.ivango.chat.server;

import net.ivango.chat.server.handlers.ReadCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

/**
 * Main server class.
 * Starts listening for the incoming client connections on the specified port.
 * Notifies the event listener if anything interesting happens.
 * */
public class Server {

    private String address;
    private int port;
    /** server socket channel used for the client-server communication */
    private AsynchronousServerSocketChannel server;
    /** Processes all the incoming/ outgoing messages and other events */
    private EventProcessor eventProcessor = new EventProcessor();

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * Initializes the server to listen for connections
     * on the specified port.
     * */
    public void init() throws IOException, InterruptedException {
        /* A thread pool to which tasks are submitted to handle I/O events and dispatch to
         * completion-handlers. */
        AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(4, Executors.defaultThreadFactory());
        server = AsynchronousServerSocketChannel.open( channelGroup );
        server.bind(new InetSocketAddress(address, port));
        logger.info("Server started listening at: " + address + ":" + port);
        /* proactive initiation */
        server.accept(null, connectionAcceptHandler);
    }

    /**
     * Completion handler, which is notified upon a new established connection.
     * */
    private CompletionHandler<AsynchronousSocketChannel, Void> connectionAcceptHandler =
                                                            new CompletionHandler<AsynchronousSocketChannel, Void>() {
        public void completed(AsynchronousSocketChannel channel, Void attachment) {
            /* listen for the next connection */
            server.accept(null, this);
            handle(channel);
        }

        public void failed(Throwable exc, Void attachment) { logger.error("Failed to accept a connection", exc); }
    };

    /**
     * Handle the newly-established connection from the client.
     * Notify the event processor.
     * */
    private void handle(AsynchronousSocketChannel channel) {
        try {
            String address = channel.getRemoteAddress().toString();
            logger.debug("Connection established from: " + address);
            /* save the connection */
            eventProcessor.onConnected(address, channel);
            /* attach read listener to this connection */
            ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
            channel.read(inputBuffer, null, new ReadCompletionHandler(channel, inputBuffer, eventProcessor));
        } catch (IOException ie) {
            logger.error("Error occurred upon accepting a new connection:", ie);
        }
    }
}
