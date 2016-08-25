package net.ivango.chat.server;

import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.*;
import net.ivango.chat.common.responses.IncomingMessage;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventProcessor {

    private Map<String, AsynchronousSocketChannel> connections = new ConcurrentHashMap<>();

    private HandlerMap handlerMap = new HandlerMap();
    private JSONMapper jsonMapper = new JSONMapper();

    public EventProcessor() {
        registerHandlers();
    }

    private void registerHandlers(){
        handlerMap.put(GetTimeRequest.class, new MessageHandler<GetTimeRequest>() {
            @Override
            public void onMessageReceived(GetTimeRequest getTimeResponse) {
                System.out.println("GetTimeRequest");
            }
        });

        handlerMap.put(GetUsersRequest.class, new MessageHandler<GetUsersRequest>() {
            @Override
            public void onMessageReceived(GetUsersRequest getUsersRequest) {
                System.out.println("GetUsersRequest");
            }
        });

        handlerMap.put(LoginRequest.class, new MessageHandler<LoginRequest>() {
            @Override
            public void onMessageReceived(LoginRequest loginRequest) {
                System.out.println("LoginRequest");
            }
        });

        handlerMap.put(SendMessageRequest.class, new MessageHandler<SendMessageRequest>() {
            @Override
            public void onMessageReceived(SendMessageRequest sendMessageRequest) {
                System.out.format("Received message: %s.\n", sendMessageRequest.getMessage());
                broadcastMessage("", sendMessageRequest.getMessage());
            }
        });
    }

    public void onConnected(String address, AsynchronousSocketChannel channel) {
        connections.put(address, channel);
    }

    public void onDisconnected(String id) {
        connections.remove(id);
    }

    public void onMessageReceived(Message message) {
        MessageHandler handler = handlerMap.get(message.getClass());
        handler.onMessageReceived(message);
    }

    private void broadcastMessage(String sender, String messageText) {
        connections.keySet().stream().filter(receiver -> !receiver.equals(sender)).forEach(receiver -> {

            AsynchronousSocketChannel channel = connections.get(receiver);

            if (channel != null && channel.isOpen()) {
                System.out.format("Sending message from %s to %s...\n", sender, receiver);

                IncomingMessage message = new IncomingMessage("", messageText, true);
                ByteBuffer outputBuffer = ByteBuffer.wrap(jsonMapper.toJSON(message).getBytes());
                channel.write(outputBuffer);
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        AsynchronousSocketChannel channel = connections.get(receiver);
        if (channel != null && channel.isOpen()) {
            // Sending message to the client
        }
    }
}
