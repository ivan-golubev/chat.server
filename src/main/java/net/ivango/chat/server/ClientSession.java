package net.ivango.chat.server;


import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds the client session info:
 * user name and the socket channel.
 * */
public class ClientSession {

    private AsynchronousSocketChannel channel;
    private final ReentrantLock lock;
    private String userName;

    public ClientSession(AsynchronousSocketChannel channel) {
        this.channel = channel;
        lock = new ReentrantLock();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public AsynchronousSocketChannel getChannel() {
        return channel;
    }

    public String getUserName() {
        return userName;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
