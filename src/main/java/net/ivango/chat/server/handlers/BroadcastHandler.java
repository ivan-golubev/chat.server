package net.ivango.chat.server.handlers;

public interface BroadcastHandler {
    public void broadcastMessage(String sender, String message);
}
