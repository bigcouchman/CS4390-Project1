// CS 4390 Math Networking Project by Nguyen Do (npd220001) Server logic

// Import libraries
import java.io.*;
import java.net.*;
import java.util.Stack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Create Client handler class via a welcoming socket
class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket){
        this.socket = socket;
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
            case "/":                   // Divide operation
                if (num2 == 0){
                    throw new ArithmeticException("Error: Cannot divide by zero.");
                }
                return num1 / num2;
            case "%":
                return num1 % num2;
            case "^":
                return Math.pow(num1, num2);
        }
        return 0;
    }

    // Check precendence of operators
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
    private int priorityCheck(String operator){
         switch(operator){
            case "+":
                return 1;
            case "-":
                return 1;
            case "*":
                return 2;
            case "/":
                return 2;
            case "%":
                return 2;
            case "^":
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
            // I will clarify the formatting of equations in the report 
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
                // Handles parentheses
                else if (t.equals("(")){
                    operators.push(t);
                }
                else if (t.equals(")")){
                    // For closing parentheses, finish the operation
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

            // When a client join, track times and dates joined
            String client = inFromUser.readLine();
            System.out.println("Client " + client + " connected at " + arriveTime + ".");
            outToServer.writeBytes("Welcome client " + client + ", you joined at time: " + arriveTime + "\n");

            // Handles equation request from client
            String clientReq;
            while ((clientReq = inFromUser.readLine()) != null){
                if (clientReq.equalsIgnoreCase("QUIT")){
                    break;
                }
                System.out.println("Request from client " + client + ": " + clientReq);
                String solution = solveEquation(clientReq);
                outToServer.writeBytes(solution + "\n");
            }
            // Log time and date that the client leaves the connection
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            String leaveTime = LocalDateTime.now().format(dateFormat);
            System.out.println("Client " + client + " disconnected at " + leaveTime + ". Duration: " + duration + "s");
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
        // Create welcoming socket for each client
      try(ServerSocket welcomeSocket = new ServerSocket(6789)){
        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            ClientHandler handler = new ClientHandler(connectionSocket);
            Thread client = new Thread(handler);
            client.start();        
        }
        // Error handling
      } catch (IOException e){
        System.err.println("Error: " + e.getMessage());
      } 
    }
}

