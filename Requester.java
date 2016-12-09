import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Requester{
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    int port = 1234;
    
    public void run() {
	//1. creating a socket to connect to the server
        try {
            requestSocket = new Socket("localhost", port);
            System.out.println("Connected to localhost in port" + port);
            PrintWriter out = new PrintWriter(requestSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
            Scanner stdIn = new Scanner(System.in);
            
            System.out.println("connected to server, expecting new port number");
            
            String line="some predefined msg";
            line = in.readLine();
            System.out.println(line);
            
            int newPort = Integer.parseInt(line.substring(line.indexOf(":")+2));
            
            requestSocket = new Socket("localhost", newPort);
            System.out.println("Connected to localhost in port " + newPort);
            
            out = new PrintWriter(requestSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
            
            String fromUser = "GET /try.html HTTP/1.1";
            if (fromUser != null) {
                System.out.println("Client: " + fromUser);
                out.println(fromUser);
            }
            while ((line = in.readLine()) !=null) {
                System.out.println(line);
            }
            stdIn.close();


        } catch(Exception e){
            System.err.println("data received in unknown format");
            e.printStackTrace();
        }
    }
    
    public void sendMessage(String msg) {
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("client>" + msg);
        } catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        Requester client = new Requester();
        client.run();
    }
}
