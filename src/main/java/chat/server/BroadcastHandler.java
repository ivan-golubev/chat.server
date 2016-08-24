package chat.server;

public interface BroadcastHandler {
    public void broadcastMessage(String sender, String message);
}
