package ca.siat.sfu.RoboBugLife.RoboBug;

import java.util.Timer;
import java.util.TimerTask;

import ca.siat.sfu.RoboBugLife.Collision.CollisionCircle;
import ca.siat.sfu.RoboBugLife.Collision.CollisionShape;
import ca.siat.sfu.RoboBugLife.Environment.Land;
import ca.siat.sfu.RoboBugLife.User.User;
import ca.siat.sfu.RoboBugLife.Utility.Layer;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * a sub-class of RoboBug that has added behavior of being able to fly
 * @author Kristofer Ken Castro
 * @date 7/25/2013
 *
 */
public class FlyingRoboBug extends RoboBug{
	
	// variables used for perline noise generated wing animations
	private float radiusNoise;
	private float radius;

	// variables used for perline noise generated wing animations
	private PVector wingCenter;
	private PVector wingPosition;
	private PVector wingLastPosition;
	
	// attraction angles
	private float angle;
	protected PVector attractionPoint; // used to pull the bug to a point . ex home base
	private boolean isAttracted;
	
	private boolean aboutToFly; // used for ground to flight animation
	private boolean isFlying;
	
	protected boolean returnHome ;
	private boolean isScared;
	
	public FlyingRoboBug(PApplet p, PVector position, Layer layer) {
		super(p, position, layer);
		wingCenter = position;
		wingPosition = new PVector();
		wingLastPosition = new PVector(-200, -200);
		velocity = new PVector(p.random(3,3),p.random(-1,1));
		attractionPoint = new PVector();
		setRandomGroundPosition();
	
		isFlying = true;
		isScared = true;
		setupRandomTravelModeSwitch(5000);
	}

