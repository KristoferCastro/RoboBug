package ca.siat.sfu.RoboBugLife.Collision;

import processing.core.PVector;

public class CollisionRect extends CollisionShape{

	public float width;
	public float height;
	
	public CollisionRect(PVector position, PVector offset, float width, float height) {
		super(position, offset);
		this.width = width;
		this.height = height;
	}

}
