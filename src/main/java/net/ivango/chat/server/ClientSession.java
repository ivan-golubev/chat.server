package net.ivango.chat.server;


import java.nio.channels.AsynchronousSocketChannel;

/**
 * Holds the client session info:
 * user name and the socket channel.
 * */
public class ClientSession {

    private AsynchronousSocketChannel channel;
    private String userName;

    public ClientSession(AsynchronousSocketChannel channel) {
        this.channel = channel;
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
}
