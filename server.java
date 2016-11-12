
package javaapplication23;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author sadievrenseker
 */

public class server
    extends Thread {
    
    public static void main(String args[]){
        new server(1234);
    }

  private void s(String s2) { //an alias to avoid typing so much!
    System.out.println(s2);
  }

    private int port; 

 public server(int listen_port) {
    port = listen_port;
    ServerSocket serversocket = null;
    try {
      
      s("Trying to bind to localhost on port " + Integer.toString(port) + "...");
      
      serversocket = new ServerSocket(port);
    }
    catch (Exception e) { //catch any errors and print errors to gui
      s("\nFatal Error:" + e.getMessage());
      return;
    }
    while (true) {
      s("\nReady, Waiting for requests...\n");
      try {
        Socket connectionsocket = serversocket.accept();
        InetAddress client = connectionsocket.getInetAddress();
        s(client.getHostName() + " connected to server.\n");
        BufferedReader input =
            new BufferedReader(new InputStreamReader(connectionsocket.
            getInputStream()));
        DataOutputStream output =
            new DataOutputStream(connectionsocket.getOutputStream());
        http_handler(input, output);
      }
      catch (Exception e) { 
        s("\nError:" + e.getMessage());
      }

    } 
  }
   private void http_handler(BufferedReader input, DataOutputStream output) {
    int method = 0; //1 get, 2 head, 0 not supported
    String http = new String(); //a bunch of strings to hold
    String path = new String(); //the various things, what http v, what path,
    String file = new String(); //what file
    String user_agent = new String(); //what user_agent
    try {
      //This is the two types of request we can handle
      //GET /index.html HTTP/1.0
      //HEAD /index.html HTTP/1.0
      String tmp = input.readLine(); //read from the stream
      String tmp2 = new String(tmp);
      tmp.toUpperCase(); //convert it to uppercase
      if (tmp.startsWith("GET")) { //compare it is it GET
        method = 1;
      } //if we set it to method 1
      if (tmp.startsWith("HEAD")) { //same here is it HEAD
        method = 2;
      } //set method to 2
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
           output.writeUTF("requested file name :"+path);
          output.writeUTF("hello world");
      
      output.close(); 
    }
    catch (Exception e) {}

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
