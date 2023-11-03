import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

// Client class
class Client {

    // driver code
    static SocketChannel socketChannel ;
    static String IP = "localhost";
    static int PORTC = 9999;
    public static void main(String[] args) {
        // establish a connection by providing host and port
        // number
        try (Socket socket = new Socket(IP, 9998)) {
            DataInputStream inStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            Client.socketChannel = SocketChannel.open(new InetSocketAddress(IP, PORTC));
            Scanner sc = new Scanner(System.in);
            String line = "";

            while (!"exit".equalsIgnoreCase(line)) {

                // reading from user
                line = sc.nextLine();

                // sending the user input to server
                outStream.writeUTF(line);
                outStream.flush();
                // displaying server reply

                if (line.equals("d")) {
                    long startTime = System.currentTimeMillis();
                    dowload_normal(inStream);
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken in  "+(endTime - startTime)+" ms.");
                } else if (line.equals("z")) {
                    long startTime = System.currentTimeMillis();
                    long size = inStream.readLong();
                    // System.out.println(size);
                    Zero_Copy(size);
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken in  "+(endTime - startTime)+" ms.");
                }
                line = inStream.readUTF();
                System.out.println("Server replied : " + line);
            }

            // closing the scanner object
            sc.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  static void Zero_Copy(long size) {
        FileChannel destination = null;
        try {
            destination = new FileOutputStream("receive_file.mp4").getChannel();
            long currentRead = 0;
            long read;
            while (currentRead < size && (read = destination.transferFrom(socketChannel, currentRead, size - currentRead)) != -1) {
                currentRead += read;
            }
        } catch (IOException e) {
        } finally {
            try {
                if (destination != null) {
                    destination.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
    private static void dowload_normal(DataInputStream inStream) throws FileNotFoundException, IOException {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(
                "receive_file.mp4"); /** edit here */
        long size = inStream.readLong(); // read file size
        byte[] buffer = new byte[1024];
        double sum = 0;
        double size_exis = size;
        System.out.println("file size :" + size);
        while (size > 0 && (bytes = inStream.read(buffer, 0,
                (int) Math.min(buffer.length, size))) != -1) {
            sum += bytes;
            //System.out.println("Client get file:" + String.format("%.2f", (sum) / size_exis * 100) + " %");
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        System.out.println(
                "successfully get size file:" + String.format("%.2f", size_exis / (1024 * 1024)) + " MB");
        fileOutputStream.close();
    }
}