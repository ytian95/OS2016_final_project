import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;

class ClientServer implements Runnable {
	private ServerSocket serverSocket;
	private DataOutputStream output;	// initially set to 1234
	private static Semaphore reader = new Semaphore(1);

	public ClientServer(ServerSocket serverSocket, DataOutputStream output) {
		this.serverSocket = serverSocket;
		this.output = output;
	}

	public void run() {
		//Two types of request we can handle:
		//GET /index.html HTTP/1.0
		//HEAD /index.html HTTP/1.0
		
		// Reads the request we are making. Gets what file we want to connect to
		BufferedReader newSocketInput = null;
		DataOutputStream newSocketOutput = null;

		try {
			Socket connectionSocket = serverSocket.accept();
			InetAddress client = connectionSocket.getInetAddress();
			System.out.println(client.getHostName()+" connected to server.\n");
			
			newSocketInput = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			newSocketOutput = new DataOutputStream(connectionSocket.getOutputStream());
			writeToOutput(newSocketInput, newSocketOutput);
			
			// closes connection to 1234. We are now only running on new port #
			output.close();
			output = newSocketOutput;
			
			// wait to close new connection
			Thread.sleep(1000);
			output.close();
		} catch(IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void writeToOutput(BufferedReader newSocketInput, DataOutputStream newSocketOutput) 
		throws IOException, InterruptedException {
		String[] request = newSocketInput.readLine().split(" ");
		String path = request[1].substring(1);
		newSocketOutput.writeBytes(constructHttpHeader(200, 5));

		// Block with another semaphore for later when have writer
		// Reading the file from the given path
		try {
			reader.acquire();
			System.out.println("Lock acquired");
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			System.out.println("path opening: " + path);
			String line="";
			while ((line=br.readLine())!=null) {
				newSocketOutput.writeUTF(line);
				System.out.println("line: "+line);
			}
			newSocketOutput.writeUTF("requested file name :"+path);
			br.close();
		} finally {
			reader.release();
			System.out.println("Lock released");
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
	// Port of the listening server 1234
	private int port;
	private static Set<Integer> ports = Collections.synchronizedSet(new HashSet<Integer>());

	public Server(int listen_port) {
		port = listen_port;
	}

	// Temporarily suppressing warning until building admin cln 
	@SuppressWarnings("resource")
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

	private void httpHandler(BufferedReader input, DataOutputStream output) {
		try {
			ServerSocket serverSocket = handshaking(output);
			// make and run new thread to talk to client
			new Thread(new ClientServer(serverSocket, output)).start();
		}
		catch (IOException e) {
			e.printStackTrace();
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

		return new ServerSocket(newPort);
	}

	public static void main(String args[]){
	    boolean run = false;
	    
	    while (true) {
		Thread t = new Thread(new Server(1234));
		while (!run) {
		    String input = System.console().readLine();
		    if (input.equals("start")) {
			System.out.println("Starting server");
			run = true;
			t.start();
		    } else {
			System.out.println("Invalid input. To start server, enter 'start'");
		    }
		}
		
		while (run) {
		    String input = System.console().readLine();
		    if (input.equals("stop")) {
			System.out.println("Stopping server");
			t.interrupt();
			run = false;
		    } else {
			System.out.println("Invalid input. To stop server, enter 'stop'");
		    }
		}
	    }
	}
} 		
