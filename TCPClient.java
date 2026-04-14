// CS 4390 Math Networking Project by Nguyen Do (npd220001) and Jeremiah Boban (jxb220076)
// Client logic

// Import libraries
import java.io.*;
import java.net.*;

class TCPClient {
    public static void main(String argv[]) throws Exception {

        // Create socket connection to server
        Socket socket = new Socket("127.0.0.1", 6789);

        // Setup input/output streams
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());

        // Get client name and send JOIN request
        System.out.print("Enter your name: ");
        String name = userInput.readLine();
        serverOutput.writeBytes("JOIN " + name + "\n");

        // Wait for server acknowledgement
        String response = serverInput.readLine();
        System.out.println("Server: " + response);

        // Send 3 math equations with random delays
        for (int i = 0; i < 3; i++) {

            // Random delay to simulate real client behavior
            Thread.sleep((int)(Math.random() * 3000));

            // Read equation from user
            System.out.print("Enter equation (e.g., 5 + 3): ");
            String eq = userInput.readLine();

            // Send calculation request to server
            serverOutput.writeBytes("CALC " + eq + "\n");

            // Receive result from server
            String result = serverInput.readLine();
            System.out.println("Server: " + result);
        }

        // Send EXIT request to terminate connection
        serverOutput.writeBytes("EXIT\n");

        // Close socket connection
        socket.close();

        System.out.println("Disconnected.");
    }
}