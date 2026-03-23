import java.io.*;
import java.net.*;
import java.util.Date;

class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    private String solveEquation(String eq){
        try {
            String[] parts = eq.split(" ");
            if (parts.length < 3 || parts.length % 2 == 0){
                return "Error: Bad format. Must write '1 + 1'\n";
            }
            double result = Double.parseDouble(parts[0]);
            for (int i = 1; i < parts.length; i += 2){
                String operator = parts[i];
                double operand = Double.parseDouble(parts[i + 1]);
                switch (operator){
                    
                }
            }
            if (operator.equals("+")){
                        result = num1 + num2;
                    }
                    else if (operator.equals("-")){
                        result = num1 - num2;
                    }
                    else if (operator.equals("*")){
                        result = num1 * num2;
                    }
                    else if (operator.equals("/")){
                        result = num1 / num2;
                    }
                    else if (operator.equals("%")){
                        result = num1 / num2;
                    }

                    serverOutput.writeBytes("Result: " + result + "\n");
                } catch (Exception e){
                    serverOutput.writeBytes("Error: Bad format. Must write '1 + 1'\n");
                }
    }

    @Override
    public void run(){
        long startTime = System.currentTimeMillis();
        try{
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());

            String clientName = clientInput.readLine();
            System.out.println("Client " + clientName + " connected.");
            serverOutput.writeBytes("Welcome client " + clientName + "\n");

            String clientEquation;
            while ((clientEquation = clientInput.readLine()) != null){
                if (clientEquation.equalsIgnoreCase("QUIT")){
                    break;
                }
                System.out.println("Request from client " + clientName + ": " + clientEquation);
            
                try {
                    String[] parts = clientEquation.split(" ");
                    if (parts.length < 3 || parts.length % 2 == 0){
                        return "Error: Bad format. Must write '1 + 1'\n";
                    }
                    double result = Double.parseDouble(parts[0]);

                    if (operator.equals("+")){
                        result = num1 + num2;
                    }
                    else if (operator.equals("-")){
                        result = num1 - num2;
                    }
                    else if (operator.equals("*")){
                        result = num1 * num2;
                    }
                    else if (operator.equals("/")){
                        result = num1 / num2;
                    }
                    else if (operator.equals("%")){
                        result = num1 / num2;
                    }

                    serverOutput.writeBytes("Result: " + result + "\n");
                } catch (Exception e){
                    serverOutput.writeBytes("Error: Bad format. Must write '1 + 1'\n");
                }
            }
            long endTime = System.currentTimeMillis();
            long sessionTime = (endTime - startTime) / 1000;
            System.out.println("LOG: " + clientName + " disconnected. Duration: " + sessionTime + "s");
            socket.close();
        } catch (IOException e){
            System.out.print("Error handling client/");
        }
    }
}

class TCPServer {

  public static void main(String argv[]) throws Exception
    {
      ServerSocket welcomeSocket = new ServerSocket(6789);

      while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            ClientHandler handler = new ClientHandler(connectionSocket);
            Thread client = new Thread(handler);
            client.start();
            
      }
    }
}

