package urbonas.modestas;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;

public class Main {

    public static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        System.out.println("Hello!");
        printMenu();

        FtpClient client = null;
        try {
            client = new FtpClient("194.135.80.233", 21);
            printResponse(client.getWelcomeMessage());

            while (client.isRunning()) {
                processCommand(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printMenu() {
        System.out.println("Commands: ");
        System.out.println("help - print available commands");
        System.out.println("login username - login to ftp server");
        System.out.println("cd path - changes directory to specified path");
        System.out.println("pwd - prints current directory");
        System.out.println("ls - lists directory contents");
        System.out.println("mkdir path - creates specified directory");
        System.out.println("rmdir path - deletes specified directory");
        System.out.println("download filename destination_filename - downloads file from server");
        System.out.println("upload filename destination_filename - uploads file from local filesystem to server");
        System.out.println("rm filename - deletes specified file from server");
        System.out.println("quit - quit client");
    }

    private static void processCommand(FtpClient client) throws IOException {
        System.out.print("> ");

        String line = input.readLine();
        String[] args = line.split(" ");
        if (args.length < 1) {
            return;
        }

        String command = args[0];

        if ("help".equals(command)) {
            printMenu();
        } else if ("login".equals(command)) {
            if (args.length != 2) {
                System.out.println("Wrong number of arguments");
                return;
            }

            FtpResponse response = client.user(args[1]);
            printResponse(response);

            if (response.getStatus() == 331 || response.getStatus() == 332) {
                System.out.print("Password: ");
                line = input.readLine();
                printResponse(client.pass(line));
            } else {
                printResponse(response);
            }
        } else if ("cd".equals(command)) {
            if (args.length != 2) {
                System.out.println("Wrong number of arguments");
                return;
            }
            printResponse(client.cwd(args[1]));
        } else if ("pwd".equals(command)) {
            if (args.length != 1) {
                System.out.println("Wrong number of arguments");
                return;
            }
            printResponse(client.pwd());
        } else if ("ls".equals(command)) {
            if (args.length != 1) {
                System.out.println("Wrong number of arguments");
                return;
            }

            printResponse(client.list(System.out));
        } else if ("mkdir".equals(command)) {
            if (args.length != 2) {
                System.out.println("Wrong number of arguments");
                return;
            }
            printResponse(client.mkd(args[1]));
        } else if ("rmdir".equals(command)) {
            if (args.length != 2) {
                System.out.println("Wrong number of arguments");
                return;
            }
            printResponse(client.rmd(args[1]));
        } else if ("download".equals(command)) {
            if (args.length != 3) {
                System.out.println("Wrong number of arguments");
                return;
            }

            FileOutputStream fileOutputStream = new FileOutputStream(args[2]);
            FtpResponse response = client.retr(args[1], fileOutputStream);
            fileOutputStream.close();

            printResponse(response);
        } else if ("upload".equals(command)) {
            if (args.length != 3) {
                System.out.println("Wrong number of arguments");
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(args[1]);
            FtpResponse response = client.stor(args[2], fileInputStream);
            fileInputStream.close();

            printResponse(response);
        } else if ("rm".equals(command)) {
            if (args.length != 2) {
                System.out.println("Wrong number of arguments");
                return;
            }
            printResponse(client.dele(args[1]));
        } else if ("quit".equals(command)) {
            if (args.length != 1) {
                System.out.println("Wrong number of arguments");
                return;
            }
            printResponse(client.quit());
        } else {
            System.out.println("Command not recognized: " + command);
        }
    }

    private static void printResponse(FtpResponse response) {
        String type = getStatusType(response.getStatus());

        List<String> messages = response.getMessages();

        if (messages.size() == 0) {
            System.out.println(type + "[" + response.getStatus() + "]");
        } else {
            System.out.println(type + "[" + response.getStatus() + "]: " + messages.get(0));
            for (int i = 1; i < messages.size(); ++i) {
                String padding = StringUtils.repeat(' ', type.length() + 7);
                System.out.println(padding + messages.get(i));
            }
        }
    }

    public static String getStatusType(int statusCode) {
        if (statusCode < 100) {
            return "UNKNOWN";
        } else if (statusCode < 200) { // 1XX
            return "EXPECT ANOTHER REPLY";
        } else if (statusCode < 300) { // 2XX
            return "COMPLETED";
        } else if (statusCode < 400) { // 3XX
            return "ANOTHER COMMAND EXPECTED";
        } else if (statusCode < 500) { // 4XX
            return "NOT COMPLETED";
        } else if (statusCode < 600) { // 5XX
            return "ERROR";
        } else {
            return "UNKNOWN";
        }
    }
}
