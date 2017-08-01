
/*
 * This class handles requests from clients that are sent through sockets to the server. Most of
 * the details of how SubServerThread handles different requests are in the comments throughout this
 * class. On a high level, you can think of each SubServerThread as acting as, or copying the functionality
 * of, the Student, Instructor, or Timer thread from the first project. As such, this class also
 * contains inner-classes that contain the code to Student, Instructor, and Timer. Once the SubServerThread
 * handling the Instructor calls ends, it will interrupt the ServerSocket object in MainServer, effectively
 * ending the server.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class SubServerThread extends Thread {
	private static final String STUDENT = "Student";
	private static final String INSTRUCTOR = "Instructor";
	private Socket socket;
	public Student student;
	public Instructor instructor;
	public Timer timer;
	
	private String threadtype;
	
	static int numStudents = 16;
	static int capacity = 12;
	static int numSeats = 3;
	static int seatOrdering = 0; //Keeps track of students who have entered the classroom
	static int studentsTakingExam; //Keeps track of students who are taking the exam
	static int examsAdministered = 0;
	static boolean endRemainingStudents = false;
	
	//A flag to determine when the final exam has been given and graded
	static boolean examsOver = false;
	
	//A queue on which students will line up in when waiting for their test grade, to facilitate FCFS order
	static Queue<WaitingForGrade> waitingForGrade = new LinkedList<WaitingForGrade>();
	
	//Objects that different tasks are synchronized on
	static Object classRoomLine = new Object(); //Students will wait on this object outside of classroom
	static Object letStudentsIn = new Object();
	static Object[] atTable = new Object[(capacity/numSeats)+1];
	static Object handOutExams = new Object();
	static Object examToFinish = new Object();
	static Object readyToGrade = new Object();
	static WaitingForGrade[] waitingForGrades = new WaitingForGrade[numStudents];
	
	static Object idSelection = new Object();
	static Object endRemainingStudentsLock = new Object();
	static int idCounter = 1;
	
	boolean s=false,i=false,t=false;
	
	public static void initiateTableObjets(){
		for(int i = 0; i<=(capacity/numSeats); i++){
			atTable[i] = new Object();
		}
	}
	
	// constructor
	public SubServerThread(Socket socket){
		this.socket = socket;
	}
	
	public void run ()
	{
		
		try {
			//Streams from the socket
			InputStream inputStream = socket.getInputStream(); 
			OutputStream outputStream = socket.getOutputStream();
			
			//Reader and Writer for the socket streams
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
			
			//Get threadtype and methodNumber
			while(!in.ready()){
			}// Wait until BufferedReader is reader
			Scanner sc = new Scanner(in.readLine());
			sc.useDelimiter(",");
			
			
			threadtype = sc.next(); //Teacher, Student, or Timer
			int whichMethod = sc.nextInt(); //Corresponding method that needs to be called
			
			sc.close();
						
			
			//Instantiate object corresponding to thread-type
			//The assumption here is that a client won't change threadtypes during runtime
			//While this does introduce a point of failure, I think it's a reasonable assumption
			//given the circumstances.
			
			//NOTE: This initial iteration is done outside of the while-true loop so we can first
			//determine what kind of thread we're dealing with
			if(threadtype.equals("Student")){
				s = true;
				
				//Synchronize the student creation, as multiple students can be made simultaneously
				synchronized(idSelection){
					waitingForGrades[idCounter-1] = new WaitingForGrade();
					student = new Student(idCounter);
					idCounter++;
				}
			}
			else if(threadtype.equals("Instructor")){
				i = true;
				instructor = new Instructor();
			}
			else {
				t = true;
				timer = new Timer();
			}
			
			
			//Run the appropriate method, and send a message back to the client detailing whether
			//They should continue the sending process or not.
			runMethod(whichMethod);
			if(examsAdministered < 4){
				out.write("CONTINUE");
				out.newLine();
				out.flush();
			}
			else{
				out.write("DONE");
				out.newLine();
				out.flush();
				return;
			}
			
			
			//We introduce a while-true loop here to continue the execution, waiting for another
			//instruction from client. Eventually, we break out of this loop after the last exam
			//is administered
			while(true){
				
				//Get threadtype and methodNumber
				while(!in.ready()){
				}// Wait until BufferedReader is reader
				sc = new Scanner(in.readLine());
				sc.useDelimiter(",");
				
				threadtype = sc.next(); //Teacher, Student, or Timer
				whichMethod = sc.nextInt(); //Corresponding method that needs to be called
				
				sc.close();
				
				//Upon taking three tests, students will send 255 as methodNumber. This is a signal
				//to the server thread to stop and break out of while-true
				if(threadtype.equals("Student") && whichMethod == 255){
					break;
				}
				
				//Determine if thread should break out of while-true loop or not. We do this once before
				//and after the method is run, as some threads were hanging without both checks.
				if(examsAdministered < 4){
					out.write("CONTINUE");
					out.newLine();
					out.flush();
				}
				else{
					out.write("DONE");
					out.newLine();
					out.flush();
					
					//Special case: After all exams administered, instructor has one more method
					//to call - the display-grades-send-students-home method. We call it before
					//letting the instructor end
					if(threadtype.equals("Instructor")){
						while(!in.ready()){
						}
						sc = new Scanner(in.readLine());
						sc.useDelimiter(",");
						
						threadtype = sc.next(); //Teacher, Student, or Timer
						whichMethod = sc.nextInt(); //Corresponding method that needs to be called
						
						sc.close();
						
						runMethod(whichMethod);
					}
					
					break;
				}
										
			
				runMethod(whichMethod);
				
				//Here's the second check, as detailed above.
				if(examsAdministered > 4 ){
					
					out.write("DONE");
					out.newLine();
					out.flush();
					
					//See "Special case:" comment above for details on this block
					if(threadtype.equals("Instructor")){
						while(!in.ready()){
						}// Wait until BufferedReader is reader
						sc = new Scanner(in.readLine());
						sc.useDelimiter(",");
						
						threadtype = sc.next(); //Teacher, Student, or Timer
						whichMethod = sc.nextInt(); //Corresponding method that needs to be called
						
						sc.close();
					}
					
					break;
				}
			}
			
			if(threadtype.equals("Instructor")){
				MainServer.serverSocket.close();
			}
			return;
		}//try
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void runMethod (int methodNumber)
	{
		if (threadtype.equals(STUDENT))
		{
			switch (methodNumber)
			{
				case 0: 
							if(student.waitOutsideRoom() == -1){
								synchronized(endRemainingStudentsLock){
									endRemainingStudents = true;
								}
							}
							break;
				case 1:
							student.waitAtTable();
							break;
				case 2:
							student.takeExam();
							break;	
				case 3:
							student.checkNotes();
							break;			
				case 4:
							student.waitForGrades();
							break;		
				
			}//switch		
		}//if
		
		else if (threadtype.equals(INSTRUCTOR))
		{
			switch (methodNumber)
			{
				case 0: 
							instructor.letStudentsIn();
							break;
				case 1:
							instructor.giveExam();
							break;
				case 2:
							instructor.waitForExamToFinish();
							break;			
				case 3:
							instructor.gradeExams();;
							break;
				
				case 4:
							instructor.sendHome();
							break;
			}//switch
		}//else if
		
		else {
			switch (methodNumber){
				case 0 :
					while(examsAdministered < 4)
						timer.processTimeUnit();
			}
		}
	}//runMethod
	
	//BELOW THIS POINT IS CODE FROM PROJECT 1
	
	private class Student{
		
		Random random = new Random();
		Object table;
		
		int tableNumber;
		int id;
		int examsTaken = 0;
		
		WaitingForGrade waitingForGrade;
		
		public long time = System.currentTimeMillis();
		
		
		public Student(int id){
			this.id = id;
			setName("Student-"+id);
			waitingForGrade =  waitingForGrades[id-1];
		}
		
		public void msg(String m) {
			System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
		}
		
		public int waitOutsideRoom(){
			synchronized(classRoomLine){
				
				if(SubServerThread.examsOver) return -1; //To catch students who arrive to school after exams ended
				
				while(true){ //Wait to be notified not interrupted
					try {
						msg(" is waiting outside classroom");
						classRoomLine.wait(); //Wait to be let into classroom
						
						//Only enter if the classroom hasn't reached its capacity. Else wait again
						if(SubServerThread.seatOrdering < SubServerThread.capacity){
						
							//Assign a student to a table depending on when they entered the room
							table = atTable[(SubServerThread.seatOrdering/numSeats) ];
							tableNumber = (SubServerThread.seatOrdering/numSeats);
							SubServerThread.seatOrdering++;
							break;
						}
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
			
			if(SubServerThread.examsOver) return -1; //To catch any students who were waiting outside the classroom when the instructor goes home
			else return 0;
		}
		
		public void waitAtTable(){
			synchronized(table){
				try {
					table.wait(); //Wait for the exam on their current table
					msg(" is starting the exam at table "+tableNumber);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void takeExam(){
			synchronized(examToFinish){
				try {
					examToFinish.wait(); //Wait for the exam to finish / simulates student taking exam
					msg(" finished the exam");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void checkNotes(){
			//Check over notes
			try {
				sleep(random.nextInt(333)); //333ms is equivalent to 5 units of time for the given time units
				msg("is submitting the exam");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			//Each student gets on waitingForGrade object. Add it to a queue to preserve the FCFS order of graded exam return.
			SubServerThread.waitingForGrade.add(waitingForGrade);
		}
		
		public void waitForGrades(){
			//When the last student finishes checking notes, they signal the instructor to start grading
			if(SubServerThread.waitingForGrade.size() == SubServerThread.studentsTakingExam){
				synchronized(readyToGrade){
						readyToGrade.notify();
				}
					
			}
			
			synchronized(waitingForGrade){
				try {
					waitingForGrade.wait(); //Wait to receive a grade
					examsTaken++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	private class Instructor{
		
		Random random = new Random();
		
		public long time = System.currentTimeMillis();
		
		
		Instructor(){
			setName("Instructor-0");
		}
		
		public void msg(String m) {
			System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
		}
		
		public void letStudentsIn(){
			synchronized(letStudentsIn){
				
				fifteenMinutesBefore();
				msg("Exam number: " + SubServerThread.examsAdministered);
			}
			synchronized(classRoomLine){
				classRoomLine.notifyAll(); //Signal students to let them know they can enter classroom
			}
		}
		
		//Used to let students into classroom 15 minutes early
		public synchronized void fifteenMinutesBefore(){
			try {
				msg("WAITING TO BE NOTIFIED");
				letStudentsIn.wait(); //Wait till it's time to let students in (will be signaled by timer)
				msg("JUST NOTIFIED");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//Give the exam to the students
		public void giveExam(){
			synchronized(handOutExams){
				try {
					handOutExams.wait(); //Wait until it's time for the exam to begin
					SubServerThread.studentsTakingExam = SubServerThread.seatOrdering; //Keeps track of how many students are taking current exam
					SubServerThread.seatOrdering = 0; //Resets count of students in Main for the next exam
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//Signal each table, informing the students at that table that they can begin the exam.
			for(Object table : atTable){
				synchronized(table){
					table.notifyAll();
				}
			}
		}
		
		//Wait for exam to finish, and wait until the last student is done checking their notes to begin grading
		public void waitForExamToFinish(){
			synchronized(examToFinish){
				try {
					examToFinish.wait(); //Wait for the exam to finish
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//In case no students made it in time to the exam, prevent deadlock.
			if(SubServerThread.studentsTakingExam != 0){
				synchronized(readyToGrade){
					try {
						readyToGrade.wait(); //Wait for students to finish looking over their notes
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//Grade the exams
		public void gradeExams(){
			while(!SubServerThread.waitingForGrade.isEmpty()){
				WaitingForGrade grade = SubServerThread.waitingForGrade.poll(); //Grade paper. Queue is used to ensure FCFS order of when students finished looking over notes.
				synchronized(grade){
					try {
						sleep(random.nextInt(133)); //133ms is equivalent to 2 units of time for the given time units
						grade.setGrade(random.nextInt(100)+1); //Student gets assigned grade. Can't be assigned 0 if they took the exam.
						grade.notify();
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			SubServerThread.examsAdministered++;
		}
		
		public void sendHome(){
			examsOver = true;
			synchronized(classRoomLine){
				classRoomLine.notifyAll();
			}
			
			msg("is leaving.");
			
			for(int i = 0; i < waitingForGrades.length; i++){
				System.out.print("Student_"+i+" received scores of: ");
				for(int j = 0; j< waitingForGrades[i].grades.length; j++){
					if(j == waitingForGrades[i].grades.length -1)
						System.out.print(waitingForGrades[i].getGrade(j));
					else
						System.out.print(waitingForGrades[i].getGrade(j)+ ", ");
				}
				System.out.println("");
			}
		}
		
	}
	
	public class Timer{
		
		int everyTwoHours = 0;
		int everyHour;
		int timeUnit = 0;
		
		
		public int getTime(){
			return timeUnit;
		}
		
		//Method that's used to advance the current time in increments of 15 minutes
		public void processTimeUnit(){
			//System.out.println("CHECKING NOW");
			try {
				//15 minutes is equivalent to 1000 ms
				sleep(1000);
				timeUnit++;
				if(timeUnit > 0 && timeUnit%7 == 0){ //Every 1:45 hours, let students into classroom
					synchronized(letStudentsIn){
						//System.out.println("NOTIFYING NOW");
						letStudentsIn.notify();
					}
				}
				else if(timeUnit > 0 && timeUnit%8 == 0){ //Every 2:00 hours, begin exam
					synchronized(handOutExams){
						handOutExams.notify();
					}
				}
				else if(timeUnit > 0 && (timeUnit+4) % 8 == 0){ //Every 2:00 hours after the first hour, end an exam
					synchronized(examToFinish){
						examToFinish.notifyAll();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
