package urbonas.modestas;

import java.net.Socket;
import java.util.List;

public class FtpResponseWithSocket extends FtpResponse {
    private final Socket socket;

    public FtpResponseWithSocket(FtpResponse response, Socket socket) {
        super(response.getStatus(), response.getMessages());
        this.socket = socket;
    }
    public FtpResponseWithSocket(int status, List<String> messages, Socket socket) {
        super(status, messages);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return "FtpResponseWithSocket { " + super.toString() + ", " + socket + " }";
    }
}
