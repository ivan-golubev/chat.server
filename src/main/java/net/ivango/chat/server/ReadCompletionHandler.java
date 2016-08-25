package net.ivango.chat.server;

import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.requests.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadCompletionHandler implements CompletionHandler<Integer, Void> {
    private AsynchronousSocketChannel socketChannel;
    private ByteBuffer inputBuffer;
    private EventProcessor eventProcessor;
    private JSONMapper jsonMapper = new JSONMapper();

    public ReadCompletionHandler(AsynchronousSocketChannel socketChannel,
                                 ByteBuffer inputBuffer,
                                 EventProcessor eventProcessor) {
        this.socketChannel = socketChannel;
        this.inputBuffer = inputBuffer;
        this.eventProcessor = eventProcessor;
    }

    public void completed(Integer bytesRead, Void sessionState) {
        try {
            String senderAddress = socketChannel.getRemoteAddress().toString();
                    if (bytesRead == -1) {
                        System.out.format("EOS received. client %s disconnected.\n", senderAddress);
                        eventProcessor.onDisconnected(senderAddress);
                        return;
                    }

            byte[] buffer = new byte[bytesRead];
            inputBuffer.rewind();
            // Rewind the input buffer to read from the beginning

            inputBuffer.get(buffer);
            String json = new String(buffer);
            System.out.println("Parsing the message: " + json);
            try {
                Message message = (Message) jsonMapper.fromJson(json);
                eventProcessor.onMessageReceived(message, senderAddress);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//
//        try {
////            System.out.format("Received message from client %s : %s.\n", socketChannel.getRemoteAddress().toString(),
////                    message);
//
////            eventProcessor.broadcastMessage(socketChannel.getRemoteAddress().toString(), "message");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        /* attach read listener to this connection to listen for the upcoming messages */
        inputBuffer.clear();
        socketChannel.read(inputBuffer, null, this);

//            // Echo the message back to client
//            WriteCompletionHandler writeCompletionHandler =
//                    new WriteCompletionHandler(socketChannel);
//
//            ByteBuffer outputBuffer = ByteBuffer.wrap(buffer);
//
//            socketChannel.write(
//                    outputBuffer, sessionState, writeCompletionHandler);
    }

    public void failed(Throwable exc, Void attachment) {
        System.out.println("Failed to receive the input message");
    }
}
