package net.ivango.chat.server.handlers;

public interface DirectMessageHandler {
    public void sendMessage(String sender, String receiver, String message);
}
