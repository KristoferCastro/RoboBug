package ca.siat.sfu.RoboBugLife.Environment;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;
import processing.core.PVector;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawable;
import ca.siat.sfu.RoboBugLife.Utility.RGB;

/**
 * The class that controls the sky and weather.
 * 
 * @author Kristofer Ken Castro
 * @date 7/30/2013
 */
public class Sky implements IDrawable{
	private static final float MAX_BLUE = 232;
	private static final float MIN_BLUE = 100;
	
	// weather variables
	private boolean isStormy;
	private boolean isNight;
	private boolean isDay;
	
	private PApplet p;
	
	// RGB color of the sky
	private RGB color;
	
	// Lightning settings
	private float numberOfLightning = 3;
	private float weightOfLightning = 4f;
	private float displacement = 30;
	private ArrayList<Lightning> lightnings;
	private ArrayList<Lightning> onScreenLightnings;
	
	public Sky(PApplet p){
		this.p = p;
		color = new RGB(193+20,229+20,212+20);
		isDay = true;
		isNight = false;
		isStormy = false;
		lightnings = new ArrayList<Lightning>();
		onScreenLightnings = new ArrayList<Lightning>();
		
		Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask(){

			@Override
			public void run() {
				if ( isNight )
					transitionToDay();
				if ( isDay )
					transitionToNight();
				updateTimeOfDay();
			}
        	
        },100, 3000);
        
        initializeLightning();
	}

	@Override
	public void draw() {
		
		drawSkyColor();
		
		if ( isNight && isStormy )
			drawStormyBackground();
	
	}
	
	public void drawLightning(){
		
		for (int i = 0 ; i < onScreenLightnings.size(); i++){
			Lightning l = onScreenLightnings.get(i);
			drawALightning(l.startPosition, l.endPosition);
		}
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
	
	public void initializeLightning(){
		for (int i = 0 ; i < numberOfLightning ; i++){
			float x = p.random(0, p.width);
			float x2 = p.random(x-20, x+20);
			float y = 157;
			Lightning lightning = new Lightning(new PVector(x, 0), new PVector(x2, y));
			lightnings.add(lightning);
		}
	}
	
	public void enableStorm(){
		isStormy = true;
	}
	
	public void disableStorm(){
		isStormy = false;
	}
	
	/**
	 * Create a lightning that is displayed at the front of the screen (not the background)
	 * @param position
	 */
	public void createLightning(PVector position){
		final Lightning l = new Lightning(new PVector(position.x, 0), position);
		onScreenLightnings.add(l);
		
		removeLightningAfter(300, l);
	}
	
	/**
	 * Remove lightning from display after waiting x milliseconds
	 * @param millisecond to wait before deleting
	 * @param l lightning to delete
	 */
	private void removeLightningAfter(int millisecond, final Lightning l){
		// This is a lightning strike effect so we delete the lightning at some future point
		// when we don't want it to display anymore.
		Timer timer = new Timer();
        timer.schedule( new TimerTask(){

			@Override
			public void run() {
				onScreenLightnings.remove(l);
			}
        	
        },millisecond);
	}
	/**
	 * The function class users will use to strike a lightning from one position to another.
	 * 
	 * @param startingPoisiton where you are 
	 * @param endPosition
	 */
	public void drawALightning(PVector startingPosition, PVector endPosition){
		
		p.beginShape();
		drawALightningRecursion(startingPosition,endPosition,0, 3);
		p.endShape();
	}
	
	/**
	 * The function helper class for drawing a lightning that does the recursive calls of 
	 * creating the lightning effect.
	 * 
	 * @param startPoint of the lightning
	 * @param endPoint target of the lightning
	 */
	private void drawALightningRecursion(PVector startPoint, PVector endPoint, int currentDetail, int maxDetail){
		p.stroke(p.random(230,255),p.random(240,255),255);
		p.strokeWeight(weightOfLightning);
		p.noFill();
		float randomDisplacement = p.random(-displacement,displacement);
		PVector midpoint = new PVector((startPoint.x + endPoint.x)/2 + randomDisplacement,(startPoint.y + endPoint.y)/2);
		
		if ( currentDetail >= maxDetail){
			p.vertex(startPoint.x, startPoint.y);
			p.vertex(midpoint.x, midpoint.y);
			p.vertex(endPoint.x, endPoint.y);
			return;
		}
		
		drawALightningRecursion(startPoint, midpoint,++currentDetail, maxDetail);
		drawALightningRecursion(midpoint, endPoint,++currentDetail, maxDetail);
	
	}
	
	/**
	 * Draws lightning every 2 seconds then changes its position.
	 */
	private void drawStormyBackground(){
		if ( p.second()%2 == 0){
			for (int i = 0; i < numberOfLightning; i++){
				Lightning l = lightnings.get(i);
				drawALightning(l.startPosition, l.endPosition);
			}
		}else{
			for (int i = 0; i < numberOfLightning; i++){
				Lightning l = lightnings.get(i);
				l.startPosition.x = p.random(0, p.width);
				l.endPosition.x = p.random(l.startPosition.x-20, l.startPosition.x+20);			}
		}
	}
	
	/**
	 * Pressing the key up will transition towards day time;
	 * Pressing the down key will transition towards night time;
	 */
	public void keyPressed(){
		if (p.key != p.CODED) return;
		
		if (p.keyCode == p.UP){
			transitionToDay();	
		}
		
		if (p.keyCode == p.DOWN){
			transitionToNight();

		}	
		updateTimeOfDay();

	}
	
	/**
	 * increment the RGB value of the sky to make it transition closer to day time
	 */
	private void transitionToDay(){
		if(color.blue < MAX_BLUE){
			color.blue += 3f;
		}
		//193+20,229+20
		if (color.red < 193+20){
			color.red += 4;
		}
		
		if (color.green < 229+20){
			color.green += 4;
		}
		
	}
	
	/**
	 * decrement the RGB value of the sky to make it transition closer to night time
	 */
	private void transitionToNight(){
		
		if ( color.blue >= MIN_BLUE){
			color.blue -= 3f;
			color.red -= 4;
			color.green -= 4;
		}
		
	}
	
	/**
	 * Depending on the blue value, we change type of day.
	 */
	private void updateTimeOfDay(){
		// when do we say it is night time
		if ( color.blue <= 120){
			isDay = false;
			isNight = true;
	
		}
		if (color.blue >= 150 ){
			isStormy = false;
		}
		if ( color.blue >= 232){
			isDay = true;
			isNight = false;;
		}
	}

	public void setStormy(boolean b) {
		isStormy = b;
	}
	
	public boolean isDay(){
		return isDay;
	}
	
	public boolean isNight(){
		return isNight;
	}
	
	public boolean isStormy(){
		return isStormy;
	}
	public void drawSkyColor(){
		p.background(color.red,color.green,color.blue);
		/*p.fill(color.red,color.green,color.blue);
		p.noStroke();
		p.rect(0,0,p.width,157);*/
	}
	

}
