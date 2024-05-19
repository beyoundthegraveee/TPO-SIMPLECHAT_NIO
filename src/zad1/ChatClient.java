/**
 *
 *  @author Kurzau Kiryl S24911
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class ChatClient{

    private final SocketChannel clientChannel;

    private final StringBuilder chatView;

    private final List<String> list = new ArrayList<>();

    private final Thread clientThread;


    private boolean isRunning = true;

    private final String id;
    public ChatClient(String host, int port, String id) {
        this.id = id;
        chatView = new StringBuilder("=== " + id + " chat view\n");
        clientThread = new Thread(this::getMessage);
        try {
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(host,port));
            while (true){
                if(clientChannel.finishConnect()){
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void login(){
        send(id + " logged in");
        clientThread.start();

    }

    public void logout(){
        send(id + " logged out");
        try {
            Thread.sleep(100);
            isRunning = false;
            clientThread.interrupt();
        } catch (InterruptedException e) {
            chatView.append("*** ").append(e).append("\n");
        }
    }

    public void send(String req){
        ByteBuffer sendBuffer = ByteBuffer.allocateDirect(req.getBytes().length);
        try {
            sendBuffer.clear();
            sendBuffer.put(req.getBytes());
            sendBuffer.flip();
            Thread.sleep(30);
            while (sendBuffer.hasRemaining()){
                clientChannel.write(sendBuffer);
            }
        } catch (IOException | InterruptedException e) {
            chatView.append("*** ").append(e).append("\n");
        }
    }

    public String getChatView(){
        for (String message : list) {
            chatView.append(message).append("\n");
        }
        return chatView.toString();
    }

    public void getMessage() {
        while (isRunning) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.setLength(0);
            try {
                while (clientChannel.read(buffer) > 0) {
                    buffer.flip();
                    CharBuffer decode = StandardCharsets.UTF_8.decode(buffer);
                    stringBuilder.append(decode);
                    buffer.clear();
                }
            } catch (IOException e) {
                chatView.append("*** ").append(e).append("\n");
            }

            if (!stringBuilder.toString().isEmpty()) {
                list.add(stringBuilder.toString());
            }
        }
    }




}
