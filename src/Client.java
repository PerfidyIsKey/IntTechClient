
import javax.print.DocFlavor;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.Stack;

public class Client {
    public ClientConfiguration conf;
    private Socket socket;
    Client.MessageReader readerThread;
    Client.MessageWriter writerThread;
    boolean isConnected = false;
    NonblockingBufferedReader nonblockReader;
    private Stack<ClientMessage> clientMessages = new Stack();
    private Stack<ServerMessage> serverMessages = new Stack();
    private String privateKey = "";
    private String publicKey = "";

    public Client(ClientConfiguration conf) {
        this.conf = conf;
    }

    public void start() {

        try {
            this.socket = new Socket(this.conf.getServerIp(), this.conf.getServerPort());
            InputStream is = this.socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            this.readerThread = new Client.MessageReader(reader);
            (new Thread(this.readerThread)).start();
            OutputStream os = this.socket.getOutputStream();
            PrintWriter writer = new PrintWriter(os);
            this.writerThread = new Client.MessageWriter(writer);
            (new Thread(this.writerThread)).start();

            while (true) {
                if (!this.serverMessages.empty()) {
                    ServerMessage serverMessage = this.serverMessages.pop();
                    if (!serverMessage.getMessageType().equals(ServerMessage.MessageType.HELO)) {
                        System.out.println("Expecting a HELO message but received: " + serverMessage.toString());
                    } else {
                        System.out.println("Please fill in your username: ");
                        Scanner scanner = new Scanner(System.in);
                        String username = scanner.nextLine();
                        ClientMessage heloMessage = new ClientMessage(ClientMessage.MessageType.HELO, username);
                        this.clientMessages.push(heloMessage);

                        while (this.serverMessages.empty()) {

                        }

                        this.isConnected = this.validateServerMessage(heloMessage, this.serverMessages.pop());
                        if (!this.isConnected) {
                            System.out.println("Error logging into server");
                        } else {
                            System.out.println("Successfully connected to server.");
                            System.out.println("(Type 'quit' to close connection and stop application.)");
                            System.out.println("Type a message: ");
                            this.nonblockReader = new NonblockingBufferedReader(new BufferedReader(new InputStreamReader(System.in)));


                            while (this.isConnected) {
                                String line = this.nonblockReader.readLine();
                                if (line != null) {
                                    ClientMessage clientMessage;
                                    if (line.startsWith("/whisper ")) {
                                        line = line.replaceFirst("/whisper ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.WISP, line);
                                    } else if (line.startsWith("/kick ")) {
                                        line = line.replaceFirst("/kick ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.KICK, line);
                                    } else if (line.startsWith("/join ")) {
                                        line = line.replaceFirst("/join ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.JOIN, line);
                                    } else if (line.startsWith("/groups")) {
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.GRPS, "");
                                    } else if (line.startsWith("/group ")) {
                                        line = line.replaceFirst("/group ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.GRP, line);
                                    } else if (line.startsWith("/users")) {
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.USRS, "");
                                    } else if (line.startsWith("/leave ")) {
                                        line = line.replaceFirst("/leave ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.LEVE, line);
                                    } else if (line.startsWith("/create ")) {
                                        line = line.replaceFirst("/create ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.CRTE, line);
                                    } else if (line.startsWith("/file ")) {
                                        line = line.replaceFirst("/file ", "");
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.FILE, line);
                                    } else if (line.startsWith("/quit")) {
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.QUIT, "");
                                        this.isConnected = false;
                                        Thread.sleep(500L);
                                    } else {
                                        clientMessage = new ClientMessage(ClientMessage.MessageType.BCST, line);
                                    }

                                    this.clientMessages.push(clientMessage);
                                }

                                if (!this.serverMessages.empty()) {
                                    ServerMessage received = this.serverMessages.pop();
                                    if (received.getMessageType().equals(ServerMessage.MessageType.BCST)) {
                                        System.out.println(received.getPayload());
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.SUC)) {
                                        System.out.println(received.getPayload());
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.ERR)) {
                                        System.out.println(received.getPayload());
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.WISP)) {
                                        System.out.println(received.getPayload());
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.GRP)) {
                                        System.out.println(received.getPayload());
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.USRS)) {
                                        String message = received.getPayload();
                                        String[] split = message.split(" ");
                                        System.out.println("List of connected users:");
                                        for (int i = 0; i < split.length; i++) {
                                            System.out.println(split[i]);
                                        }
                                        System.out.println("--------");
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.GRPS)) {
                                        String message = received.getPayload();
                                        String[] split = message.split(" ");
                                        System.out.println("List of groups:");
                                        for (int i = 0; i < split.length; i++) {
                                            System.out.println(split[i]);
                                        }
                                        System.out.println("--------");
                                    } else if(received.getMessageType().equals(ServerMessage.MessageType.SFILE)) {
                                        String message = received.getPayload();
                                        String[] split = message.split(" ");
                                        String filename = split[1];
                                        String port = split[2];
                                        sendFile(filename, Integer.parseInt(port));
                                    } else if(received.getMessageType().equals(ServerMessage.MessageType.RFILE)) {
                                        String message = received.getPayload();
                                        String[] split = message.split(" ");
                                        String filename = split[1];
                                        String port = split[2];
                                        createFile(filename, Integer.parseInt(port));
                                    } else if (received.getMessageType().equals(ServerMessage.MessageType.DATA)) {
                                        String message = received.getPayload();
                                        String[] split = message.split(" ");
                                        String filename = split[0];
                                        String receivedBytes = message.replaceFirst(filename + " ", "");
                                        FileWriter out = null;
                                        boolean success = true;
                                        try {
                                            String home = System.getProperty("user.home");
                                            out = new FileWriter(home + "/Downloads/" + filename);
                                        } catch (FileNotFoundException ex) {
                                            System.out.println("File not found. ");
                                            success = false;
                                        }
                                        if (success) {
                                            receivedBytes = receivedBytes.replace("[", "");
                                            receivedBytes = receivedBytes.replace("]", "");
                                            receivedBytes = receivedBytes.replace(" ", "");
                                            String[] array = receivedBytes.split(",");

                                            for (int i = 0; i < array.length; i++) {
                                                int character = Integer.parseInt(array[i]);
                                                out.write((char)character);
                                            }

                                            System.out.println("Received file: " + filename);
                                            out.close();
                                        }
                                    }
                                }
                            }
                        }

                        this.disconnect();
                        System.out.println("Client disconnected!");
                    }
                    break;
                }
            }
        } catch (IOException var11) {
            System.out.println("Ouch! Could not connect to server!");
        } catch (InterruptedException var12) {
            var12.printStackTrace();
        }

    }

    public void sendFile(String filename, int port) {
        FileSendingThread thread = new FileSendingThread(this, port, filename);
        Thread t1 = new Thread(thread);
        t1.start();
    }

    public void createFile(String filename, int port){
        FileReceivingThread thread = new FileReceivingThread(this, port, filename);
        Thread t1 = new Thread(thread);
        t1.start();
    }

    private boolean validateServerMessage(ClientMessage clientMessage, ServerMessage serverMessage) {
        boolean isValid = false;

        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(clientMessage.toString().getBytes());
            String encodedHash = new String(Base64.getEncoder().encode(hash));
            if (serverMessage.getMessageType().equals(ServerMessage.MessageType.OK) && encodedHash.equals(serverMessage.getPayload())) {
                isValid = true;
            }
        } catch (NoSuchAlgorithmException var6) {
            var6.printStackTrace();
        }

        return isValid;
    }

    private void disconnect() {
        if (this.readerThread != null) {
            this.readerThread.kill();
        }

        if (this.writerThread != null) {
            this.writerThread.kill();
        }

        if (this.nonblockReader != null) {
            this.nonblockReader.close();
        }

        this.isConnected = false;
    }

    public class MessageWriter implements Runnable {
        private volatile boolean isRunning = true;
        PrintWriter writer;

        public MessageWriter(PrintWriter writer) {
            this.writer = writer;
        }

        public void run() {
            while (this.isRunning) {
                if (!Client.this.clientMessages.empty()) {
                    this.writeToServer(Client.this.clientMessages.pop(), this.writer);
                }
            }

        }

        private void writeToServer(ClientMessage message, PrintWriter writer) {
            String line = message.toString();
            if (Client.this.conf.isShowLogging()) {
                if (Client.this.conf.isShowColors()) {
                    Client.this.conf.getClass();
                    String colorCode = "\u001b[32m";
                    PrintStream var10000 = System.out;
                    StringBuilder var10001 = (new StringBuilder()).append(colorCode).append("<< ").append(line);
                    Client.this.conf.getClass();
                    var10000.println(var10001.append("\u001b[0m").toString());
                } else {
                    System.out.println("<< " + line);
                }
            }

            writer.println(line);
            writer.flush();
        }

        public void kill() {
            this.isRunning = false;
        }
    }

    public class MessageReader implements Runnable {
        private volatile boolean isRunning = true;
        private BufferedReader reader;

        public MessageReader(BufferedReader reader) {
            this.reader = reader;
        }

        public void run() {
            int receiveNull = 0;

            while (this.isRunning) {
                String line = this.readFromServer(this.reader);
                if (line == null) {
                    ++receiveNull;
                    if (receiveNull >= 3) {
                        Client.this.disconnect();
                    }
                } else {
                    ServerMessage message = new ServerMessage(line);
                    if (message.getMessageType().equals(ServerMessage.MessageType.PING)) {
                        ClientMessage pongMessage = new ClientMessage(ClientMessage.MessageType.PONG, "");
                        Client.this.clientMessages.push(pongMessage);
                    }

                    if (message.getMessageType().equals(ServerMessage.MessageType.DSCN)) {
                        System.out.println("Client disconnected by server.");
                        Client.this.disconnect();
                    }

                    Client.this.serverMessages.push(message);
                    receiveNull = 0;
                }
            }

        }

        private String readFromServer(BufferedReader reader) {
            String line = null;

            try {
                line = reader.readLine();
                if (line != null && Client.this.conf.isShowLogging()) {
                    if (Client.this.conf.isShowColors()) {
                        Client.this.conf.getClass();
                        String colorCode = "\u001b[31m";
                        PrintStream var10000 = System.out;
                        StringBuilder var10001 = (new StringBuilder()).append(colorCode).append(">> ").append(line);
                        Client.this.conf.getClass();
                        var10000.println(var10001.append("\u001b[0m").toString());
                    } else {
                        System.out.println("<< " + line);
                    }
                }
            } catch (IOException var4) {
                System.out.println("Error reading buffer: " + var4.getMessage());
            }

            return line;
        }

        public void kill() {
            this.isRunning = false;
        }
    }
}
