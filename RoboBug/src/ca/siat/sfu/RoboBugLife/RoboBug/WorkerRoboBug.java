package ca.siat.sfu.RoboBugLife.RoboBug;

import java.util.Timer;
import java.util.TimerTask;

import ca.siat.sfu.RoboBugLife.Collision.CollisionCircle;
import ca.siat.sfu.RoboBugLife.Collision.CollisionShape;
import ca.siat.sfu.RoboBugLife.Environment.Land;
import ca.siat.sfu.RoboBugLife.Environment.Resource;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawableWithImage;
import ca.siat.sfu.RoboBugLife.User.User;
import ca.siat.sfu.RoboBugLife.Utility.Layer;
import ca.siat.sfu.RoboBugLife.Utility.RGB;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import ddf.minim.*;

/**
 * 
 * Worker bugs that extends FlyingRoboBug.  These bugs behavior is to collect resources.
 * 
 * @author Kristofer Ken Castro
 * @date 7/22/2013
 */
public class WorkerRoboBug extends FlyingRoboBug implements IDrawableWithImage{

	// Y position based on their image to properly display them correctly
	static final float BACK_LAND_Y = Land.groundPosition -5;
	static final float MIDDLE_LAND_Y = Land.groundPosition + 15;
	static final float FRONT_LAND_Y = Land.groundPosition + 35;
	
	// image stuff
	private PImage image;
	private PImage extractedResourceImage;
	private final String IMAGE_PATH = "/ca/siat/sfu/RoboBugLife/resources/worker.png";
	private final String EXTRACTED_RESOURCE_PATH = "/ca/siat/sfu/RoboBugLife/resources/extracted_resource.png";
	
	// behavior variables
	private boolean isMoving;
	private boolean collectingResource;
	private boolean isHoldingAResource;
	
	private Minim minim;
	
	// sound players
	private AudioPlayer miningResourceSound;
	private AudioPlayer extractedResourceSound;
	private AudioPlayer resourceReturned;
	
	// paths for all sounds that this clas uses
	private final String SOUND_MINING_RESOURCE = "/ca/siat/sfu/RoboBugLife/resources/sounds/mining_resource.wav";
	private final String SOUND_EXTRACTED_RESOURCE = "/ca/siat/sfu/RoboBugLife/resources/sounds/extracted_resource.wav";
	private final String SOUND_RESOURCE_RETURNED = "/ca/siat/sfu/RoboBugLife/resources/sounds/resource_returned.wav";
	private Resource currentResource;
	
	public WorkerRoboBug(PApplet p, PVector position, Layer layer) {
		super(p, position, layer);
		image = p.loadImage(IMAGE_PATH);
		extractedResourceImage = p.loadImage(EXTRACTED_RESOURCE_PATH);
		velocity = new PVector(2,0);
		isMoving = false;
		onGround = true;
		isHoldingAResource = false;
		this.layer = layer;
		this.startFlying(); // in the beginning, worker bugs are flying
		this.dropToGroundRandomly(); // and then we drop them randomly to the ground
		
		initializeSound();
	}

	private void initializeSound(){
		minim = new Minim(p);
		try{
			miningResourceSound = minim.loadFile(SOUND_MINING_RESOURCE,512);
			miningResourceSound.setGain(-15.0f);
			extractedResourceSound = minim.loadFile(SOUND_EXTRACTED_RESOURCE,512);
			extractedResourceSound.setGain(-15.0f);
			resourceReturned = minim.loadFile(SOUND_RESOURCE_RETURNED,512);
			resourceReturned.setGain(resourceReturned.getGain()-10.0f);
		}catch(NullPointerException e){
			System.out.println(e.getStackTrace());
			System.out.println("Something wrong with sound file(s) in WorkerRoboBug.java, initializeSound()");
		}
	}
	
	@Override
	public void update() {
		super.update();
		checkReachDestination();
	}
	
	/**
	 * Check wether the bug has reached its attraction point
	 */
	private void checkReachDestination() {
		if(returnHome){
			for(String shapeKey : collisionShapes.keySet()){
				if ( !shapeKey.equals("Search Range")){
					CollisionCircle circle = (CollisionCircle) collisionShapes.get(shapeKey);
					if(p.dist(circle.center.x, circle.center.y, attractionPoint.x, attractionPoint.y) <= circle.radius){
						
						// once we reach the destination, stop attracting it and make it move again
						returnHome = false;
						this.stopAttracting();
						this.startFlying();
						
						// the bug was heading home to return a resource
						if ( collectingResource ){
							User.getInstance().resources += 1;
							resourceReturned.rewind();
							resourceReturned.play();
							collectingResource = false;
							setHoldingAResource(false);
							setResource(null);
						}
						return;
					}
				}
			}
		}
	}
	
