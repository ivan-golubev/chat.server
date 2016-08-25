package net.ivango.chat.server;


import java.nio.channels.AsynchronousSocketChannel;

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
