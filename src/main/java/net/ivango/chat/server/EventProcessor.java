package net.ivango.chat.server;

import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.*;
import net.ivango.chat.common.responses.GetUsersResponse;
import net.ivango.chat.common.responses.IncomingMessage;
import net.ivango.chat.common.responses.User;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventProcessor {

    private Map<String, ClientSession> addressToSessionMap = new ConcurrentHashMap<>();

    private HandlerMap handlerMap = new HandlerMap();
    private JSONMapper jsonMapper = new JSONMapper();

    public EventProcessor() {
        registerHandlers();
    }

    private void registerHandlers(){
        handlerMap.put(GetTimeRequest.class, new MessageHandler<GetTimeRequest>() {
            @Override
            public void onMessageReceived(GetTimeRequest getTimeResponse, String address) {
                System.out.println("GetTimeRequest");
            }
        });

        handlerMap.put(GetUsersRequest.class, new MessageHandler<GetUsersRequest>() {
            @Override
            public void onMessageReceived(GetUsersRequest getUsersRequest, String senderAddress) {
                GetUsersResponse getUsersResponse = getUsers(senderAddress);
                if ( !getUsersResponse.getUsers().isEmpty() ) {
                    AsynchronousSocketChannel clientChannel = addressToSessionMap.get(senderAddress).getChannel();
                    sendJson(clientChannel, getUsersResponse);
                }
            }
        });

        handlerMap.put(LoginRequest.class, new MessageHandler<LoginRequest>() {
            @Override
            public void onMessageReceived(LoginRequest loginRequest, String address) {
                System.out.format("login request from: %s.\n", loginRequest.getUserName());
                ClientSession session = addressToSessionMap.get(address);
                session.setUserName(loginRequest.getUserName());
            }
        });

        handlerMap.put(SendMessageRequest.class, new MessageHandler<SendMessageRequest>() {
            @Override
            public void onMessageReceived(SendMessageRequest sendMessageRequest, String address) {
                System.out.format("Received message: %s.\n", sendMessageRequest.getMessage());
//                broadcastMessage("", new IncomingMessage("", sendMessageRequest.getMessage(), true));
                sendMessage(address, sendMessageRequest.getReceiver(), sendMessageRequest.getMessage());
            }
        });
    }

    private GetUsersResponse getUsers(String senderAddress){
        List<User> users = new ArrayList<>();

        addressToSessionMap.keySet().stream().filter(k -> !k.equals(senderAddress)).forEach(address -> {
            ClientSession session = addressToSessionMap.get(address);
            if (session.getUserName() != null) {
                users.add(
                        new User(session.getUserName(), address)
                );
            }
        });
        GetUsersResponse getUsersResponse = new GetUsersResponse(users);
        return getUsersResponse;
    }

    public void onConnected(String address, AsynchronousSocketChannel channel) {
        addressToSessionMap.put(address, new ClientSession(channel));
    }

    public void onDisconnected(String id) {
        addressToSessionMap.remove(id);
    }

    public void onMessageReceived(Message message, String senderAddress) {
        MessageHandler handler = handlerMap.get(message.getClass());
        handler.onMessageReceived(message, senderAddress);
    }

    private void broadcastMessage(String sender, Message message) {
        addressToSessionMap.keySet().stream().filter(receiver -> !receiver.equals(sender)).forEach(receiver -> {

            AsynchronousSocketChannel channel = addressToSessionMap.get(receiver).getChannel();

            if (channel != null && channel.isOpen()) {
                System.out.format("Sending message from %s to %s...\n", sender, receiver);

                sendJson(channel, message);
            }
        });
    }

    private void sendJson(AsynchronousSocketChannel channel, Message message) {
        ByteBuffer outputBuffer = ByteBuffer.wrap(jsonMapper.toJSON(message).getBytes());
        channel.write(outputBuffer);
    }

    private void sendMessage(String senderAddress, String receiver, String message) {
        AsynchronousSocketChannel channel = addressToSessionMap.get(receiver).getChannel();
        if (channel != null && channel.isOpen()) {
            // Sending message to the client
            String senderName = addressToSessionMap.get(senderAddress).getUserName();
            sendJson(channel, new IncomingMessage(senderAddress, senderName, message, false));
        }
    }
}
