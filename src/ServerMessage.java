
public class ServerMessage {
    private String line;

    public ServerMessage(String line) {
        this.line = line;
    }

    public ServerMessage.MessageType getMessageType() {
        ServerMessage.MessageType result = ServerMessage.MessageType.UNKOWN;

        try {
            if (this.line != null && this.line.length() > 0) {
                String[] splits = this.line.split("\\s+");
                String lineTypePart = splits[0];
                if (lineTypePart.startsWith("-") || lineTypePart.startsWith("+")) {
                    lineTypePart = lineTypePart.substring(1);
                }

                result = ServerMessage.MessageType.valueOf(lineTypePart);
            }
        } catch (IllegalArgumentException var4) {
            System.out.println("[ERROR] Unknown command");
        }

        return result;
    }

    public String getPayload() {
        if (this.getMessageType().equals(ServerMessage.MessageType.UNKOWN)) {
            return this.line;
        } else if (this.line != null && this.line.length() >= this.getMessageType().name().length() + 1) {
            int offset = 0;
            if (this.getMessageType().equals(ServerMessage.MessageType.OK) || this.getMessageType().equals(ServerMessage.MessageType.ERR)) {
                offset = 1;
            }

            return this.line.substring(this.getMessageType().name().length() + 1 + offset);
        } else {
            return "";
        }
    }

    public String toString() {
        return this.line;
    }

    public static enum MessageType {
        HELO,
        BCST,
        WISP,
        USRS,
        PING,
        DSCN,
        OK,
        ERR,
        UNKOWN;


        private MessageType() {
        }
    }
}
