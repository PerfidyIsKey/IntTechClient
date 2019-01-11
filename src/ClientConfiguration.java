

public class ClientConfiguration {
    public final String VERSION = "1.0";
    public final String DEFAULT_SERVER_IP = "127.0.0.1";
    public final int DEFAULT_SERVER_PORT = 1337;
    public final String RESET_CLI_COLORS = "\u001b[0m";
    public final String CLI_COLOR_INCOMING = "\u001b[31m";
    public final String CLI_COLOR_OUTGOING = "\u001b[32m";
    private String serverIp = "127.0.0.1";
    private int serverPort = 1337;
    private boolean showColors = true;
    private boolean showLogging = true;

    public ClientConfiguration() {
    }

    public boolean isShowColors() {
        return this.showColors;
    }

    public void setShowColors(boolean showColors) {
        this.showColors = showColors;
    }

    public boolean isShowLogging() {
        return this.showLogging;
    }

    public void setShowLogging(boolean showLogging) {
        this.showLogging = showLogging;
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
