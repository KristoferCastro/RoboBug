package ca.siat.sfu.RoboBugLife.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.siat.sfu.RoboBugLife.Collision.CollisionCircle;
import ca.siat.sfu.RoboBugLife.Collision.CollisionShape;
import ca.siat.sfu.RoboBugLife.Environment.Environment;
import ca.siat.sfu.RoboBugLife.Environment.Land;
import ca.siat.sfu.RoboBugLife.Environment.Resource;
import ca.siat.sfu.RoboBugLife.Environment.Sky;
import ca.siat.sfu.RoboBugLife.HUD.ControlHUD;
import ca.siat.sfu.RoboBugLife.RoboBug.ConcreteRoboBugFactory;
import ca.siat.sfu.RoboBugLife.RoboBug.FlyingRoboBug;
import ca.siat.sfu.RoboBugLife.RoboBug.RoboBugFactory;
import ca.siat.sfu.RoboBugLife.RoboBug.WorkerRoboBug;
import ca.siat.sfu.RoboBugLife.RoboBug.RoboBug;
import ca.siat.sfu.RoboBugLife.User.User;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import processing.core.PApplet;
import processing.core.PVector;
/**
 * Main applicaiton logic for this program
 * @author Kristofer Ken Castro
 * @date 8/2/2013
 *
 */
public class Main extends PApplet{

	
	ArrayList<RoboBug> bugs;
	RoboBugFactory bugFactory;
	Environment environment; 
	ControlHUD controlHUD;
	IntroScreen introScreen;
	Minim minim;
	private final String CLICK_SOUND_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/buttonclick.wav";
	private AudioPlayer clickPlayer;
	
	public void setup(){
		size(1440,800);
		smooth();
		environment = new Environment(this);
		bugFactory = new ConcreteRoboBugFactory(this);
		controlHUD = new ControlHUD(this, new PVector(0, Land.groundPosition+250));
		introScreen = new IntroScreen(this);
		// testing
		bugs = new ArrayList<RoboBug>();
		bugs.add(bugFactory.createRoboBug("Worker"));
		bugs.add(bugFactory.createRoboBug("Soldier"));
		minim = new Minim(this);
		
		try{
			clickPlayer = minim.loadFile(CLICK_SOUND_PATH,2048);
		}catch(Exception e){
			System.out.println(e.getStackTrace());
			System.out.println("Something wrong with the click sound file. ");
		}
		
		setupRoboBugCreationListeners();
		setupSpawnMineralsInterval(700);
		setupLightningStrikeInterval(1000);
		startDecreaseHealthOfBugs();
		setupWeatherManipulationListeners();
		
	}
	
