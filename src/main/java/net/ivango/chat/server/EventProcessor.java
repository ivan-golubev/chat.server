package net.ivango.chat.server;

import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.misc.HandlerMap;
import net.ivango.chat.common.misc.MessageHandler;
import net.ivango.chat.common.requests.*;
import net.ivango.chat.common.responses.GetTimeResponse;
import net.ivango.chat.common.responses.GetUsersResponse;
import net.ivango.chat.common.responses.IncomingMessage;
import net.ivango.chat.common.responses.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to process all the incoming/ outgoing messages and other events.
 * */
public class EventProcessor {

    /** holds the address->Session mapping of the active users */
    private Map<String, ClientSession> addressToSessionMap = new ConcurrentHashMap<>();
    /** holds the message type -> handler mapping */
    private HandlerMap handlerMap = new HandlerMap();
    /** maps messages to json and back */
    private JSONMapper jsonMapper = new JSONMapper();

    private static Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    public EventProcessor() {
        registerHandlers();
    }

    /**
     * Registers the message handlers.
     * */
    private void registerHandlers(){
        handlerMap.put(GetTimeRequest.class, (getTimeResponse, address) -> {
            AsynchronousSocketChannel clientChannel = addressToSessionMap.get(address).getChannel();
            sendJson(clientChannel, new GetTimeResponse(new Date().getTime()));
        });

        handlerMap.put(GetUsersRequest.class, (getUsersRequest, senderAddress) -> {
            GetUsersResponse getUsersResponse = getUsers(senderAddress);

            try {
                ClientSession session = addressToSessionMap.get(senderAddress);
                if (session != null) {
                    AsynchronousSocketChannel clientChannel = session.getChannel();
                    sendJson(clientChannel, getUsersResponse);
                }
            } catch (Exception e) {
                logger.error("An error occured during the GetUserRequest processing", e);
            }
        });

        handlerMap.put(LoginRequest.class, (loginRequest, address) -> {
            logger.debug("login request from: " + loginRequest.getUserName());
            ClientSession session = addressToSessionMap.get(address);
            if (session != null) {
                session.setUserName(loginRequest.getUserName());
            }
        });

        handlerMap.put(SendMessageRequest.class, (sendMessageRequest, senderAddress) -> {
            String senderName = addressToSessionMap.get(senderAddress).getUserName();
            if ( sendMessageRequest.isBroadcast() ) {
                addressToSessionMap.keySet().stream().filter(receiver -> !receiver.equals(senderAddress)).forEach(
                        receiver -> sendMessage(senderAddress, senderName, receiver, sendMessageRequest.getMessage(), true)
                );
            } else { /* direct message */
                sendMessage(senderAddress, senderName, sendMessageRequest.getReceiver(), sendMessageRequest.getMessage(), false);
            }
        });
    }

    /**
     * @return a list of currently active users, except for the @param senderAddress
     * */
    private GetUsersResponse getUsers(String senderAddress){
        List<User> users = new ArrayList<>();

        addressToSessionMap.keySet().stream().filter(k -> !k.equals(senderAddress)).forEach(address -> {
            ClientSession session = addressToSessionMap.get(address);
            if (session != null && session.getUserName() != null) {
                users.add(
                        new User(session.getUserName(), address)
                );
            }
        });
        return new GetUsersResponse(users);
    }

    /**
     * Called when a new user established a connection to the server.
     * Saves the address-to-channel mapping.
     * */
    public void onConnected(String address, AsynchronousSocketChannel channel) {
        addressToSessionMap.put(address, new ClientSession(channel));
    }

    /**
     * Called when a user disconnects from the server.
     * Updates the address-to-channel mapping.
     * */
    public void onDisconnected(String id) {
        addressToSessionMap.remove(id);
    }

    /**
     * Called when a new message arrives from the @param senderAddress.
     * */
    public void onMessageReceived(Message message, String senderAddress) {
        /* route the message to a corresponding handler */
        MessageHandler handler = handlerMap.get(message.getClass());
        handler.onMessageReceived(message, senderAddress);
    }

    /**
     * Maps the message to JSON and sends it over the socket channel to the client.
     * */
    private void sendJson(AsynchronousSocketChannel channel, Message message) {
        if (channel != null && channel.isOpen()) {
            ByteBuffer outputBuffer = ByteBuffer.wrap(jsonMapper.toJSON(message).getBytes());
            channel.write(outputBuffer);
        }
    }

    /**
     * Sends the text message to a specific user or to everyone.
     * @param broadcast - send to everyone if true
     * */
    private void sendMessage(String senderAddress, String senderName, String receiver, String message, boolean broadcast) {
        ClientSession session = addressToSessionMap.get(receiver);
        if (session != null) {
            AsynchronousSocketChannel channel = session.getChannel();
            if (channel != null && channel.isOpen()) {
                // Sending message to the client
                sendJson(channel, new IncomingMessage(senderAddress, senderName, message, broadcast));
            }
        }
    }
}
