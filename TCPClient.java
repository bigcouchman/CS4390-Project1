// CS 4390 Math Networking Project by Nguyen Do (npd220001) Client logic

// Import libraries
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
class TCPClient {

  // Handling clients connecting with server
    public static void main(String argv[]) throws Exception
    {
      // Track client variables and its inputs
        String sentence;
        String modifiedSentence;
        System.out.println("Client is running: " );

        long startTime = System.currentTimeMillis();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String arriveTime = LocalDateTime.now().format(dateFormat);
        
        // Multiple clients can connect with the server, create a client and track their join times and dates
        try (Socket clientSocket = new Socket("127.0.0.1", 6789)){
          BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
          BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
          
          System.out.println("A client joined at: " + arriveTime);
          System.out.print("Enter your name: ");
          String name = inFromUser.readLine();
          outToServer.writeBytes(name + '\n');

          String welcome = inFromServer.readLine();
          System.out.println("Server: " + welcome);

          // Clients can type 3 equations and then be asked if they want to type 3 more
          boolean continuing = true;
          while (continuing){
            for (int i = 0; i < 3; i++){
              Thread.sleep(1000);
              
              // Equations are sent to the server to process, and sent back to the client with the solved result
              System.out.print("\nEnter equation (with spaces): ");
              sentence = inFromUser.readLine();
              System.out.println("Sending Equation: " + sentence);
              outToServer.writeBytes(sentence + '\n');
              modifiedSentence = inFromServer.readLine();
              System.out.println("FROM SERVER: " + modifiedSentence);
            }
            // Check if user wants to type in 3 more equations
            System.out.println("\nDo you like to stay for 3 more equations? (YES/NO): ");
            String opt = inFromUser.readLine();
            if (!opt.equalsIgnoreCase("YES") || opt.equalsIgnoreCase("NO")){
              continuing = false;
            }
            else {
              continuing = true;
            }
          }

          // When the client completes, terminate its connection and close the client
            System.out.println("Terminating?");
            outToServer.writeBytes("QUIT\n");
          
            clientSocket.close();
            // Track leaving times for clients
            String leaveTime = LocalDateTime.now().format(dateFormat);
            long endTime = System.currentTimeMillis();
            long sessionTime = (endTime - startTime) / 1000;
            System.out.println("Connection closed at " + leaveTime + ". Duration: " + sessionTime);
          // Error handling
          } catch (ConnectException e) {
            System.err.println("Error: TCPServer is not connected properly.");
          } catch (IOException e){
            System.err.println("Error: Connection lost: " + e.getMessage());
          }
        }
      }
