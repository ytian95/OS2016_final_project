/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author sadievrenseker
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Requester{
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    Requester(){}
    void run()
    {

        //1. creating a socket to connect to the server
        try{
            requestSocket = new Socket("localhost", 1234);
            System.out.println("Connected to localhost in port 1234");

            PrintWriter out = new PrintWriter(requestSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(requestSocket.getInputStream()));
            Scanner stdIn = new Scanner(System.in);
            String fromUser = "GET /try.html HTTP/1.1";
            if (fromUser != null) {
                System.out.println("Client: " + fromUser);
                out.println(fromUser);
            }
            String line;
            while((line =in.readLine())!=null){
                System.out.println(line);
            }

            // System.out.println("sending :GET /try.html HTTP/1.0 ");
                  /*  sendMessage("GET /try.html HTTP/1.1");
                     message = (String)in.readObject();
                    System.out.println("server>" + message);
                    message = "bye";
                    sendMessage(message);*/
            stdIn.close();
        }
        catch(Exception e){
            System.err.println("data received in unknown format");
            e.printStackTrace();
        }
    }
    void sendMessage(String msg)
    {
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("client>" + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    public static void main(String args[])
    {
        Requester client = new Requester();
        client.run();
    }
}
