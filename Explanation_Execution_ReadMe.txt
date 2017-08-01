############################
# CS344 Project 2 ReadMe   #
# Steven Wojsnis	   # 	
############################

Brief overview:

The general idea of my project is that I have three client programs that send signals in an iterative fashion
to a server. The server then creates a serverthread for each socket that connects to it and execute the functions
corresponding to the messages received from the client programs. The code for Student, Instructor, and Timer are
contained within three separate inner classes of the SubServerThread class. This allows the SubServerThread to
access the needed functions, while still maintaining the thread-specific instructions within our Student, Instructor
and Timer classes, like sleep() etc. In other words, this program doesn't create student, etc., threads on the server
but spawns SubServerThreads that, for all intents and purposes, acts as either a Student, Instructor, or Timer thread.


#############
# Execution #
#############

The code is set up so that the clients try to connect to the hawk.cs.qc.edu server at port 3000. Similarly, the
server listens on port 3000.

Each client is its own program with its own main method. Though this could have been avoided by just turning them
into threads, I thought this better reflected the story. As such, all three files, StudentClient, InstructorClient,
and TimerClient must be run separately. It is not necessary that these be run on the same machine.

The server is comprised of three classes: MainServer.java, SubServerThread.java, and WaitingForGrade.java.
These three classes should be compiled and ran on the same machine.

So, once all the server files are on the same machine, you must compile it with:

		javac MainServer.java

and, similarly, run it with:

		java MainServer

Once the server is running, you can then run each client program with the following commands (assuming they're
already compiled):
		
		java StudentClient
		java InstructorClient
		java TimerClient

(NOTE: The server must be running before running these three client programs. Also, it's advisable to start the
Student and Instructor programs before Timer)


It should be noted that this project had to be tested on a singular server (hawk.cs.qc.edu) as it seems to be
the only server that's up and running the latest version of Java. (Apparently crow.cs.qc.edu was also supposed
to be running the latest version of Java, but I wasn't able to connect to it).