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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

public class ChatServer {

    private Thread serverThread;

    private final ServerSocketChannel ssc;

    private final Selector selector;

    private final Map<SocketChannel, String> clientIds;

    private final Map<SocketChannel, String> clients;


    private String serverLog;


    public ChatServer(String host, int port) {
        this.serverLog = "";
        this.clientIds = new HashMap<>();
        this.clients = new HashMap<>();
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(host,port));
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void startServer(){
        serverThread = new Thread(() -> {
            while (!serverThread.isInterrupted()) {
                try {
                    selector.select();
                    if (serverThread.isInterrupted()) break;
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {
                            SocketChannel socketChannel = ssc.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            continue;
                        }
                        if (key.isReadable()){
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            serviceRequest(socketChannel);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        System.out.println("Server started \n");

    }

    private void serviceRequest(SocketChannel socketChannel) {
        if (!socketChannel.isOpen()){
            return;
        }
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            socketChannel.read(buffer);
            String request = new String(buffer.array()).trim();
            if (request.contains("logged in")){
                String id = request.split("\\s")[0];
                String resp = id + " logged in"+"\n";
                clientIds.put(socketChannel, id);
                clients.put(socketChannel, resp);
                serverLog+= LocalTime.now() + " "+ resp;
            }
            else if (request.contains("logged out")){
                String id = request.split("\\s")[0];
                String resp = id + " logged out"+"\n";
                clients.put(socketChannel, resp);
                serverLog+= LocalTime.now() + " "+ resp;
                ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(request.toString());
                socketChannel.write(byteBuffer);
                clients.remove(socketChannel);
            }else {
                String id = clientIds.get(socketChannel);
                String resp = id +": "+ request+ "\n";
                serverLog+= LocalTime.now() + " "+ resp;
                clients.put(socketChannel, request);
            }
            if (!request.isEmpty()){
                writeMessage(request);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void stopServer() {
        try {
            serverThread.interrupt();
            selector.close();
            ssc.close();
            System.out.println("Server stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getServerLog() {
        return serverLog;
    }

    public void writeMessage(String request){
        clients.forEach((channel, req) -> {
            try {
                ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(request));
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
