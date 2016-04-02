package urbonas.modestas;

import java.io.IOException;
import java.net.Socket;

public class FtpClient {
    private final Socket socket;

    public FtpClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }
}
