/*
 * This class essentially connects to the server, and sends messages containing information about
 * executing different Student methods on the server.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class StudentClient extends Thread {
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
			
			int examsTaken = 0;
			while(true){
				
				//Student has 5 functions - cycle through them.
				for(int i = 0; i < 5; i++){
					
					//If client receives the "Done" signal or completed third exam, break the loop
					if(response == RESPONSE || examsTaken == 3){
						out.write("Student,"+255);
						out.newLine();
						out.flush();
						s.close();
						break;
					}
					
					//Write to the server with threadtype and methodNumber
					out.write("Student,"+i);
					out.newLine();
					out.flush();
					
					String temp = in.readLine();
					
					//Get the response from server (Either Done or Continue)
					if(temp != null){
						response = temp;
					}
					
					
				}
				
				//Break out of the while-true if third exam taken or received "Done" sginal
				if(response == RESPONSE || examsTaken == 3){
					System.out.println("EXITING");
					break;
				}
				examsTaken++; //Increment number of exams taken
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
		for (int i=0; i<numStudents; i++)
			new StudentClient().start();
	}
}
