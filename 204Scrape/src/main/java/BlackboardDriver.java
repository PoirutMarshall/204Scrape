import java.io.IOException;
import java.util.Scanner;

public class BlackboardDriver implements BlackboardVars{
	public static void main(String[] args) {
		final String USERNAME = "username";
		final String PASSWORD = "password";
		
		//don't forget "\\" at the end of destination
		final String DESTINATION = "C:\\Users\\user\\Desktop\\Blackboard\\";

		
		BlackboardClient client = new BlackboardClient();
		
		//mymc login
		client.login(USERNAME, PASSWORD);
		
		client.download(WEEK1, DESTINATION);
		client.download(WEEK2, DESTINATION);
		client.download(WEEK3, DESTINATION);
		client.download(WEEK4, DESTINATION);
		client.download(WEEK5, DESTINATION);
		client.download(WEEK6, DESTINATION);
		client.download(WEEK7, DESTINATION);
		client.download(WEEK8, DESTINATION);
		client.download(WEEK9, DESTINATION);
		client.download(WEEK10, DESTINATION);
		client.download(WEEK11, DESTINATION);
		client.download(WEEK12, DESTINATION);
		client.download(WEEK13, DESTINATION);
		client.download(WEEK14, DESTINATION);
		
		try {
			client.quit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
