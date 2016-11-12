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

public class Server implements Runnable {
    private int port;
    Socket connectionSocket;

    public Server(Socket socket) {
	port = socket.getLocalPort();
	System.out.println("Port:" + port);
	connectionSocket = socket;
    }
    
    public void run() {
	try {
	    BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
	    DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
	    http_handler(input, output);
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	}
    }
 
    private void http_handler(BufferedReader input, DataOutputStream output) {
	//Two types of request we can handle:
	//GET /index.html HTTP/1.0
	//HEAD /index.html HTTP/1.0
	String path = new String(); 
	try {
	    String tmp = input.readLine(); 
	    System.out.println("read: "+tmp);
	    String tmp2 = new String(tmp);
	    tmp.toUpperCase(); 
	    int start = 0;
	    int end = 0;
	    for (int a = 0; a < tmp2.length(); a++) {
		if (tmp2.charAt(a) == ' ' && start != 0) {
		    end = a;
		    break;
		}
		if (tmp2.charAt(a) == ' ' && start == 0) {
		    start = a;
		}
	    }
	    path = tmp2.substring(start + 2, end); //fill in the path
	    output.writeBytes(construct_http_header(200, 5));
	    BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	    System.out.println("openning file"+path);
	    String line="";
	    while((line=br.readLine())!=null){
		output.writeUTF(line);
		System.out.println("line: "+line);
	    }
	    output.writeUTF("requested file name :"+path);
	    output.writeUTF("hello world");
	    output.close(); 
	    br.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private String construct_http_header(int return_code, int file_type) {
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

    public static void main(String args[]){
	ServerSocket serverSocket;
	try {
	    System.out.println("Trying to bind to localhost on port 1234...");
	    serverSocket = new ServerSocket(1234);
        ports.add(1234);
	    System.out.println("Listening");
	} catch(Exception e) { //catch any errors and print errors to gui
	    System.out.println("\nFatal Error:" + e.getMessage());
	    return;
	}
	while (true) {
	    System.out.println("\nReady, Waiting for requests...\n");
	    try {
            Socket socket = serverSocket.accept();
            System.out.println("Connected");
            
            Random r = new Random();
            int newPort = r.nextInt() % 10000 + 40000;
            while (ports.contain(newPort)){
                newPort = r.nextInt() % 10000 + 40000;
            }
            ports.add(newPort);
            System.out.println("random port: " + newPort);
            
            new Thread(new Server(new serverSocket(newPort).accept())).start();
                        
            InetAddress client = socket.getInetAddress();
            System.out.println(client.getHostName() + " connected to server.\n");
	    } catch (Exception e) { 
            System.out.println("\nError:" + e.getMessage());
	    }
	}
    }
} 