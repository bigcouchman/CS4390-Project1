import java.io.*;
import java.net.*;
class TCPClient {

    public static void main(String argv[]) throws Exception
    {
        String sentence;
        String modifiedSentence;
        System.out.println("Client is running: " );

        Socket clientSocket = new Socket("127.0.0.1", 6789);

        BufferedReader inFromUser =
          new BufferedReader(new InputStreamReader(System.in));

        BufferedReader inFromServer =
                new BufferedReader(new
                InputStreamReader(clientSocket.getInputStream()));

        DataOutputStream outToServer =
          new DataOutputStream(clientSocket.getOutputStream());
        

        System.out.println("Enter your name: ");
        String name = inFromUser.readLine();
        outToServer.writeBytes(name + '\n');

        String welcome = inFromServer.readLine();
        System.out.println("Server: " + welcome);

        for (int i = 0; i < 3; i++){
          Thread.sleep(1000);

          System.out.print("\nEnter equation: ");
          sentence = inFromUser.readLine();
          
          System.out.println("Sending Equation: " + sentence);
          outToServer.writeBytes(sentence + '\n');

          modifiedSentence = inFromServer.readLine();

          System.out.println("FROM SERVER: " + modifiedSentence);
        }
          System.out.println("Terminating?");
          outToServer.writeBytes("QUIT\n");
          
          clientSocket.close();
          System.out.println("Connection closed");

            

          }
      }
