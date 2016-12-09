import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Requester implements Runnable{
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
			System.out.println("connected to server, expecting new port number");

			String line = in.readLine();
			System.out.println(line);

			String[] parsed = line.split(":\\s+");
			int newPort = Integer.parseInt(parsed[1]);
			requestSocket = new Socket("localhost", newPort);
			System.out.println("now connected to localhost in port " + newPort);

			out = new PrintWriter(requestSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));

			String fromUser = "GET /try.html HTTP/1.1";
			if (fromUser != null) {
				System.out.println("Client sends: " + fromUser);
				out.println(fromUser);
			}
			// 
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}

			in.close();
			out.close();
		} catch(IOException e) {
			System.err.println("data received in unknown format");
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg) {
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("client>" + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		if (args.length == 0) {
			Requester client = new Requester();
			client.run();
		} else {
			int numberOfClients = Integer.valueOf(args[0]);
			for (int i = 0; i < numberOfClients; i++) {
				new Thread(new Requester()).start();
			}
		}
	}
}
