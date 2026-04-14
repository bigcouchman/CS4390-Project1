// CS 4390 Math Networking Project by Nguyen Do (npd220001) and Jeremiah Boban (jxb220076)
// Server logic

// Import libraries
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

// Request object for queue
class Request {
    String clientName;
    String equation;
    DataOutputStream output;

    public Request(String clientName, String equation, DataOutputStream output) {
        this.clientName = clientName;
        this.equation = equation;
        this.output = output;
    }
}

// Worker thread (processes requests in order)
class RequestProcessor implements Runnable {
    private BlockingQueue<Request> queue;

    public RequestProcessor(BlockingQueue<Request> queue) {
        this.queue = queue;
    }

    private String solveEquation(String eq) {
        try {
            String[] parts = eq.split(" ");
            double result = Double.parseDouble(parts[0]);

            for (int i = 1; i < parts.length; i += 2) {
                String op = parts[i];
                double num = Double.parseDouble(parts[i + 1]);

                switch (op) {
                    case "+": result += num; break;
                    case "-": result -= num; break;
                    case "*": result *= num; break;
                    case "/":
                        if (num == 0) return "Error: Divide by zero";
                        result /= num;
                        break;
                    default: return "Error: Unknown operator";
                }
            }
            return "RESULT " + result;
        } catch (Exception e) {
            return "Error: Invalid expression";
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request req = queue.take(); // FIFO
                System.out.println("Processing [" + req.clientName + "]: " + req.equation);

                String result = solveEquation(req.equation);
                req.output.writeBytes(result + "\n");

            } catch (Exception e) {
                System.out.println("Processing error");
            }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BlockingQueue<Request> queue;

    public ClientHandler(Socket socket, BlockingQueue<Request> queue) {
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String connectTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Expect JOIN
            String joinMsg = in.readLine();
            String clientName = joinMsg.substring(5).trim();

            System.out.println("[" + connectTime + "] " + clientName + " connected.");
            out.writeBytes("ACK Connected at " + connectTime + "\n");

            String msg;
            while ((msg = in.readLine()) != null) {

                if (msg.equalsIgnoreCase("EXIT")) break;

                if (msg.startsWith("CALC")) {
                    String equation = msg.substring(5);

                    System.out.println("Received from " + clientName + ": " + equation);

                    queue.put(new Request(clientName, equation, out));
                }
            }

            long endTime = System.currentTimeMillis();
            String disconnectTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            long duration = (endTime - startTime) / 1000;

            System.out.println("[" + disconnectTime + "] " + clientName +
                    " disconnected. Duration: " + duration + "s");

            socket.close();

        } catch (Exception e) {
            System.out.println("Client error");
        }
    }
}

public class TCPServer {
    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(6789);
        BlockingQueue<Request> queue = new LinkedBlockingQueue<>();

        // Start processor thread
        new Thread(new RequestProcessor(queue)).start();

        System.out.println("Server started...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket, queue)).start();
        }
    }
}