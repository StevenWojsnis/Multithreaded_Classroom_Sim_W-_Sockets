/*
 * This class essentially connects to the server, and sends messages containing information about
 * executing different Timer methods on the server.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TimerClient extends Thread {
	static int numStudents = 16;
	public void run ()
	{
		try 
		{
					
			final String RESPONSE = "DONE";
			String response = "";
			
			//Connect to server
			//Socket s = new Socket("hawk.cs.qc.edu", 3000);
			Socket s = new Socket("localhost", 3000);
			
			//Get writers and readers for socket streams
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
		
			while(true){
				
				//Timer only has one method, so just send "Timer,0" every time
				out.write("Timer,"+0);
				out.newLine();
				out.flush();
				
				String temp = in.readLine();
				
				//Get the response from server (Either Done or Continue)
				if(temp != null)
					response = temp;
				
				//If we get the "Done" signal from the server, break the loop
				if(response == RESPONSE)
					break;
			}
			
			out.close();
			s.close();
			
		}
		
		catch (Exception e)
		{
		}
		
	}
	
	public static void main (String [] args)
	{
		new TimerClient().start();
	}
}
