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

        File file = new File("./src/" + filename);
        byte[] bytes = new byte[16 * 1024];
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (in.read(bytes) > 0) {
                socket.getOutputStream().write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
