# Multithreaded_Classroom_Sim_W-_Sockets

<b> This is a re-implementation of a project so that it works with sockets to execute on separate client and server machines.
For details on how the underlying project works, see the ReadMe at https://github.com/StevenWojsnis/Multithreaded_Classroom_Sim/tree/master </b>

For details on the server-client socket implementation, see the Explanation_Execution_ReadMe.txt file.

A short explanation is as follows: Synchronizes instructor, student, and timer threads (Project story-line imitates a classroom exam
scenario). Allows the threads to be run from separate machines, communicating with eachother via a central server. The server uses
sockets to communicate with clients, and delegates all underlying work (business logic for this project) to a "subserver thread" which
runs in the background of the server machine.

The code is well documented and should be viewed for further explanation of the client-server communication.
