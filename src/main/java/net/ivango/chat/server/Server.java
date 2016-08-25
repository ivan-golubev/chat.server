package net.ivango.chat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

public class Server {

    private int port;
    private AsynchronousServerSocketChannel server;
    private EventProcessor eventProcessor = new EventProcessor();

    public Server(int port) {
        this.port = port;
    }

    public void init() throws IOException, InterruptedException {
        /* A thread pool to which tasks are submitted to handle I/O events and dispatch to
         * completion-handlers. */
        AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(4, Executors.defaultThreadFactory());
        server = AsynchronousServerSocketChannel.open( channelGroup );
        server.bind(new InetSocketAddress("localhost", port));
        /* proactive initiation */
        server.accept(null, connectionAcceptHandler);
    }

    private CompletionHandler<AsynchronousSocketChannel, Void> connectionAcceptHandler =
                                                            new CompletionHandler<AsynchronousSocketChannel, Void>() {
        public void completed(AsynchronousSocketChannel channel, Void attachment) {
            /* listen for the next connection */
            server.accept(null, this);
            handle(channel);
        }

        public void failed(Throwable exc, Void attachment) { System.out.println("Connection failed"); }
    };

    private void handle(AsynchronousSocketChannel channel) {
        try {
            String address = channel.getRemoteAddress().toString();
            System.out.format("Connection from %s established.\n", address);
            /* save the connection */
            eventProcessor.onConnected(address, channel);
            /* attach listener to this connection */
            ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
            channel.read(inputBuffer, null, new ReadCompletionHandler(channel, inputBuffer, eventProcessor));
        } catch (IOException ie) {

        }
    }

}
