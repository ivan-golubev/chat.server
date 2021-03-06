package net.ivango.chat.server.handlers;

import com.google.gson.JsonSyntaxException;
import net.ivango.chat.common.JSONMapper;
import net.ivango.chat.common.requests.Message;
import net.ivango.chat.server.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashSet;
import java.util.Set;

public class ReadCompletionHandler implements CompletionHandler<Integer, Void> {
    private AsynchronousSocketChannel socketChannel;
    private ByteBuffer inputBuffer;
    private EventProcessor eventProcessor;
    private JSONMapper jsonMapper = new JSONMapper();

    private static Logger logger = LoggerFactory.getLogger(ReadCompletionHandler.class);

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
                        logger.debug("EOS received. client disconnected: " + senderAddress);
                        eventProcessor.onDisconnected(senderAddress);
                        return;
                    }

            byte[] buffer = new byte[bytesRead];
            inputBuffer.rewind();
            // Rewind the input buffer to read from the beginning

            inputBuffer.get(buffer);
            String json = new String(buffer);

            try {
                for (String j: preprocessInput(json)) {
                    Message message = (Message) jsonMapper.fromJson(j);
                    eventProcessor.onMessageReceived(message, senderAddress);
                }
            } catch (ClassNotFoundException e) {
                logger.error("Failed to map the json to message. Unknown data type.");
            } catch (JsonSyntaxException je) {
                logger.error("Failed to parse the message: " + json);
            }

        } catch (IOException e) {
            logger.error("Error during the input data parsing", e);
        }

        /* attach read listener to this connection to listen for the upcoming messages */
        inputBuffer.clear();
        socketChannel.read(inputBuffer, null, this);
    }

    /* client might send several successive messages concatenated */
    private Set<String> preprocessInput(String input) {
        /* splitting the concatenated json objects */
        String[] res = input.split("\\}\\{");

        /* ignore duplicated messages (polling requests) */
        Set<String> set = new HashSet<>();

        /* adding brackets back */
        if (res.length > 1) {
            set.add(res[0] + "}");
            set.add("{" + res[res.length-1]);
            if (res.length > 2) {
                for (int i=1; i<res.length-2; i++) {
                    set.add("{" + res[i] + "}");
                }
            }
        } else {
            set.add(input);
        }

        return set;
    }

    public void failed(Throwable exc, Void attachment) {
        logger.info("Failed to receive the input message");
    }
}
