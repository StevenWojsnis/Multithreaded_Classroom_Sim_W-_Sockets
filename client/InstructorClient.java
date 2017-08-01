/*
 * This class essentially connects to the server, and sends messages containing information about
 * executing different Instructor methods on the server.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class InstructorClient extends Thread {
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
				//Instructor has 4 functions - cycle through them.
				for(int i = 0; i < 4; i++){
					
					//If client receives the "Done" signal, break the loop
					if(response.equals(RESPONSE))
						break;
					
					//Write to the server with threadtype and methodNumber
					out.write("Instructor,"+i);
					out.newLine();
					out.flush();
					
					String temp = in.readLine();
					
					//Get the response from server (Either Done or Continue)
					if(temp != null)
						response = temp;
					
				}
				
				//Break out of while-true if response is done
				if(response.equals(RESPONSE))
					break;
			}
			
			//Special case for instructors - perform last function to print grades
			out.write("Instructor,"+4);
			out.newLine();
			out.flush();
			
			out.close();
			s.close();
			
		}
		
		catch (Exception e)
		{
		}
		
	}
	
	public static void main (String [] args)
	{
		new InstructorClient().start();
	}
}