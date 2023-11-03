import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Scanner;

// Server class
class Server {
    static String IP = "localhost";
    static int PORTC = 9999;
    public static void main(String[] args) {
        ServerSocket server = null;
        ServerSocketChannel serverChannel;
        int count = 0;
        try {

            // server is listening on port 1234
            server = new ServerSocket(9998);
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(IP, PORTC));
            // server.setReuseAddress(true);
            System.out.println("open server");
            // running infinite loop for getting
            // client request
            while (true) {
                count++;
                // socket object to receive incoming client
                // requests
                Socket client = server.accept();
                System.out.println("From client"+client.getRemoteSocketAddress());
                // System.out.println(client.getInetAddress());
                SocketChannel socketChannel = serverChannel.accept();
                // Displaying that new client is connected
                // to server
                System.out.println("New client connected " + count + " "
                        + client.getInetAddress()
                                .getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client,socketChannel);

                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final SocketChannel socketChannel;

        // Constructor
        public ClientHandler(Socket socket,SocketChannel socketChannel) {
            this.clientSocket = socket;
            this.socketChannel=socketChannel;
        }

        public void run() {

            try {

                DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
                // get the outputstream of client
                String filePath = "my_file.mp4"; /** edit here */
                String line = "";

                while (!line.equals("bye")) {
                    line = inStream.readUTF();
                    if (line.equals("d")) {
                        sendFile(filePath, outStream);
                    } else if (line.equals("z")) {
                        FileChannel source = new FileInputStream(filePath).getChannel();
                        long size = source.size();
                        outStream.writeLong(size);
                        Zero_Copy(filePath,size);
                        System.out.println("Video file sent successfully.");
                    }
                    System.out.println("Sent from the client: " + line);
                    outStream.writeUTF("hello client ");
                    outStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                System.out.println("hello");
            }
        }

        public void Zero_Copy(String filePath,long size) {
            FileChannel source = null;
            try {
                source = new FileInputStream(filePath).getChannel();
                long currentRead = 0;
                long read;
                while (currentRead < size
                        && (read = source.transferTo(currentRead, size - currentRead, socketChannel)) != -1) {
                    currentRead += read;
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (source != null) {
                        source.close();
                    }
                } catch (IOException e) {
                    
                }
            }
        }

        public void sendFile(String path, DataOutputStream outStream) throws Exception {
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            // send file size
            outStream.writeLong(file.length());
            // break file into chunks
            double sum = 0;
            byte[] buffer = new byte[1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                sum += bytes;
                // System.out.println("Server send file :" + String.format("%.2f", (sum) /
                // file.length() * 100) + " %");
                outStream.write(buffer, 0, bytes);
                outStream.flush();

            }
            fileInputStream.close();
        }

    }
}