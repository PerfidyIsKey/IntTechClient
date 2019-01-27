import java.io.*;
import java.net.Socket;

public class FileSendingThread implements Runnable {

    private Client parent;
    private int port;
    private String filename;

    FileSendingThread(Client parent, int port, String filename){
        super();
        this.parent = parent;
        this.port = port;
        this.filename = filename;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(parent.conf.getServerIp(), port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String home = System.getProperty("user.home");

        File file = new File(home + "/Downloads/" + filename);
        byte[] bytes = new byte[1024];
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while (in.available() != 0) {
                if (in.read(bytes) > 0) {
                    socket.getOutputStream().write(bytes);
                }
            }
            System.out.println("File " + filename + " sent.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
