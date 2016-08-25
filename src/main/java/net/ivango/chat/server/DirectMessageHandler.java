package net.ivango.chat.server;

public interface DirectMessageHandler {
    public void sendMessage(String sender, String receiver, String message);
}
