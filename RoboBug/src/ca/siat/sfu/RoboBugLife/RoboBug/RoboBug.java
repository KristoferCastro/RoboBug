package ca.siat.sfu.RoboBugLife.RoboBug;
import java.util.ArrayList;
import java.util.HashMap;

import ca.siat.sfu.RoboBugLife.Collision.CollisionCircle;
import ca.siat.sfu.RoboBugLife.Collision.CollisionShape;
import ca.siat.sfu.RoboBugLife.Environment.Land;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawable;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawableWithImage;
import ca.siat.sfu.RoboBugLife.Utility.Layer;
import ca.siat.sfu.RoboBugLife.Utility.RGB;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/*
 * Class for the RoboBug object.
 * 
 * @author Kristofer Ken Castro
 * @date 7/20/2013
 */
public abstract class RoboBug{
	
	public static final float BACK_LAND_Y = Land.groundPosition -5;
	public static final float MIDDLE_LAND_Y = Land.groundPosition + 15;
	public static final float FRONT_LAND_Y = Land.groundPosition + 35;
	
	protected PApplet p;
	private PImage image;
	private final String IMAGE_PATH = "/ca/siat/sfu/RoboBugLife/resources/regular.png";
	public PVector position;
	protected PVector velocity;
	protected HashMap<String,CollisionShape> collisionShapes;
	
	// health variables
	protected float MAX_HEALTH_WIDTH = 30;
	protected float currentHealth;
	private final int DIES_EVERY_X_SECONDS = 120; // every 2 minutes
	
	private PVector bodyPosition;
	protected float scaleFactor;
	
	protected int changedVelocityTime;
	
	protected float groundPosition;
	public boolean onGround;
	private boolean isMoving;
	public Layer layer; // each bug has a layer that tells it which ground layer it will land to
	
	protected boolean returnHome ;
	
	protected float maxSpeed = 3;
	protected float minSpeed = 2;
	
	protected float tint; // used for alpha on images
	protected boolean collisionShapesRotated;
	
	// sound stuff
	Minim minim;
	AudioPlayer rechargeHealthPlayer;
	private final String RECHARGE_SOUND_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/recharge_health.wav";
	
	public RoboBug(PApplet p, PVector position, Layer layer){
		this.p = p;
		this.position = position;
		onGround = false;
		scaleFactor = 1f;
		bodyPosition = position;
		velocity = new PVector(0,0);
		isMoving = true;
		image = p.loadImage(IMAGE_PATH);
		collisionShapesRotated = false;
		collisionShapes = new HashMap<String, CollisionShape>();
		initializeCollisionShape();
		this.layer = layer;
		currentHealth = MAX_HEALTH_WIDTH;
		minim = new Minim(p);
		rechargeHealthPlayer = minim.loadFile(RECHARGE_SOUND_PATH);
		tint = 255;
	}

	public void draw() {
		p.pushMatrix();
			p.translate(position.x, position.y);
			drawHealthBar();
			if ( velocity.x < 0){
				p.rotate(p.PI);
				p.scale(1,-1);
				
				if( !collisionShapesRotated ){
					collisionShapesRotated = true;
					rotateCollisionShapes();
				}
			}else{
				if ( collisionShapesRotated ){
					collisionShapesRotated = false;
					rotateCollisionShapes();
				}
			}
			
			movingAnimation();
			deathAnimation();
		p.popMatrix();
		//drawRealCollisionShapes();

	}
	
	/**
	 * if its moving, rotate it to make it look like its moving
	 */
	protected void movingAnimation(){
		if (isMoving){
			p.rotate(p.PI/10);
		}
	}
	
	public abstract void update();
	
	
	public boolean isVisible(){
		return tint != 0;
	}
	public void turnVisibilityOff(){
		this.tint = 0;
	}
	public void turnVisibilityOn(){
		this.tint = 255;
	}
	/**
	 * Set the ground position to a random layer
	 */
	protected void setRandomGroundPosition(){
		int randNumber = (int) (Math.random()*3);
		if(randNumber == 0){
			groundPosition = RoboBug.BACK_LAND_Y;
			layer.setToBack();
		}else if ( randNumber == 1){
			groundPosition = RoboBug.MIDDLE_LAND_Y;
			layer.setToMiddle();

		}else{
			groundPosition = RoboBug.FRONT_LAND_Y;
			layer.setToFront();

		}
	}
	
	/**
	 * Specifically set the ground layer of the bug's landing point
	 * @param layer
	 */
	public void changeLayer(Layer layer){
		this.layer = layer;
		if (layer.getLayer().equals("middle")){
			groundPosition = RoboBug.MIDDLE_LAND_Y;
		}
		else if ( layer.getLayer().equals("front")){
			groundPosition = RoboBug.FRONT_LAND_Y;

		}
		else if (layer.getLayer().equals("back")){
			groundPosition = RoboBug.BACK_LAND_Y;
		}
	}
	
	/**
	 * Check weather the bug hit the left, right, and top wall
	 */
	protected void checkBoundaryCollisions(){
		CollisionCircle head = (CollisionCircle) collisionShapes.get("Head");
		if( head.center.x + head.radius >= p.width ) {
			velocity.x *= -1;
			position.x = p.width-(head.radius+20)*scaleFactor;
			// make sure we update the collision circles as well
			for ( String key : collisionShapes.keySet()){
				CollisionCircle circle = (CollisionCircle) collisionShapes.get(key);
				circle.changeX(position.x);
			}	
			//rotateCollisionShapes();
		}
		if ( head.center.x - head.radius <= 0){
			velocity.x *= -1;
			position.x = (head.radius+20)*scaleFactor;
			// make sure we update the collision circles as well
			for ( String key : collisionShapes.keySet()){
				CollisionCircle circle = (CollisionCircle) collisionShapes.get(key);
				circle.changeX(position.x);
			}	
			rotateCollisionShapes();
		}
	}
	
