package ca.siat.sfu.RoboBugLife.RoboBug;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Abstract factory class for RoboBugs
 * @author Kristofer Ken Castro
 * @date 8/3/2013
 */
public abstract class RoboBugFactory {
	PApplet p;
	
	public  RoboBugFactory(PApplet p){
		this.p = p;
	}
	
	public abstract RoboBug createRoboBug(String type);
	public abstract RoboBug createRoboBug(String type, PVector position);
}
