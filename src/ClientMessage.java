

public class ClientMessage {
    private ClientMessage.MessageType type;
    private String line;

    public ClientMessage(ClientMessage.MessageType type, String line) {
        this.type = type;
        this.line = line;
    }

    public String toString() {
        return this.type + " " + this.line;
    }

    public static enum MessageType {
        HELO,
        BCST,
        PONG,
        WISP,
        KICK,
        GRPS,
        GRP,
        JOIN,
        USRS,
        LEVE,
        CRTE,
        FILE,
        DATA,
        QUIT;

        private MessageType() {
        }
    }
}