	/**
	 * decrease the health of the bug by x amount so that
	 * by "DIES_EVERY_X_SECONDS" amount of time has passed,
	 * the current health will be zero.
	 */
	public void decreateHealth(){
		currentHealth -= MAX_HEALTH_WIDTH/DIES_EVERY_X_SECONDS;
		if ( currentHealth < 0){
			currentHealth = 0;
		}
	}
	
	/**
	 * reset the health of the bug to the max
	 */
	public void rechargeHealth(){
		rechargeHealthPlayer.play();
		rechargeHealthPlayer.rewind();
		currentHealth = MAX_HEALTH_WIDTH;
	}
	
	/**
	 * used for when they are healing inside home base so we don't get annoying sounds
	 */
	public void rechargeHealthNoSounds(){
		currentHealth = MAX_HEALTH_WIDTH;
	}
	
	/**
	 * initializes all the collision shapes of the the robo bug and put it inside the hash map of collision shapes.
	 */
	public void initializeCollisionShape(){
		//CollisionShape circle = new CollisionCircle();
		
		// body 
		float collisionBodySize = 20*scaleFactor;
		float bodyOffsetX = -2*scaleFactor;
		float bodyOffsetY = 15*scaleFactor;
		CollisionShape bodyCollisionShape = new CollisionCircle(new PVector(position.x, position.y), new PVector(bodyOffsetX,bodyOffsetY), collisionBodySize/2);
		
		// head
		float collisionHeadSize = 40*scaleFactor;
		float headOffsetX = -2*scaleFactor;
		float headOffsetY = -6*scaleFactor;
		CollisionShape headCollisionShape = new CollisionCircle(new PVector(position.x, position.y), new PVector(headOffsetX,headOffsetY), collisionHeadSize/2);
		
		// claw
		float collisionClawSize = 8*scaleFactor;
		float clawOffsetX = 15*scaleFactor;
		float clawOffsetY = 7*scaleFactor;
		CollisionShape clawCollisionShape = new CollisionCircle(new PVector(position.x, position.y), new PVector(clawOffsetX, clawOffsetY), collisionClawSize);
		
		collisionShapes.put("Body", bodyCollisionShape);
		collisionShapes.put("Head", headCollisionShape);
		collisionShapes.put("Claw", clawCollisionShape);
		
		// search area to detect resources
		float collisionSearchSize = 250*scaleFactor;
		CollisionShape searchCollisionShape = new CollisionCircle(new PVector(position.x,position.y), new PVector(0,0), collisionSearchSize/2);
		collisionShapes.put("Search Range", searchCollisionShape);
	}
	
	
	public void drawRealCollisionShapes(){
		p.fill(255,0,0,80);
		p.stroke(0,50);
		for(CollisionShape colShape : collisionShapes.values()){
			CollisionCircle colCircle = (CollisionCircle) colShape;
			p.ellipse(colCircle.center.x, colCircle.center.y, colCircle.radius*2, colCircle.radius*2);
		}
	}
	
	/**
	 * draw collision shapes for debugging purposes
	 */
	private void drawCollisionShapes(){
		p.fill(255,0,0,150);
		p.ellipse(-2, 15, 20, 20); // body
		p.ellipse(-2,-6, 40,40);	// head
		//p.ellipse(0,0,800, 800); // search area
	}
	
	protected void rotateCollisionShapes() {
		
		float angle = p.PI;
		for ( String key : collisionShapes.keySet()){
			CollisionCircle circle = (CollisionCircle) collisionShapes.get(key);
			
			// do some geometry to rotate the circle's center by pi
			circle.center.x = position.x + (circle.center.x - position.x) * p.cos(angle) - (circle.center.y - position.y) * p.sin(angle); 
			circle.center.y= position.y + (circle.center.x - position.x) * p.sin(angle) + (circle.center.y - position.y) * p.cos(angle);
		
			// flip vertically, first bring y coordinate back to origin, then flip (-1), then translate back
			circle.center.y = (circle.center.y - position.y)*-1  + position.y ;	
		}	
	}
	
	public HashMap<String, CollisionShape> getCollisionShapes(){
		return collisionShapes;
	}
	
	/**
	 * health bar that displays above the bug's head
	 */
	protected void drawHealthBar(){
		float height = 5;
		float offsetY = -40;
		float offsetX = -15;
		p.stroke(0,100);
		p.strokeWeight(1f);
		p.fill(204,57,17);
		p.rect(offsetX, offsetY, MAX_HEALTH_WIDTH, height);
		p.fill(91,191,35);
		p.rect(offsetX, offsetY, currentHealth, height);
	}

	/**
	 * the bug is dead if its health is zero and it's completely not visible anymore
	 * @return
	 */
	public boolean isDead() {
		return currentHealth <= 0 && this.tint <= 0;
	}
	
	/**
	 * slowly lower the tint giving the fading away effect once the bugs
	 * health is below zero (in other words, its dead)
	 */
	private void deathAnimation(){
		if ( currentHealth <= 0 ){
			this.tint -= 3;
		}
	}
	
	void stop(){
		rechargeHealthPlayer.close();
		minim.stop();
	}
	
	/** checks to see if the bug has been clicked
	 * 
	 * @return if the bug has been clicked or not
	 */
	public boolean isClicked(){
		for(String shapeKey : collisionShapes.keySet()){
			if (!shapeKey.equals("Search Range")){
				CollisionCircle circle = (CollisionCircle)collisionShapes.get(shapeKey);
				if(p.dist(circle.center.x, circle.center.y, p.mouseX, p.mouseY) <= circle.radius){
					return true;
				}
			}
		}
		return false;
	}

}
