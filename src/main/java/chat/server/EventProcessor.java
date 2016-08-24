package chat.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventProcessor implements BroadcastHandler, DirectMessageHandler, DisconnectHandler {

    private Map<String, AsynchronousSocketChannel> connections = new ConcurrentHashMap<>();

    public void onConnected(String address, AsynchronousSocketChannel channel) {
        connections.put(address, channel);
    }

    public void onDisconnected(String id) {
        connections.remove(id);
    }

    public void broadcastMessage(String sender, String message) {
        connections.keySet().stream().filter(receiver -> !receiver.equals(sender)).forEach(receiver -> {

            AsynchronousSocketChannel channel = connections.get(receiver);

            if (channel != null && channel.isOpen()) {
                System.out.format("Sending message from %s to %s...\n", sender, receiver);
                ByteBuffer outputBuffer = ByteBuffer.wrap(message.getBytes());
                channel.write(outputBuffer);
            }
        });
    }

    public void sendMessage(String sender, String receiver, String message) {
        AsynchronousSocketChannel channel = connections.get(receiver);
        if (channel != null && channel.isOpen()) {
            // Sending message to the client
        }
    }
}
