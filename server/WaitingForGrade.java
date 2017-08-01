

//Note that this is a custom object that students will wait on while waiting for their
//exams to be graded. The reason for using this instead of a generic Object is to implement
//the "printing grades" feature at the end of the day.
public class WaitingForGrade {
	int[] grades; //Used to store a student's grades
	
	public WaitingForGrade(){
		grades = new int[4];
	}
	
	public int getGrade(int i){
		return grades[i];
	}
	
	public void setGrade(int grade){
		grades[SubServerThread.examsAdministered] = grade;
	}
}