	/**
	 * Strike a lightning every given interval
	 * @param intervalMilliseconds interval of when we strike lightning
	 */
	private void setupLightningStrikeInterval(int intervalMilliseconds) {
		Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask(){

			@Override
			public void run() {
				int chance = 5;
				int rand = (int) (Math.random()*100);
				
				// random chance to kill a bug during a lightning storm
				if (rand <= chance){
					strikeRandomBug();
				}else{
					PVector randomPosition = new PVector (random(0,width), random(Land.groundPosition,RoboBug.FRONT_LAND_Y));
					boolean miss = true;
					environment.strikeLightning2(randomPosition, miss);
				}
			}
        	
        },100, intervalMilliseconds);
	}

	/**
	 * every given interval minerals spawn
	 */
	private void setupSpawnMineralsInterval(int intervalMilliseconds) {
		Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask(){

			@Override
			public void run() {
				
				if (environment.getSky().isDay()){
					final int chanceToSpawn = 4; // percentage
					int randomValue = (int) (Math.random()*100);
					if (randomValue <= chanceToSpawn){
						environment.spawnResource();
					}
				}
			}
        	
        },100, intervalMilliseconds);		
	}

	/**
	 * Every second decrease the health of bugs
	 */
	private void startDecreaseHealthOfBugs() {
		Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask(){

			@Override
			public void run() {
				decreaseHealthOfBugs();
			}
        	
        },100, 1000);
		
	}

	public void draw(){
		// if intro screen is still visible, only draw it.
		if (introScreen.isVisible()){
			introScreen.draw();
			return;
		}
		
		environment.draw();
		for(int i = 0 ; i < bugs.size(); i++){
			RoboBug bug = bugs.get(i);
			bug.draw();
			bug.update();
		}
		controlHUD.draw();
		bugResourceCollision();
		
		update();
	}

	public void update(){
		environment.update();
		checkForDeadBugs();
		removeResources();
		checkMoveBugsHome();
		resetBugScared();
	}
	
	/**
	 * Bugs go home when its night time and if they are scared
	 */
	private void checkMoveBugsHome() {

		for(int i = 0; i < bugs.size(); i++){
			RoboBug bug = bugs.get(i);
			if ( bug instanceof FlyingRoboBug){
				FlyingRoboBug flyBug = (FlyingRoboBug) bug;
				if ( environment.getSky().isNight() && flyBug.isScared() ){
					
					// lets attract them to the home
					if (!flyBug.reachAttractionPoint() ){
						PVector attractionPoint = new PVector(environment.getHome().getCenterPosition().x,
								environment.getHome().getCenterPosition().y);
						flyBug.setAttractionPoint(attractionPoint);
						
						// if the bug is on the ground, he needs to start flying to get to home base
						if (flyBug.onGround){
							flyBug.startFlying();
						}
						
					}else if (flyBug.reachAttractionPoint()){ //if we reach attraction point
						flyBug.turnVisibilityOff();
						flyBug.rechargeHealthNoSounds();
					}
				}else if ( environment.getSky().isDay() || !flyBug.isScared()){
					if ( !bug.isVisible() ){
						bug.turnVisibilityOn();
					}
				}
			}		
		}
	}

	/**
	 * remove resources that has already been used up
	 */
	private void removeResources() {
		for(int i = 0 ; i < bugs.size() ; i++){
			RoboBug bug = bugs.get(i);
			if (bug instanceof WorkerRoboBug){
				WorkerRoboBug currentWorkerBug = (WorkerRoboBug) bug;
				if(currentWorkerBug.isReturningHome() && currentWorkerBug.hasResource()){
					ArrayList<Resource> resources = environment.getResources();
					if (resources.contains(currentWorkerBug.getResource())){
						resources.remove(currentWorkerBug.getResource());
					}
				}
			}
		}
		
	}

	/**
	 * Listener functions that subscribes to the ControlHUD that contains
	 * the buttons for creating RoboBugs.  We handle creation here and listen
	 * for click events in the HUD.
	 */
	private void setupRoboBugCreationListeners(){
		ChangeListener createWorkerListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				if ( User.getInstance().resources >= 2){
					System.out.println(environment.getHome());
					bugs.add(bugFactory.createRoboBug("Worker",environment.getHome().getCenterPosition()));
					User.getInstance().resources -= 2;
					controlHUD.playPurchaseSound();
				}
			}
			
		};
		controlHUD.addCreateWorkerChangeListener(createWorkerListener);
		
		ChangeListener createSoldierListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				if ( User.getInstance().resources >= 3){
					bugs.add(bugFactory.createRoboBug("Soldier",environment.getHome().getCenterPosition()));
					User.getInstance().resources -= 3;
					controlHUD.playPurchaseSound();
				}
			}
			
		};
		controlHUD.addCreateSoldierChangeListener(createSoldierListener);
	}
	
	/**
	 * Listens if the user has clicked on the toggle storm weather, if so set the weather accordingly
	 */
	private void setupWeatherManipulationListeners(){
		ChangeListener changeToStormyListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				Sky sky = environment.getSky();
				if ( sky.isNight() ){
					if ( !sky.isStormy() )
						sky.setStormy(true);
					else{
						sky.setStormy(false);
					}
				}
			}
			
		};
		controlHUD.addToggleStormChangeListener(changeToStormyListener);
	}
	
	/**
	 * checks if any of the bugs has collided with a resource, if so start collecting
	 */
	private void bugResourceCollision(){
		ArrayList<Resource> resources = environment.getResources();
		for(int bugI = 0; bugI < bugs.size(); bugI++){
			for( int resI = 0; resI < resources.size(); resI++){
				RoboBug currentBug = bugs.get(bugI);
				
				// only worker bugs can collect resource so check if we're dealing with workers
				if(currentBug instanceof WorkerRoboBug){
					
					// store values for easier readability and caching performance
					WorkerRoboBug currentWorkerBug = (WorkerRoboBug) currentBug; 
					Resource currentResource = resources.get(resI);
					
					// go through each collision shape of the bug  to check for collision
					for(String bugShapeKey: currentBug.getCollisionShapes().keySet()){
			
							CollisionCircle bugCircle = (CollisionCircle) currentBug.getCollisionShapes().get(bugShapeKey);
							
							// go through each collision shape of the resource to check for collision with the bug
							for(CollisionShape resourceShape : currentResource.getCollisionShapes().values()){
								CollisionCircle resourceCircle = (CollisionCircle) resourceShape;
															
								// if we have a collision
								if (bugCircle.center.dist(resourceShape.center) <= bugCircle.radius + resourceCircle.radius){
									
									// if the collision between the bug's search range and resource is vertically aligned
									if(bugShapeKey.equals("Search Range") && Math.abs(currentBug.position.x - resourceCircle.center.x) <= 10){
										
										// if the bug is not on the ground, bring it to the ground layer, that is where the resource is!
										if( !currentWorkerBug.onGround ){
											currentWorkerBug.changeLayer(currentResource.layer);
											currentWorkerBug.dropStraightDownToCurrentGround();
										}
									}else{ // if we are colliding, not the search range, but a real bug body part
										
										// if the body part is the claw (which is what bugs use to extract resource) and on the ground level of the resource
										if(currentBug.layer.equals(currentResource.layer)
												&& bugShapeKey.equals("Claw")){
											
											// start collecting resource animation
											currentWorkerBug.collectResource(bugCircle.center);
											
											// if the bug doesn't have a resource holding yet, add this one
											if ( !currentWorkerBug.hasResource()){
												currentWorkerBug.setResource(currentResource);
											}
										}
									}
								}
							}
					}
				}
			}
		}
	}
	
	/**
	 * checks if a bug has been clicked, if it has then it changes travel mode
	 */
	private void checkBugClicked(){
		for(int i = 0 ; i < bugs.size(); i++){
			RoboBug bug = bugs.get(i);
			if (bug instanceof FlyingRoboBug){
				for(String shapeKey : bug.getCollisionShapes().keySet()){
					if (!shapeKey.equals("Search Range")){ // clicking the search range collision doesn't count
						CollisionCircle circle = (CollisionCircle) bug.getCollisionShapes().get(shapeKey);
						if(this.dist(circle.center.x, circle.center.y, this.mouseX, this.mouseY) <= circle.radius){
							FlyingRoboBug flyBug = (FlyingRoboBug) bug;
							
							// if the bug is on the ground, lets make it fly
							if (flyBug.onGround){
								flyBug.startFlying();
							}
							else{ // otherwise, its already flying, so drop it to a random ground layer
								flyBug.dropToGroundRandomly();
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * move bug to the mouse location.  Note: you can only click on anythign above the ground
	 * and you cannot click a bug
	 */
	private void moveFlyingBugsToLocation(){
		// can only click above the ground and if its not a bug being clicked
		if (mouseY >= Land.groundPosition || isAnyBugsClicked()) return;
		
		for(int i = 0 ; i < bugs.size(); i++){
			RoboBug bug = bugs.get(i);
			if (bug instanceof FlyingRoboBug ){ 
				FlyingRoboBug flyBug = (FlyingRoboBug) bug;
				
				// make sure they're not busy returning home already and they are indeed flying
				if (!flyBug.isReturningHome() && flyBug.isFlying()){
					if (flyBug instanceof WorkerRoboBug){
						if (!((WorkerRoboBug) flyBug).isCollectingResource()){ // unmovable if collecting resource
							flyBug.setAttractionPoint(new PVector(mouseX, mouseY));
						}
					}else{
						flyBug.setAttractionPoint(new PVector(mouseX, mouseY));
					}
				}
			}
		}
	}
	
	/**
	 * check if the bug is clicked on
	 * @param bug
	 * @return if a bug has been clicked
	 */
	private boolean isAnyBugsClicked(){
		try{
			for(int i = 0 ; i < bugs.size(); i++){
				RoboBug bug = bugs.get(i);
				if (bug.isClicked()) return true;
			}	
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("tried to call isAnyBugsclicked but index > bugs.size()");
		}
		return false;
	}
	
	/**
	 * Toggle between bugs being scared of the night or not
	 */
	private void checkHomeBaseClicked() {
		if ( environment.getSky().isDay() ) return; // only clickable during the day
		CollisionCircle collisionCenter = environment.getHome().getCollisionShapes().get("body");
		if( dist(collisionCenter.center.x, collisionCenter.center.y, mouseX, mouseY) <= collisionCenter.radius){			
			for ( int i = 0 ; i < bugs.size() ; i++){
				RoboBug bug = bugs.get(i);
				FlyingRoboBug flyBug = (FlyingRoboBug) bug;
				
				// toggle scared-ness
				if(flyBug.isScared())
					flyBug.setIsScared(false);
				else
					flyBug.setIsScared(true);
			}
		}
	}


	/**
	 * Find out the bug that was clicked and then recharge it if we have enough resources to recharge it.
	 */
	private void checkRechargeClickedBugs() {
		for(int i = 0 ; i < bugs.size() ; i++){
			RoboBug bug = bugs.get(i);
			if (bug instanceof FlyingRoboBug){
				for(String shapeKey : bug.getCollisionShapes().keySet()){
					if (!shapeKey.equals("Search Range")){
						CollisionCircle circle = (CollisionCircle) bug.getCollisionShapes().get(shapeKey);
						if(this.dist(circle.center.x, circle.center.y, this.mouseX, this.mouseY) <= circle.radius){
							if (User.getInstance().resources >= 1){
								bug.rechargeHealth();
								User.getInstance().resources -= 1;
							}
						}
					}
				}
			}
		}	
	}

	/**
	 * looks for bugs that have no more health and destroys them.
	 */
	private void checkForDeadBugs(){
		for(int i = 0; i < bugs.size(); i++){
			RoboBug bug = bugs.get(i);
			if (bug.isDead()){
				bugs.remove(bug);
			}
		}	
	}
	
	/**
	 * universal clicking sound for all layers
	 */
	private void clickSound(){
		clickPlayer.play();
		clickPlayer.rewind();
	}
	
	public void mousePressed() {
		  
		  if (mouseButton == LEFT) {
			  checkBugClicked();
			  checkHomeBaseClicked();
		  } 
		  
		  if (mouseButton == RIGHT){
			  moveFlyingBugsToLocation();
			  checkRechargeClickedBugs();
		  }
		  
		  environment.mousePressed();
	}
	
	public void mouseReleased(){
		  clickSound();
	}
	
	public void keyPressed(){
		environment.keyPressed();
		
		// space bar strikes a random bug with lightning
		if (this.key == 32){
			strikeRandomBug();
		}
	}

	/**
	 * randomly removes a bug and strike it with lightning
	 */
	private void strikeRandomBug(){
		if ( !bugs.isEmpty()){
			int randomIndex = (int) Math.random()*bugs.size();
			PVector position = null;
			RoboBug bug = bugs.get(randomIndex);
			
			if ( !bug.isVisible() ) return;  // only able to strike visible bugs
			
			position = new PVector(bug.position.x,bug.position.y);
			boolean miss = false;
			if (environment.strikeLightning2(position, miss)) // only if you are able to strike lightning
				bugs.remove(bug);
		}
		
	}
	
	private void resetBugScared(){
		if ( environment.getSky().isDay()){
			for ( int i = 0 ; i < bugs.size() ; i++ ){
				FlyingRoboBug bug = (FlyingRoboBug) bugs.get(i);
				if (!bug.isScared())
					bug.setIsScared(true); // reset them back to scared of the night
			}
		}
	}
	
	/**
	 * decrease the health of the bugs
	 */
	private void decreaseHealthOfBugs(){
		for(RoboBug bug : bugs){
			bug.decreateHealth();
		}
	}
	
	public void stop(){
		minim.stop();
		super.stop();
	}
	
	
}
