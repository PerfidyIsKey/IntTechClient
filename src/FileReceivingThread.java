import java.io.*;
import java.net.Socket;

public class FileReceivingThread implements Runnable {

    private Client parent;
    private int port;
    private String filename;

    FileReceivingThread(Client parent, int port, String filename){
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
        InputStream in = null;
        try {
            in = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String home = System.getProperty("user.home");
        System.out.println(filename);
        String[] split = filename.split("\\.");
        String suffix = split[1];
        String name = split[0];
        name = name + "(new)";
        filename = name + "." + suffix;

        File file = new File(home + "/Downloads/" + filename);

        byte[] bytes = new byte[1024];
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        try {
            while (in.available() != 0) {
                if (in.read(bytes) > 0) {
                    out.write(bytes);
                }
            }
            System.out.println("File " + filename + " received.");

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
