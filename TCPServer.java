import java.io.*;
import java.net.*;
import java.util.Date;

class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());

            String clientName = clientInput.readLine();
            System.out.println("Client " + clientName + " connected.");
            serverOutput.writeBytes("Welcome client " + clientName + "\n");

            String clientEquation;
            while ((clientEquation = clientInput.readLine()) != null){
                if (clientEquation.equalsIgnoreCase("Exit")){
                    break;
                }
                System.out.println("Request from client " + clientName + ": " + clientEquation);
            
                try {
                    String[] parts = clientEquation.split(" ");
                    double num1 = Double.parseDouble(parts[0]);
                    double num2 = Double.parseDouble(parts[2]);
                    String operator = parts[1];
                    double result = 0;

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

                    serverOutput.writeBytes("Result: " + result + "\n");
                } catch (Exception e){
                    serverOutput.writeBytes("Error: Bad format. Must write '1 + 1'\n");
                }
            }

            System.out.println("LOG: " + clientName + " disconnected.");
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
            welcomeSocket.close();
      }
    }
}

