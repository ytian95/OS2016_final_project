import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private int port;
    
    public static void main(String args[]){
        new Server(1234);
    }

    public Server(int listen_port) {
	port = listen_port;
	ServerSocket serversocket = null; 
	try {
	    System.out.println("Trying to bind to localhost on port " + Integer.toString(port) + "...");
	    serversocket = new ServerSocket(port);
	} catch (Exception e) { //catch any errors and print errors to gui
	    System.out.println("\nFatal Error:" + e.getMessage());
	    return;
	}
	while (true) {
	    System.out.println("\nReady, Waiting for requests...\n");
	    try {
		Socket connectionsocket = serversocket.accept();
		InetAddress client = connectionsocket.getInetAddress();
		System.out.println(client.getHostName() + " connected to server.\n");
		BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
		DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
		http_handler(input, output);
	    } catch (Exception e) { 
		System.out.println("\nError:" + e.getMessage());
	    }
	} 
    }
    private void http_handler(BufferedReader input, DataOutputStream output) {
	//Two types of request we can handle:
	//GET /index.html HTTP/1.0
	//HEAD /index.html HTTP/1.0
	int method = 0; //1 get, 2 head, 0 not supported
	String http = new String(); 
	String path = new String(); 
	String file = new String(); 
	String user_agent = new String();
	try {
	    String tmp = input.readLine(); 
	    System.out.println("read: "+tmp);
	    String tmp2 = new String(tmp);
	    tmp.toUpperCase(); 
	    if (tmp.startsWith("GET")) {
		method = 1;
	    } 
	    if (tmp.startsWith("HEAD")) {
		method = 2;
	    }
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

}