	@Override
	public void draw(){
		super.draw();
		p.pushMatrix();
		p.translate(position.x, position.y);
		if ( velocity.x < 0){
			p.rotate(p.PI);
			p.scale(1,-1);
		}
		
		movingAnimation();
		
		if(returnHome)
			drawExtractedResource();
		
		drawImage();
		p.popMatrix();
		//super.drawRealCollisionShapes();
	}
	
	/**
	 * Collect resource animation
	 * @param resourcePosition the location of the resource
	 */
	public void collectResource(PVector resourcePosition){
		if ( returnHome == false){
			stopMoving();		

			collectingResource = true;
			if (!miningResourceSound.isPlaying()){
				miningResourceSound.rewind();
				miningResourceSound.play();
			}
			
			collectingResource(resourcePosition);
		
			// return home
	        Timer timer = new Timer();
	        timer.schedule(new TimerTask(){

				@Override
				public void run() {
					returnHome = true;
					miningResourceSound.pause();
					if ( !extractedResourceSound.isPlaying() ){
						extractedResourceSound.play();
						extractedResourceSound.rewind();
					}
					startFlying();
					setAttractionPoint(new PVector(150, p.height/2-250));
				}
	        	
	        },2000);
		}
	}
	
	/**
	 * Animation effect for collecting a resource
	 */
	private void collectingResource(PVector resourcePosition){
		p.pushMatrix();
		float offsetX1 = 25;
		float offsetX2 = 22;
		if ( velocity.x < 0){
			offsetX1 *= -1;
			offsetX2 *= -1;
		}
		p.fill(p.random(122,142),p.random(171,191),p.random(160,186));
		p.noStroke();
		p.ellipse(position.x+offsetX1, position.y+5, p.random(10,30), p.random(10,30));
		p.noStroke();
		p.fill(p.random(160,189),p.random(230,255),p.random(220,248),110);
		p.ellipse(position.x+offsetX2, position.y+8, p.random(10,20), p.random(10,20));
		p.endShape();
		p.popMatrix();
	}

	/**
	 * Stop the animation effect for collecting a resource
	 */
	private void stopCollectingResource(){
		startMoving();
	}
	
	private void startMoving(){
		velocity = new PVector(maxSpeed,0);
		isMoving = true;
	}
	
	/**
	 * Stop the bug from moving
	 */
	public void stopMoving(){
		float almostZeroVelocity = 0.000001f;
		isMoving = false;
		if (velocity.x < 0)
			almostZeroVelocity *= -1;
		velocity = new PVector(almostZeroVelocity, 0);
	}
	
	@Override
	public void drawImage() {
		p.pushMatrix();
		p.imageMode(p.CENTER);
		p.scale(1f);
		p.tint(255, this.tint);
		p.image(image, 0, 0);
		p.imageMode(p.CORNERS); // reset back image mode to default
		p.popMatrix();
		p.noTint();
	}
	
	public void drawExtractedResource(){
		p.pushMatrix();
			p.imageMode(p.CENTER);
			p.scale(1/1.4f);
			p.translate(30,2);
			p.image(extractedResourceImage, 0, 0);
			p.imageMode(p.CORNERS);
		p.popMatrix();
	}
	
	void stop(){
		// always close audio I/O classes
		  miningResourceSound.close();
		  extractedResourceSound.close();
		  resourceReturned.close();
		  // always stop your Minim object
		  minim.stop();
	}

	public boolean isCollectingResource() {
		return this.collectingResource;
	}

	public boolean isHoldingAResource() {
		return isHoldingAResource;
	}

	public void setHoldingAResource(boolean isHoldingAResource) {
		this.isHoldingAResource = isHoldingAResource;
	}

	public void setResource(Resource currentResource) {
		this.currentResource = currentResource;
		
	}

	public boolean hasResource() {
		// TODO Auto-generated method stub
		return currentResource != null;
	}

	public Object getResource() {
		// TODO Auto-generated method stub
		return currentResource;
	}
}
