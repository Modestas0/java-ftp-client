package urbonas.modestas;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FtpClient {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public FtpClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private FtpResponse readResponse() throws IOException {
        Integer status = null;
        List<String> messages = new ArrayList<>();
        String line;
        boolean completed = false;

        do {
            line = reader.readLine();
            if(line.matches("^[0-9]{3}[ -]")) {
                status = Integer.valueOf(line.substring(0, 3));
                messages.add(line.substring(4));

                if(line.charAt(3) == ' ') {
                    completed = true;
                }
            } else if(status != null) {
                messages.add(line);
            } else {
                throw new RuntimeException("Invalid FTP response");
            }
        } while(!completed);

        return new FtpResponse(status, messages);
    }
}
