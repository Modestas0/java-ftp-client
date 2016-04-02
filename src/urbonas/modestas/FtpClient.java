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

    public FtpResponse getWelcomeMessage() throws IOException {
        return readResponse();
    }

    public FtpResponse user(String username) throws IOException {
        return execute("USER " + username);
    }

    public FtpResponse pass(String password) throws IOException {
        return execute("PASS " + password);
    }

    public FtpResponse syst() throws IOException {
        return execute("SYST");
    }

    public FtpResponse cwd(String directory) throws IOException {
        return execute("CWD " + directory);
    }

    public FtpResponse pwd() throws IOException {
        return execute("PWD");
    }

    public FtpResponse mkd(String directory) throws IOException {
        return execute("MKD " + directory);
    }

    public FtpResponse rmd(String directory) throws IOException {
        return execute("RMD " + directory);
    }

    public FtpResponse size(String file) throws IOException {
        return execute("SIZE " + file);
    }

    public FtpResponse quit() throws IOException {
        return execute("QUIT");
    }

    public FtpResponse execute(String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
        return readResponse();
    }

    private FtpResponse readResponse() throws IOException {
        Integer status = null;
        List<String> messages = new ArrayList<>();
        String line;
        boolean completed = false;

        // https://www.ietf.org/rfc/rfc959
        // page 36
        do {
            line = reader.readLine();
            if(line.matches("^[0-9]{3}[ -].*")) {
                status = Integer.valueOf(line.substring(0, 3));
                messages.add(line.substring(4));

                if(line.charAt(3) == ' ') {
                    completed = true;
                }
            } else if(status != null) {
                messages.add(line);
            } else {
                throw new RuntimeException("Invalid FTP response: " + line);
            }
        } while(!completed);

        return new FtpResponse(status, messages);
    }
}
