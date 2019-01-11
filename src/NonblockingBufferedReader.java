

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NonblockingBufferedReader {
    private final BlockingQueue<String> lines = new LinkedBlockingQueue();
    private volatile boolean closed = false;
    private Thread backgroundReaderThread = null;

    public NonblockingBufferedReader(final BufferedReader bufferedReader) {
        this.backgroundReaderThread = new Thread(new Runnable() {
            public void run() {
                try {
                    while(!Thread.interrupted()) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }

                        NonblockingBufferedReader.this.lines.add(line);
                    }
                } catch (IOException var5) {
                    throw new RuntimeException(var5);
                } finally {
                    NonblockingBufferedReader.this.closed = true;
                }

            }
        });
        this.backgroundReaderThread.setDaemon(true);
        this.backgroundReaderThread.start();
    }

    public String readLine() throws IOException {
        try {
            return this.closed && this.lines.isEmpty() ? null : (String)this.lines.poll(500L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException var2) {
            throw new IOException("The BackgroundReaderThread was interrupted!", var2);
        }
    }

    public void close() {
        if (this.backgroundReaderThread != null) {
            this.backgroundReaderThread.interrupt();
            this.backgroundReaderThread = null;
        }

    }
}
