
// CS 4390 Math Networking Project by Nguyen Do (npd220001) and Jeremiah Boban (jxb220076) Server logic
// The main algorithms are applying a blocking queue to solve client requests at a FIFO order, and use
// Shunting Yard algorithm to solve these equations

// Import libraries
import java.io.*;
import java.net.*;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Request per equation to apply FIFO order in a request queue when solving equations
class Request {
    String client;
    String equation;
    DataOutputStream output;

    // Constructor for request
    public Request(String client, String equation, DataOutputStream output){
        this.client = client;
        this.equation = equation;
        this.output = output;
    }
}


// Request queue to apply FIFO order for every requests
class RequestQueue implements Runnable {
    // We find BlockingQueue to be most effective at enforcing the FIFO order
    private BlockingQueue<Request> queue;
    public RequestQueue(BlockingQueue<Request> queue){
        this.queue = queue;
    }
    // Math performing function with 2 operands and 1 operator
    private double operation(double num1, String op, double num2){
        switch(op){
            case "+":                   // Add operation
                return num1 + num2;
            case "-":                   // Subtract operation
                return num1 - num2;
            case "*":                   // Multiply operation
                return num1 * num2;
            case "/":                   // Divide operation (throw divide by 0 error)
                if (num2 == 0){
                    throw new ArithmeticException("Error: Cannot divide by zero.");
                }
                return num1 / num2;
            case "%":                   // Modulo operation
                return num1 % num2;
            case "^":                   // Exponent operation
                return Math.pow(num1, num2);
        }
        return 0;
    }

    // Check precendence of operators, good to resolve conflict between left and right associativity
    private boolean checkPrecedence(String o1, String o2){
        // Check if 2nd operator is either a parentheses or both operators are exponential
        if (o2.equals("(") || o2.equals(")")){
            return false;
        }
        // Check priority of each operator
        int o1Prior = priorityCheck(o1);
        int o2Prior = priorityCheck(o2);
        if (o1.equals("^") && o2.equals("^")){
            return false;
        }
        return o2Prior >= o1Prior;
    }

    // Assign priority on operator to check for later
    // Note that shunting yard automatically takes care of parentheses as highest precedence
    private int priorityCheck(String operator){
         switch(operator){
            case "+":
                return 1;   // For + and - with the lowest precedence
            case "-":
                return 1;
            case "*":       // For *, /, and % with middle precedence
                return 2;
            case "/":
                return 2;
            case "%":
                return 2;
            case "^":       // For ^ with highest precedence
                return 3;
            default:
                return 0;
        }
    }

    // Equation solving function using Shunting yards (my version)
    private String solveEquation(String eq){
        // Store operands and operators in stacks
        Stack<Double> operands = new Stack<>();
        Stack<String> operators = new Stack<>();
        try {
            // (Note in report) assumption is for the tokens to have a single space between them (including parentheses)
            String[] toks = eq.split(" ");
            
            // Take an equation and gather tokens (separated by spaces)
            double val2;
            double val1;
            for (String t : toks){
                // Check if a token is empty
                if (t.isEmpty()){
                    continue;
                }

                // Check if a token is a number
                if (t.matches("-?\\d+(\\.\\d+)?")){
                    operands.push(Double.parseDouble(t));
                } 

                // check if a token is an operator, pop 2 operands (numbers) and perform calculation
                else if ("+-*/%^".contains(t)){
                    while(!operators.isEmpty() && checkPrecedence(t, operators.peek())){
                        val2 = operands.pop();
                        val1 = operands.pop();
                        operands.push(operation(val1, operators.pop(), val2));
                    }
                    // Other than that, push the token to the stack
                    operators.push(t);
                }
                // Push front parentheses
                else if (t.equals("(")){
                    operators.push(t);
                }
                else if (t.equals(")")){
                    // For closing parentheses, take operator and 2 operands, do the equation, and pop that parentheses out
                    while (!operators.isEmpty() && !operators.peek().equals("(")){
                        val2 = operands.pop();
                        val1 = operands.pop();
                        operands.push(operation(val1, operators.pop(), val2));
                    }
                    operators.pop();
                }
                // If the token is unknown show error
                else {
                    return "Error: Unknown token '" + t + "'";
                } 
            }
            // pop out remaining characters as long as there are remaining operators
            while (!operators.isEmpty()){
                val2 = operands.pop();
                val1 = operands.pop();
                operands.push(operation(val1, operators.pop(), val2));
            }
            // If there is a format error
            if (operands.size() != 1){
                return "Error: Invalid equation format";
            }
            // Return result
            return "Result: " + operands.pop();
            
            // Catch other formatting exception
            }catch (Exception e){
               return "Error: Could not calculate due to invalid format, must use spaces. Ex: 1 + ( 1 * 3 )";
            }
    }

