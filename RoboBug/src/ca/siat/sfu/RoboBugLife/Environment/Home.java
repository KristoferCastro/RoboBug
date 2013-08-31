package ca.siat.sfu.RoboBugLife.Environment;

import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import ca.siat.sfu.RoboBugLife.Collision.CollisionCircle;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawableWithImage;

/**
 * The nest for where the bugs return to
 * @author Kristofer Ken Castro
 * @date 8/5/2013
 */
public class Home implements IDrawableWithImage{

	private PVector position;
	private PApplet p;
	private HashMap<String, CollisionCircle> collisionShapes;
	private PImage image;
	private final String IMAGE_PATH = "/ca/siat/sfu/RoboBugLife/resources/home.png";
	
	public Home(PApplet p, PVector position){
		this.position = position;
		this.p = p;
		collisionShapes = new HashMap<String, CollisionCircle>();
		initializeCollisionShapes();
		
		try{
			image = p.loadImage(IMAGE_PATH);
		}catch(NullPointerException e){
			System.out.println("Something wrong witht the home base image in Home.java");
		}
	}

	public void draw() {
		p.pushMatrix();
			p.translate(position.x, position.y);
			p.rotate(p.PI/10);
			drawImage();
		p.popMatrix();
		//drawCollisionShapes();
	}
	
	public void drawCollisionShapes(){
		p.fill(255,0,0,50);
		for(CollisionCircle circle : collisionShapes.values()){
			p.stroke(0,2);
			p.ellipse(circle.center.x, circle.center.y, circle.radius, circle.radius);
		}
	}
	
	public void initializeCollisionShapes(){
		float bodyCollisionSize = 120;
		PVector offset = new PVector(20, -20);
		collisionShapes.put("body", new CollisionCircle(position, offset, bodyCollisionSize));
	}
	
	public PVector getCenterPosition(){
		PVector position = new PVector(collisionShapes.get("body").center.x, collisionShapes.get("body").center.y);
		return position;
	}

	public HashMap<String, CollisionCircle> getCollisionShapes(){
		return this.collisionShapes;
	}
	
	@Override
	public void drawImage() {
		p.image(image, 0, 0);
	}

}