	/**
	 * Every interval have a chance to switch travel mode
	 * @param intervalMilliseconds
	 */
	private void setupRandomTravelModeSwitch(int intervalMilliseconds) {
		Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask(){

			@Override
			public void run() {
				int randomValue = (int) (Math.random()*100);
				int chance = 30;
				if ( randomValue <= chance){
					if ( onGround){
						startFlying();
					}else{
						dropToGroundRandomly();
					}
				}
			}
        	
        },100, intervalMilliseconds);	
		
	}

	@Override
	public void draw(){
		super.draw();
		p.pushMatrix();
		
		// attracted to a point, start attracting
		if ( isAttracted() )
			attract();
		
		if (isVisible()){
			if( !onGround ){
				drawWings();
				isFlying = true;
			}else{
				isFlying = false;
			}
		}
		
		p.popMatrix();
		
	}
	
	@Override
	public void update(){
		
		PVector velocityToAdd = new PVector();
		if ( !onGround ){
			PVector randomVelocity = new PVector(p.random(-1,1), p.random(-2,2));
			velocityToAdd.add(randomVelocity);
			velocityToAdd.add(velocity);
		}else{
			velocityToAdd.add(new PVector(velocity.x ,0));
		}
		
		position.add(velocityToAdd);
		
		for(CollisionShape colShape : collisionShapes.values()){
			CollisionCircle colCircle = (CollisionCircle) colShape;
			colCircle.addToCenter(velocityToAdd);
			
		}
		
		checkHitGround();		
		checkBoundaryCollisions();	
	}
	
	/**
	 * start attracting the bug from its current position to the stored attraction point.
	 * Keep attracting, once it reaches destination, stop attracting.
	 */
	public void attract(){
		float marginOfError = 2;
		if(p.dist(position.x, position.y, attractionPoint.x, attractionPoint.y) >= marginOfError){
			setAttracted(true);
			angle = p.atan2(attractionPoint.y - position.y, attractionPoint.x - position.x);
			velocity.x = this.maxSpeed*p.cos(angle);
			velocity.y = this.maxSpeed*p.sin(angle);
		}else{
			this.stopAttracting();
		}
	}
	
	public boolean reachAttractionPoint(){
		for(String shapeKey : collisionShapes.keySet()){
			if ( !shapeKey.equals("Search Range")){
				CollisionCircle circle = (CollisionCircle) collisionShapes.get(shapeKey);
				if(p.dist(circle.center.x, circle.center.y, attractionPoint.x, attractionPoint.y) <= circle.radius){
					this.stopAttracting();
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Make the bug move towards some point
	 * @param attractionPoint where in the screen should they move towards to
	 */
	public void setAttractionPoint(PVector attractionPoint){
		this.attractionPoint = attractionPoint;
		this.setAttracted(true);
		
	}
	
	protected void stopAttracting(){
		this.setAttracted(false);
	}
	
	/**
	 * function that makes it so the bug can change its direction every x seconds (50% chance)
	 */
	private void randomMovementDirection(){
		int seconds = p.second();
		int interval = 7;
		// every 5 seconds there is a random chance it will turn around
		if( seconds%interval == 0 && seconds != changedVelocityTime){
			if (p.random(-1,1) > 0){
				velocity.x *= -1;
			}
			changedVelocityTime = seconds;
		}
		
	}
	
	/**
	 * Check if the body of the bug has collided with its ground position.
	 */
	protected void checkHitGround(){
		
		if ( !returnHome ){
			CollisionCircle body = (CollisionCircle) collisionShapes.get("Body");
		
			if(body.center.y >= this.groundPosition && !aboutToFly ){
				stopFlying();
			}
		}
	}
	
	/**
	 * launching to fly (initially move at a higher speed to elevate then slow down)
	 */
	public void startFlying(){
		
		if (onGround){
			aboutToFly = true;
			onGround = false;
			float speedX = maxSpeed;
			if ( velocity.x < 0) speedX *= -1;
		
			velocity = new PVector(speedX,-3);
	        Timer timer = new Timer();
	        timer.schedule(new TimerTask(){
	
				@Override
				public void run() {
					velocity = new PVector(p.random(minSpeed, maxSpeed),p.random(-1,1));
					aboutToFly = false;
				}
	        	
	        },1000);
		}
	}
	
	
	@Override
	protected void checkBoundaryCollisions(){
		super.checkBoundaryCollisions();
		CollisionCircle head = (CollisionCircle) collisionShapes.get("Head");
		if( head.center.y - head.radius <= 0 ) { // if head hit the top wall
			velocity.y *= -1;
			this.position.y = head.radius+5;
			for ( String key : collisionShapes.keySet()){
				CollisionCircle circle = (CollisionCircle) collisionShapes.get(key);
				circle.changeY(position.y);
			}	
		}

	}
	
	/**
	 * Drop to a random ground layer (back, middle, or front)
	 */
	public void dropToGroundRandomly(){
		setRandomGroundPosition();
		if( !onGround && !aboutToFly){
			float speedX = p.random(minSpeed,maxSpeed);
			if(velocity.x < 0) speedX *= -1;
			velocity = new PVector(speedX,p.random(minSpeed,maxSpeed));
		}
	}
	
	/**
	 * drop to the stored ground position
	 */
	public void dropToCurrentGround(){
		if( !onGround && !aboutToFly){
			int randDirection = (int) (Math.random()*2 == 0? velocity.x*=-1*maxSpeed : maxSpeed);
			velocity = new PVector(randDirection,p.random(1,2));
		}
	}
	
	public void dropStraightDownToCurrentGround(){
		if( !onGround && !aboutToFly){
			velocity = new PVector(0,3);
		}
	}
	
	private void stopFlying(){
		onGround = true;
	}
	
	/**
	 * Draw a perline noise generated wings or flight animation
	 */
	private void drawWings(){
		radiusNoise = p.random(50);
		radius = 2;
		p.stroke(p.random(0, 50), p.random(0, 50), p.random(0, 50), 80);
		p.strokeWeight(1.2f);
		float startangle = p.random(360);
		float anglestep = 0.9f;// 1 + (int) random(2);

		for (float ang = startangle; ang <= 500 + p.random(100); ang += anglestep) {
			radiusNoise += 0.01;
			radius += 0.009;

			float noiseRadius = radius + (p.noise(radiusNoise) * 200) - 100;
			float rad = p.radians(ang); // converts angle to radians

			p.pushMatrix();
			p.translate(wingCenter.x, wingCenter.y);
			wingPosition.x = noiseRadius * p.cos(rad);
			wingPosition.y = noiseRadius * p.sin(rad);
			if (wingLastPosition.x > -999){
				p.stroke(p.random(0, 50), p.random(0, 50), p.random(0, 50), 50);
				p.strokeWeight(p.random(1.2f,2));
				p.line(wingPosition.x, wingPosition.y, wingLastPosition.x, wingLastPosition.y);
				p.stroke(191,186,54,50);
				p.strokeWeight(p.random(1.5f,3f));
				p.line(wingPosition.x+p.random(-5,5), wingPosition.y+p.random(-5,5), wingLastPosition.x+p.random(-5,5), wingLastPosition.y+p.random(-5,5));
			}
			p.popMatrix();
			wingLastPosition.x = wingPosition.x;
			wingLastPosition.y = wingPosition.y;
		}
	}

	public boolean isReturningHome() {
		// TODO Auto-generated method stub
		return this.returnHome;
	}

	public boolean isFlying() {
		// TODO Auto-generated method stub
		return isFlying;
	}


	public boolean isScared() {
		// TODO Auto-generated method stub
		return isScared;
	}

	public void setIsScared(boolean b) {
		isScared = b;
	}
	
	
	public boolean isAttracted() {
		return isAttracted;
	}

	public void setAttracted(boolean isAttracted) {
		this.isAttracted = isAttracted;
	}

}
