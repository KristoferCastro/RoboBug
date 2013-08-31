package ca.siat.sfu.RoboBugLife.Collision;

import processing.core.PVector;

public class CollisionCircle extends CollisionShape {

	public float radius;
	public CollisionCircle(PVector position, PVector offset, float radius) {
		super(position, offset);
		this.radius = radius;
		center = new PVector(position.x + offset.x, position.y + offset.y);
		// TODO Auto-generated constructor stub
	}
	
	public void addToCenter(PVector vector){
		center.add(vector);
	}

	private void updateCenterX(){
		center.x = position.x + offset.x;
	}
	
	private void updateCenterY(){
		center.y = position.y + offset.y;
	}
	
	public void changeX(float x){
		position.x = x;
		updateCenterX();
	}
	
	public void changeY(float y){
		position.y = y;
		updateCenterY();
	}
}
