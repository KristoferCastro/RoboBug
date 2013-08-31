package ca.siat.sfu.RoboBugLife.Environment;

import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import ca.siat.sfu.RoboBugLife.Collision.CollisionCircle;
import ca.siat.sfu.RoboBugLife.Collision.CollisionShape;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawable;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawableWithImage;
import ca.siat.sfu.RoboBugLife.Utility.Layer;

/**
 * 
 * Resource that the bug uses to collect.
 * 
 * @author Kristofer Ken Castro
 * @date 7/23/2013
 *
 */
public class Resource implements IDrawableWithImage{

	private PImage image;
	private final String IMAGE_PATH = "ca/siat/sfu/RoboBugLife/resources/resource.png";
	public PVector position;
	private PApplet p;
	private HashMap<String, CollisionShape> collisionShapes;
	public Layer layer; // determine which layer it is back middle or front.
	private float scaleFactor;
	public static final float BACK_LAND_Y = Land.groundPosition-15;
	public static final float MIDDLE_LAND_Y = Land.groundPosition+5;
	public static final float FRONT_LAND_Y = Land.groundPosition+20;

	public Resource(PApplet p, PVector position, Layer layer){
		this.p = p;
		image = p.loadImage(IMAGE_PATH);
		this.position = position;
		collisionShapes = new HashMap<String, CollisionShape>();
		scaleFactor = 1;
		this.layer = layer;
		initializeCollisionShape();
	}
	
	private void initializeCollisionShape(){
		float bodySize = 40*scaleFactor;
		CollisionCircle bodyCollision = new CollisionCircle(position, new PVector(0,0), bodySize);
		collisionShapes.put("Body", bodyCollision);
	}
	
	public void draw(){
		p.pushMatrix();
		p.translate(position.x, position.y);
		drawImage();
		p.popMatrix();
		//drawCollisionShape();
	}
	
	private void drawCollisionShape(){
		p.fill(255,0,0,50);
		p.fill(0,50);
		for (CollisionShape shape : collisionShapes.values()){
			CollisionCircle circle = (CollisionCircle) shape;
			p.ellipse(circle.center.x, circle.center.y, circle.radius, circle.radius);
		}
	}
	@Override
	public void drawImage() {
		p.imageMode(p.CENTER);
		p.image(image, 0, 0);
		p.imageMode(p.CORNERS);
		
	}

	public HashMap<String, CollisionShape> getCollisionShapes() {
		return collisionShapes;
	}

	

}
