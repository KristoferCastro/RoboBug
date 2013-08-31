package ca.siat.sfu.RoboBugLife.Collision;

import processing.core.PVector;

/**
 * Collision shape struct used as the hit boxes/circles for the objects in the application.
 * 
 * @author Kristofer Ken CAstro
 * @date 7/22/2013
 */
public class CollisionShape {
	public PVector position; // anchor point of the object containing collision shapes
	public PVector offset;	// how far from position
	public PVector center; // actual center of the circle
	
	public CollisionShape(PVector position, PVector offset){
		this.position = position;
		this.offset = offset;
	}
}
