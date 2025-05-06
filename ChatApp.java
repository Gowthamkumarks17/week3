import java.io.*;
import java.net.*;
import java.util.*;

// ==================== SERVER ====================
class ChatServer {
    private static final int PORT = 1234;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New user connected");

                ClientHandler client = new ClientHandler(clientSocket);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;

        ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Client setup failed.");
            }
        }

        public void run() {
            String message;
            try {
                while ((message = input.readLine()) != null) {
                    System.out.println("Received: " + message);
                    ChatServer.broadcast(message, this);
                }
            } catch (IOException e) {
                System.out.println("User disconnected.");
            } finally {
                try {
                    socket.close();
                    ChatServer.removeClient(this);
                } catch (IOException e) {
                    System.out.println("Error closing socket.");
                }
            }
        }

        void send(String message) {
            output.println(message);
        }
    }
}

// ==================== CLIENT ====================
class ChatClient {
    public static void startClient() {
        try {
            Socket socket = new Socket("localhost", 1234);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to chat. Type your messages:");

            // Thread to read messages from server
            new Thread(() -> {
                String message;
                try {
                    while ((message = serverInput.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Main thread for sending messages
            String input;
            while ((input = userInput.readLine()) != null) {
                serverOutput.println(input);
            }

        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
        }
    }
}

// ==================== MAIN ====================
public class ChatApp {
    public static void main(String[] args) {
        System.out.println("Enter 'server' to start as server, or 'client' to start as client:");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("server")) {
            ChatServer.startServer();
        } else if (choice.equals("client")) {
            ChatClient.startClient();
        } else {
            System.out.println("Invalid choice. Please run again and type 'server' or 'client'.");
        }

        scanner.close();
    }
}
