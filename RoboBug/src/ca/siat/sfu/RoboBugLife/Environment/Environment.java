package ca.siat.sfu.RoboBugLife.Environment;

import java.util.ArrayList;
import ddf.minim.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * 
 * Class that contains all the environmental object of the map from trees, sky, land, resources.
 * 
 * @author Kristofer Ken Castro
 * @date 7/22/2013
 */
public class Environment {
	PApplet p;
	Land land;
	Sky sky;
	Home home;
	
	private final String BACKGROUND_PATH =  "/ca/siat/sfu/RoboBugLife/resources/background2.png";
	private final String HOMETREE_PATH ="/ca/siat/sfu/RoboBugLife/resources/hometree.png";
	PImage background;
	PImage homeTree;

	private Minim minim;
	private final String MORNING_SOUND_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/morning.wav";
	private final String NIGHT_SOUND_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/night.wav";
	private final String NIGHT_SOUND_LOOP_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/night_loop.wav";
	private final String STORMY_SOUND_LOOP_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/stormy.mp3";
	private final String LIGHTNING_SOUND_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/target_lightning.wav";
	private final String LIGHTNING_SOUND_MISS_PATH ="/ca/siat/sfu/RoboBugLife/resources/sounds/lightning_miss.wav";
	
	private AudioPlayer morningSoundPlayer;
	private AudioPlayer nightSoundPlayer;
	private AudioPlayer nightSoundLoopPlayer;
	private AudioPlayer stormySoundPlayer;
	private AudioPlayer lightningSoundPlayer;
	private AudioPlayer lightningSoundMissPlayer;
	
	// variables so we only play sounds the correct amount
	private boolean changedDaySound;
	private boolean changedNightSound;
	
	public Environment(PApplet p){
		this.p = p;
		land = new Land(p);
		sky = new Sky(p);
		background = p.loadImage(BACKGROUND_PATH);
		homeTree = p.loadImage(HOMETREE_PATH);
		homeTree.resize(0, 350);
		home = new Home(p, new PVector(50,Land.groundPosition-300));
		
		initializeSound();
	}

	private void initializeSound(){
		minim = new Minim(p);
		try{
			morningSoundPlayer = minim.loadFile(MORNING_SOUND_PATH,2048);
			nightSoundPlayer = minim.loadFile(NIGHT_SOUND_PATH,2048);
			nightSoundLoopPlayer = minim.loadFile(NIGHT_SOUND_LOOP_PATH,2048);
			nightSoundLoopPlayer.setGain(-20.0f);
			stormySoundPlayer = minim.loadFile(STORMY_SOUND_LOOP_PATH,2048);
			stormySoundPlayer.setGain(stormySoundPlayer.getGain()+20.0f);
			lightningSoundPlayer = minim.loadFile(LIGHTNING_SOUND_PATH,2048);
			lightningSoundMissPlayer = minim.loadFile(LIGHTNING_SOUND_MISS_PATH);
		}catch(Exception e){
			System.out.println(e.getStackTrace());
			System.out.println("something is wrong with the sounds from environment.java, initializeSound() method.");
		}
	}
	
	public void draw(){
		sky.draw();
		drawBackground();
		sky.drawLightning();
		land.draw();
		drawHomeTree();
		home.draw();
	}
	
	public void update(){
		updateTimeOfDaySounds();
	}
	
	/**
	 * Play the correct sounds during the day, night, and weather
	 */
	private void updateTimeOfDaySounds(){
		
		if (sky.isDay()){
			if( !changedDaySound){
				nightSoundPlayer.pause();
				nightSoundLoopPlayer.pause();
				stormySoundPlayer.pause();
				morningSoundPlayer.play();
				morningSoundPlayer.rewind();
				changedDaySound = true;
				changedNightSound = false;
			}
		}
		else if (sky.isNight()){
			if( !changedNightSound) {
				morningSoundPlayer.pause();
				nightSoundLoopPlayer.loop();
				nightSoundPlayer.rewind();
				nightSoundPlayer.play();
				changedNightSound = true;
				changedDaySound = false;
			}
			
			if ( sky.isStormy() ){
				if ( !stormySoundPlayer.isPlaying() ){
					stormySoundPlayer.rewind();
					stormySoundPlayer.loop();
					stormySoundPlayer.play();
				}
			}else{
				stormySoundPlayer.pause();
			}
		}
		
	}
	
	/**
	 * Draw a vertical lightning from the top to the given position
	 * @param position to strike
	 * @return if we were able to strike lightning (only when stormy weather)
	 */
	public boolean strikeLightning(PVector position){
		if (sky.isStormy()){
			lightningSoundPlayer.rewind();
			if(!lightningSoundPlayer.isPlaying())
				lightningSoundPlayer.play();
			sky.drawALightning(new PVector(position.x, 0), position);
			sky.drawALightning(new PVector(position.x, 0), position);
			return true;
		}
		return false;
	}
	
	/**
	 * Strike a lightning to a position
	 * 
	 * @param position where to strike the lightning
	 * @param miss wether it hit a target or not
	 * @return if we succesfully threw a lightning
	 */
	public boolean strikeLightning2(PVector position, boolean miss){
		if (sky.isStormy()){
			if ( miss ){
				lightningSoundMissPlayer.rewind();
				lightningSoundMissPlayer.play();
			}else{
				lightningSoundPlayer.rewind();
				lightningSoundPlayer.play();
			}
			sky.createLightning(position);
			return true;
		}
		return false;
	}
	private void drawBackground(){
		p.pushMatrix();
			p.translate(0,-90);
			p.image(background, 0, 0);
		p.popMatrix();
	}
	
	/**
	 * draw the tree that carries the home base of the bugs
	 */
	private void drawHomeTree(){
		p.pushMatrix();
			p.imageMode(p.CENTER);
			p.translate(90, Land.groundPosition-150);
			p.image(homeTree, 0, 0);
		p.popMatrix();
	}
	
	public ArrayList<Resource> getResources(){
		return land.getResources();
	}
	
	public void mousePressed() {
		land.mousePressed();
	}
	
	public void keyPressed(){
		sky.keyPressed();
	}
	
	void stop(){
		// always close audio I/O classes
		morningSoundPlayer.close();
		nightSoundPlayer.close();
		nightSoundLoopPlayer.close();
		stormySoundPlayer.close();
		lightningSoundPlayer.close();
		 // always stop your Minim object
		minim.stop();
	}

	public void spawnResource() {
		land.createResource();
	}

	public Sky getSky() {
		// TODO Auto-generated method stub
		return sky;
	}

	public Home getHome() {
		// TODO Auto-generated method stub
		return home;
	}
}