    @Override
    // Run function
    public void run(){
        while (true){
            // Get a request from the queue, solve the equation
            try{
                Request req = queue.take();
                Thread.sleep(1000);
                System.out.println("Request from client " + req.client + ": " + req.equation);
                String solution = solveEquation(req.equation);
                req.output.writeBytes(solution + "\n");
                // Log the server response to the log
                try (PrintWriter out = new PrintWriter(new FileWriter("server_logs.txt", true))){
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
                    out.println("[" + time + "] RESPONSE TO " + req.client + ": " + solution);
                }
                catch (IOException e){
                    System.err.println("Error: Could not write in log");
                }
            }
            // If there are any error, catch the error
            catch (Exception e){
                System.err.println("Error processing request");
            }
        }
    }
}

// Create Client handler class via a welcoming socket, requests create here are put in queue
class ClientHandler implements Runnable{
    private Socket socket;
    private BlockingQueue<Request> bqueue;
    public ClientHandler(Socket socket, BlockingQueue<Request> bqueue){
        this.socket = socket;
        this.bqueue = bqueue;
    }

    // Function to log client activities in a log file
    private synchronized void logClients(String message){
        // Store client activities in a log format, you can check it in server_logs.txt
        try (PrintWriter out = new PrintWriter(new BufferedWriter((new FileWriter("server_logs.txt", true))))){
            out.println(message);
        } catch (IOException e){
            System.err.println("Error: Could not write in log");
        }
    }

    // Defining format of the server
    @Override
    public void run(){
        // Variables to log time and date of clients
        long startTime = System.currentTimeMillis();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String arriveTime = LocalDateTime.now().format(dateFormat);
        try{
            // Keep track of input and ouput streams
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());

            // When a client join, track times and dates joined, log the join times in the log
            String client = inFromUser.readLine();
            System.out.println("Client " + client + " connected at " + arriveTime + ".");
            outToServer.writeBytes("Welcome client " + client + ", you joined at time: " + arriveTime + "\n");
            logClients("[" + arriveTime + "] JOIN: " + client);
            // Handles equation request from client
            String clientReq;
            while ((clientReq = inFromUser.readLine()) != null){
                if (clientReq.equalsIgnoreCase("QUIT")){
                    break;
                }
                // Every request a client send, put in the blocking queue, or catch an interrupt exception
                try{
                    bqueue.put(new Request(client, clientReq, outToServer));
                }
                catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    System.err.println("Thread was interrupted while queueing request.");
                }
                // log the request sent time in the log per client
                logClients("[" + LocalDateTime.now().format(dateFormat) + "] REQUEST from " + client + ": " + clientReq);
            }
            // Log time and date that the client leaves the connection, log the client leaving in a log
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            String leaveTime = LocalDateTime.now().format(dateFormat);
            System.out.println("Client " + client + " disconnected at " + leaveTime + ". Duration: " + duration + "s");
            logClients("[" + leaveTime + "] QUIT: " + client + ". Duration: " + duration + "s");
            socket.close();
            // Error handling
        } catch (IOException e){
            System.out.print("Error handling client\n");
        }
    }
}

// Main function 
class TCPServer {
  public static void main(String argv[]) throws Exception
    {
    // Log the server starting up and open for connection
      try (PrintWriter out = new PrintWriter(new FileWriter("server_logs.txt", true))){
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        out.println("[" + time + "] SERVER START: Server is opened for connection.");
      }
      catch (IOException e){
        System.err.println("Error: Could not write in log");
      }
        // Create a blocking queue to enforce FIFO order in clients
      System.out.println("Server is opened for connection...");
      BlockingQueue<Request> bQueue = new LinkedBlockingQueue<>();
      new Thread(new RequestQueue(bQueue)).start();
        // Create welcoming socket for each client
      try(ServerSocket welcomeSocket = new ServerSocket(6789)){
        while(true) {
            // This approach lets us create a thread safe queue without any interference
            Socket connectionSocket = welcomeSocket.accept();
            new Thread(new ClientHandler(connectionSocket, bQueue)).start();
        }
        // Error handling
      } catch (IOException e){
        System.err.println("Error: " + e.getMessage());
      } 
    }
}
