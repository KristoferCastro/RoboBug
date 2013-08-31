package ca.siat.sfu.RoboBugLife.Environment;

import processing.core.PVector;

/**
 * Just a simple struct to represent one lightning stroke
 * @author Kristofer Ken Castro
 * @date 8/3/2013
 */
public class Lightning {
	
	public PVector startPosition;
	public PVector endPosition;
	
	public Lightning(PVector startPosition, PVector endPosition){
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

}
