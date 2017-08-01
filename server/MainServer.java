/*
 * This class is essentially a docking port for the server. It sets up a ServerSocket and listens
 * for all incoming socket requests. Upon receiving a socket request it creates a new SubServerThread
 * to handle whatever the client connected to the socket wants to do. Eventually, this program
 * will stop listening when its serverSocket is interrupted. At this point, this program will end.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
	
	static ServerSocket serverSocket;
	
	public MainServer ()
	{
		try {
			serverSocket = new ServerSocket(3000);	
			SubServerThread.initiateTableObjets();
			
			while (true)
			{
				
				
				Socket socket = serverSocket.accept();
				System.out.println("Creating Server Thread");
				
				SubServerThread serverThread = new SubServerThread(socket);
				serverThread.start();

			}//while
		}//try
		catch (IOException e)
		{
			System.out.println("Not connected to port");
		}//catch
	}//constructor
	
	public static void main (String [] args)
	{
		new MainServer();
	}//main
}//class
