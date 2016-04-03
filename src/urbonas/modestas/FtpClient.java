package urbonas.modestas;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtpClient {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private static final int BUFFER_SIZE = 1024;

    public FtpClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        socket.close();
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

    public FtpTransferResponse list() throws IOException {
        FtpResponseWithSocket pasvResponse = getPassiveSocket();
        Socket socket = pasvResponse.getSocket();
        if(socket == null) {
            return new FtpTransferResponse(pasvResponse, null);
        }

        FtpResponse response = execute("LIST");
        if(response.getStatus() != 150) {
            return new FtpTransferResponse(response, null);
        }

        byte[] output = readFromSocket(socket);

        response = readResponse();
        return new FtpTransferResponse(response, output);
    }

    public FtpTransferResponse retr(String filename) throws IOException {
        FtpResponse typeResponse = execute("TYPE I");
        if(typeResponse.getStatus() != 200) {
            return new FtpTransferResponse(typeResponse, null);
        }

        FtpResponseWithSocket pasvResponse = getPassiveSocket();
        Socket socket = pasvResponse.getSocket();
        if(socket == null) {
            return new FtpTransferResponse(pasvResponse, null);
        }

        FtpResponse response = execute("RETR " + filename);
        if(response.getStatus() != 150) {
            return new FtpTransferResponse(response, null);
        }

        byte[] output = readFromSocket(socket);

        response = readResponse();
        return new FtpTransferResponse(response, output);
    }

    public FtpResponse stor(String filename, byte[] bytes) throws IOException {
        FtpResponse typeResponse = execute("TYPE I");
        if(typeResponse.getStatus() != 200) {
            return typeResponse;
        }

        FtpResponseWithSocket pasvResponse = getPassiveSocket();
        Socket socket = pasvResponse.getSocket();
        if(socket == null) {
            return pasvResponse;
        }

        FtpResponse response = execute("STOR " + filename);
        if(response.getStatus() != 150) {
            return response;
        }

        writeToSocket(socket, bytes);

        return readResponse();
    }

    public FtpResponse quit() throws IOException {
        return execute("QUIT");
    }

    public FtpResponse execute(String command) throws IOException {
        debug("Executing \"" + command + "\"");
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
            debug("Response: " + line);
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

    private FtpResponseWithSocket getPassiveSocket() throws IOException {
        FtpResponse response = execute("PASV");
        if(response.getStatus() != 227) {
            return new FtpResponseWithSocket(response, null);
        }

        Pattern pattern = Pattern.compile("([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+),([0-9]+)");
        Matcher matcher = pattern.matcher(response.getJoinedMessages());
        if(!matcher.find() || matcher.groupCount() != 6) {
            throw new RuntimeException("Could not parse PASV output: " + response.getJoinedMessages());
        }

        String ip = matcher.group(1) + "." + matcher.group(2) + "."
                + matcher.group(3) + "." + matcher.group(4);

        int port = Integer.valueOf(matcher.group(5)) * 0x100 + Integer.valueOf(matcher.group(6));
        Socket socket = new Socket(ip, port);
        return new FtpResponseWithSocket(response, socket);
    }

    private byte[] readFromSocket(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        while((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }

        socket.close();

        return output.toByteArray();
    }

    private void writeToSocket(Socket socket, byte[] data) throws IOException {
        OutputStream output = socket.getOutputStream();
        output.write(data);
        socket.close();
    }

    private void debug(String text) {
        System.out.println(text);
    }
}
