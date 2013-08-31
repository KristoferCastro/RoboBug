package ca.siat.sfu.RoboBugLife.User;

/**
 * Singleton class that keep tracks of user statistics such as resources.
 * @author Kristofer Ken Castro
 * @date 8/5/2013
 */
public class User {

	private static User manager = new User();
	
	public int resources;
	
	private User(){
		resources = 0;
	}
	
	public static User getInstance(){
		return manager;
	}
	
}
