import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;

class ClientServer implements Runnable {
    private ServerSocket serverSocket;
    private DataOutputStream output;
    private static Semaphore reader = new Semaphore(1);
    
    public ClientServer(ServerSocket serverSocket, DataOutputStream output) {
        this.serverSocket = serverSocket;
        this.output = output;
    }
    
    public void run() {
        //Two types of request we can handle:
	//GET /index.html HTTP/1.0
	//HEAD /index.html HTTP/1.0
	String path = "";
        
        BufferedReader input2 = null;
        DataOutputStream output2 = null;
        
        try {
            Socket connectionSocket = serverSocket.accept();
            InetAddress client = connectionSocket.getInetAddress();
            System.out.println(client.getHostName()+" connected to server.\n");
            input2 = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            output2 = new DataOutputStream(connectionSocket.getOutputStream());
            String[] request = input2.readLine().split(" ");
			
            path = request[1].substring(1);
            output2.writeBytes(constructHttpHeader(200, 5));
        
            // // Block with semaphore for later when have writer
            
	    try {
		reader.acquire();
		System.out.println("Lock acquired");
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		System.out.println("path opening: " + path);
		String line="";
		while ((line=br.readLine())!=null) {
		    output2.writeUTF(line);
		    System.out.println("line: "+line);
		}
		output2.writeUTF("requested file name :"+path);
		br.close();
	    } catch (InterruptedException e) {
		System.out.println(e.getMessage());
            } finally {
		reader.release();
		System.out.println("Lock released");
	    }
            output.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private String constructHttpHeader(int return_code, int file_type) {
        String s = "HTTP/1.0 ";
        switch (return_code) {
            case 200:
                s = s + "200 OK";
                break;
            case 400:
                s = s + "400 Bad Request";
                break;
            case 403:
                s = s + "403 Forbidden";
                break;
            case 404:
                s = s + "404 Not Found";
                break;
            case 500:
                s = s + "500 Internal Server Error";
                break;
            case 501:
                s = s + "501 Not Implemented";
                break;
            default:
                s = s + "You've reached the error page for the error page! You win!";
                break;
        }

        s = s + "\r\n"; 
        s = s + "Connection: close\r\n"; 
        s = s + "Server: SmithOperatingSystemsCourse v0\r\n"; //server name

        switch (file_type) {
            case 0:
                break;
            case 1:
                s = s + "Content-Type: image/jpeg\r\n";
                break;
            case 2:
                s = s + "Content-Type: image/gif\r\n";
            case 3:
                s = s + "Content-Type: application/x-zip-compressed\r\n";
            default:
                s = s + "Content-Type: text/html\r\n";
                break;
        }
        s = s + "\r\n"; 
        return s;
    }
}

public class Server implements Runnable {
    private int port;

    private static Set<Integer> ports = Collections.synchronizedSet(new HashSet<Integer>());
    
    private static Semaphore reader = new Semaphore(1);

    public Server(int listen_port) {
        port = listen_port;
//        System.out.println("Port:" + port);
    }
    
    public void run() {
        ServerSocket serverSocket = null;
        
        try {
            System.out.println("Trying to bind to localhost on port " + port);
            serverSocket = new ServerSocket(port);
        } catch(IOException e) {
            System.out.println("\nFatal Error: " + e.getMessage());
            return;
        }
        while (true) {
            System.out.println("\nReady, Waiting for requests...\n");
            try {
                Socket connectionSocket = serverSocket.accept();
                InetAddress client = connectionSocket.getInetAddress();
                System.out.println(client.getHostName() + " connected to server.\n");

                BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
                httpHandler(input, output);
                
            } catch (Exception e) { 
                System.out.println("\nError:" + e.getMessage());
            }
        }
    }
 
    private ServerSocket handshaking(DataOutputStream output) throws IOException {
        Random r = new Random();
        int newPort = r.nextInt() % 10000 + 40000;
        while (ports.contains(newPort)){
            newPort = r.nextInt() % 10000 + 40000;
        }
        ports.add(newPort);
        System.out.println("random port: " + newPort);
        
        output.writeUTF("new port number: " + newPort);
        output.flush();
        output.close();
        System.out.println("random port sent to client: " + newPort);
        
        ServerSocket serverSocket = new ServerSocket(newPort);
        return serverSocket;
    }
    
    private void httpHandler(BufferedReader input, DataOutputStream output) {
		try {
			ServerSocket serverSocket = handshaking(output);
            // make and run new listening thread
            new Thread(new ClientServer(serverSocket, output)).start();
		}
		catch (IOException e) {//| InterruptedException e) {
			e.printStackTrace();
		}
	}

    public static void main(String args[]){
	   new Thread(new Server(1234)).start();	
    }
} 
