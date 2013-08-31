package ca.siat.sfu.RoboBugLife.Environment;

import java.util.ArrayList;
import java.util.HashMap;

import ca.siat.sfu.RoboBugLife.Collision.CollisionRect;
import ca.siat.sfu.RoboBugLife.Utility.Layer;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Land contains resources and collection of ground tiles.  This class handles
 * resource manipulation (creation/deletion).
 * @author Kristofer Ken Castro
 * @date 7/27/2013
 */
public class Land {

	PApplet p;
	ArrayList<Ground> land; // land consist of a collection of ground tiles
	private ArrayList<Resource> resources;
	private HashMap<String, CollisionRect> collisionRects;
	public static int groundPosition;
	
	public Land(PApplet p){
		this.p = p;
		resources = new ArrayList<Resource>();
		land = new ArrayList<Ground>();
		groundPosition = p.height/2;
		collisionRects = new HashMap<String, CollisionRect>();
		
		initializeLand();
		initializeGroundCollisionLayers();
		createResource();
	}
	
	/**
	 * Create resource at a random ground layer (back, middle, or front)
	 */
	void createResource(){
		Layer layer = new Layer();
		
		PVector resourceGroundPosition = new PVector();
		int chance = (int) (Math.random()*3);
		if( chance == 0){
			layer.setToBack();
			resourceGroundPosition = new PVector(p.random(100, p.width),Resource.BACK_LAND_Y);
		}else if (chance == 1){
			layer.setToMiddle();
			resourceGroundPosition = new PVector(p.random(100, p.width), Resource.MIDDLE_LAND_Y);
		}else{
			layer.setToFront();
			resourceGroundPosition = new PVector(p.random(100, p.width), Resource.FRONT_LAND_Y);

		}
		resources.add(new Resource(p, resourceGroundPosition, layer));
	}
	
	/**
	 * Create resource based on mouse position.
	 * @param mouse position where to create resource
	 */
	private void createResource(PVector mouse){
		Layer layer = layerClicked(mouse);
		PVector groundLocation = new PVector(mouse.x, 0);
		if (layer == null) return;
		if (layer.getLayer().equals("back")){
			groundLocation.y = Resource.BACK_LAND_Y;
		}else if ( layer.getLayer().equals("middle") ){
			groundLocation.y = Resource.MIDDLE_LAND_Y;
		}else if ( layer.getLayer().equals("front") ){
			groundLocation.y = Resource.FRONT_LAND_Y;
		}
		resources.add(new Resource(p, groundLocation, layer));
	}
	
	private void initializeGroundCollisionLayers(){
		PVector backPosition = new PVector(0, Land.groundPosition);
		CollisionRect back = new CollisionRect(backPosition, new PVector(0,0), p.width, 15);
		this.collisionRects.put("back", back);
		
		PVector middlePosition = new PVector(0, Land.groundPosition+15);
		CollisionRect middle = new CollisionRect(middlePosition, new PVector(0,0), p.width, 20);
		this.collisionRects.put("middle", middle);
		
		PVector frontPosition = new PVector(0, Land.groundPosition+35);
		CollisionRect front = new CollisionRect(frontPosition, new PVector(0,0), p.width, 20);
		this.collisionRects.put("front", front);
		
	}
	
	private void drawCollisionShapes(){
		p.fill(255,0,0,100);
		for(CollisionRect shape : collisionRects.values()){
			p.rect(shape.position.x, shape.position.y, shape.width, shape.height);
		}
	}
	
	private void initializeLand(){
		PVector position0 = new PVector(-175, groundPosition);
		PVector position1 = new PVector(0, groundPosition);
		PVector position2 = new PVector(175, groundPosition);
		PVector position3 = new PVector(175*2, groundPosition);
		PVector position4 = new PVector(175*3, groundPosition);
		PVector position5 = new PVector(175*4, groundPosition);
		PVector position6 = new PVector(175*5, groundPosition);
		PVector position7 = new PVector(175*6, groundPosition);
		PVector position8 = new PVector(175*7, groundPosition);
		PVector position9 = new PVector(175*8, groundPosition);
		land.add(new Ground(p, position0));
		land.add(new Ground(p, position1));
		land.add(new Ground(p, position2));
		land.add(new Ground(p, position3));
		land.add(new Ground(p, position4));
		land.add(new Ground(p, position5));
		land.add(new Ground(p, position6));
		land.add(new Ground(p, position7));
		land.add(new Ground(p, position8));
		land.add(new Ground(p, position9));
	}
	
	/**
	 * Returns which layer the player clicked on the ground.  Used to spawn resources
	 * @param mouse position that was clicked
	 * @return layer
	 */
	public Layer layerClicked(PVector mouse){
		Layer layer = null;
		
		for(String key : this.collisionRects.keySet()){
			CollisionRect currentCollisionRect = collisionRects.get(key);
			PVector topLeft = currentCollisionRect.position;
			PVector topRight = new PVector(topLeft.x + currentCollisionRect.width, topLeft.y);
			PVector bottomLeft = new PVector(topLeft.x, topLeft.y + currentCollisionRect.height);
			
			if ( mouse.x >= topLeft.x && mouse.x <= topRight.x  //check x boundaries
				&& mouse.y >= topLeft.y && mouse.y <= bottomLeft.y  ){ // check y boundaries
				layer = new Layer();
				if ( key.equals("back") )
					layer.setToBack();
				else if ( key.equals("middle") )
					layer.setToMiddle();
				else 
					layer.setToFront();
			}
		}
		return layer;
	}
	
	public void draw(){
		for(Ground ground : land){
			ground.draw();
		}
		
		for ( int i = 0 ; i < resources.size() ; i++){
			resources.get(i).draw();
		}
		//drawCollisionShapes();
	}
	
	public ArrayList<Resource> getResources(){
		return resources;
	}

	/**
	 * Whenever we right-click the land, try to create a resource if possible
	 */
	public void mousePressed() {
		if ( p.mouseButton == p.RIGHT){
			this.createResource(new PVector(p.mouseX, p.mouseY));
		}
		
	}

}
