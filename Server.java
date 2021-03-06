
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Server {

    // to do create socketserver multithread use try-catch
    static int port = 1234;
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String RED = "\u001B[31m";
    public static SocketChannel socketChannel;

    public static void main(String[] args) throws IOException {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            System.out.println(GREEN + "Server is Running . . .");
            System.out.println(GREEN + "Port : " + BLUE + "" + port);
            while (true) {
                socketChannel = serverSocketChannel.accept();
                System.out.println(GREEN + "connect server : " + BLUE + "" + socketChannel);
                //do something with socketChannel...
                Thread thread = new Thread(new ClientHandler(socketChannel));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("socket error : " + e);
        }
    }
}

class ClientHandler extends Thread {
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    private final SocketChannel socket;
    ByteBuffer buffer = null;
    String sourcepath = "C:\\Users\\Newii\\Documents\\NetBeansProjects\\ZeroCopyServer\\files";
    FileChannel fileChannel;

    ClientHandler(SocketChannel client) {
        this.socket = client;
    }

    public void run() {
        try {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            socket.read(buf);
            String filename = new String(buf.array(), StandardCharsets.UTF_8);
            System.out.println(GREEN + "receive file to transfer : " + BLUE + filename);

            ByteBuffer buf1 = ByteBuffer.allocate(1024);
            socket.read(buf1);
            String newfilename = new String(buf1.array(), StandardCharsets.UTF_8);
            System.out.println(GREEN + "receive new filename : " + BLUE + newfilename );

            sentFile(filename);
        } catch (IOException e) {
            System.out.println("method run : "+e);
        }
    }
    public void sentFile(String filename) throws FileNotFoundException, IOException {
        String path = sourcepath + "\\" + filename.trim();
        System.out.println(RED + path);
        fileChannel = new FileInputStream(path).getChannel();
        long size = fileChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.asLongBuffer().put(fileChannel.size());
        socket.write(buffer);
        System.out.println(GREEN + "File Size :"+ BLUE + size + " byteBuffer");
        Long start = System.currentTimeMillis();
        long num = 0;
        while (num < size) {
            long tranferToCount = fileChannel.transferTo(num, size - num, socket);
            if (tranferToCount <= 0) {
                break;
            }
            num += tranferToCount;
        }
        status(num, size, start);
        System.out.println(GREEN + "Transfer File Success");
    }
    public static void status(long now, long max, long start) {
        long sum = (now * 100) / max;
        long time = (System.currentTimeMillis() / 1000) - start / 1000;
        String a = sum + "% " + Long.toString(time) + "sec ";
        for (int i = 0; i < sum; i++) {
            if (i % 3 == 0) {
                a += "";
            }
        }
        Long min = time / 60;
        Long sec = time - (min * 60);
        System.out.println(GREEN + "Use Time : " + BLUE + min + " min " + sec + " sec");
        
    }
    
}
