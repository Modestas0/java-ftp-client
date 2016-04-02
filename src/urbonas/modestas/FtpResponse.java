package urbonas.modestas;

import java.util.List;

public class FtpResponse {
    private int status;
    List<String> messages;

    public FtpResponse(int status, List<String> messages) {
        this.status = status;
        this.messages = messages;
    }

    public int getStatus() {
        return status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getJoinedMessages() {
        return String.join("\n", messages);
    }
}
