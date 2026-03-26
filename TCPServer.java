import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Stack;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
        if (o1.equals("(") || o2.equals(")")){
            return false;
        }
        int o1Prior = priorityCheck(o1);
        int o2Prior = priorityCheck(o2);
        if (o1.equals("^") && o2.equals("^")){
            return false;
        }
        return o1Prior >= o2Prior;
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

    // Equation solving function using Shunting yards
    private String solveEquation(String eq){
        try {
            String[] toks = eq.split(" ");
            Stack<Double> operands = new Stack<>();
            Stack<String> operators = new Stack<>();
            if (toks.length < 3 || toks.length % 2 == 0){
                return "Error: Bad format. Must write '1 + 1'\n";
            }
            for (String t : toks){
                if (t.isEmpty()){
                    continue;
                }
                if (t.matches("-?\\d+(\\.\\d+)?")){
                    operands.push(Double.parseDouble(t));
                } 
                else if ("+-*/%^".contains(t)){
                    while(!operators.isEmpty() && checkPrecedence(t, operators.peek())){
                        operands.push(operation(operands.pop(), operators.pop(), operands.pop()));
                    }
                    operators.push(t);
                }
                else if (t.equals("(")){
                    operators.push(t);
                }
                else if (t.equals(")")){
                    while (!operators.isEmpty() && !operators.peek().equals("(")){
                        operands.push(operation(operands.pop(), operators.pop(), operands.pop()));
                    }
                    operators.pop();
                } 
            }
            while (!operators.isEmpty()){
                operands.push(operation(operands.pop(), operators.pop(), operands.pop()));
            }
            return "Result: " + operands.pop();
            
            }catch (Exception e){
               return "Error: Could not calculate due to invalid format, must use spaces. Ex: 1 + ( 1 * 3 )";
            }
    }

    // Defining format of the server
    @Override
    public void run(){
        long startTime = System.currentTimeMillis();
        String arriveTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        try{
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());

            String clientName = clientInput.readLine();
            System.out.println("Client " + clientName + " connected.");
            serverOutput.writeBytes("Welcome client " + clientName + ", you joined at time: " + arriveTime + "\n");

            String clientEquation;
            while ((clientEquation = clientInput.readLine()) != null){
                if (clientEquation.equalsIgnoreCase("QUIT")){
                    break;
                }
                System.out.println("Request from client " + clientName + ": " + clientEquation);
                String solution = solveEquation(clientEquation);
                serverOutput.writeBytes(solution + "\n");
            }
            long endTime = System.currentTimeMillis();
            long sessionTime = (endTime - startTime) / 1000;
            System.out.println("Client " + clientName + " disconnected. Duration: " + sessionTime + "s");
            socket.close();
        } catch (IOException e){
            System.out.print("Error handling client/n");
        }
    }
}

// Main function 
class TCPServer {
  public static void main(String argv[]) throws Exception
    {
      try(ServerSocket welcomeSocket = new ServerSocket(6789)){
        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            ClientHandler handler = new ClientHandler(connectionSocket);
            Thread client = new Thread(handler);
            client.start();
            
        }
      } catch (IOException e){
        System.err.println("Error: " + e.getMessage());
      }

      
    }
}

