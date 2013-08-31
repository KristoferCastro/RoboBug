package ca.siat.sfu.RoboBugLife.Main;

import java.awt.Color;

import controlP5.Button;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;

/**
 * intro screen showing how to interact and to proceed to the application
 * @author Kristofer Ken Castro
 * @date 8/6/2013
 */
public class IntroScreen {

	private PApplet p;
	private PVector position;
	private final String INTRO_IMAGE_PATH = "/ca/siat/sfu/RoboBugLife/resources/intro_screen.png";
	private PImage introImage;
	private boolean isVisible;
	Button startButton;
	private ControlP5 controlP5;

	
	public IntroScreen(PApplet p){
		this.p = p;
		position = new PVector(0,0);
		isVisible = true;
		controlP5 = new ControlP5(p);

		 PFont font = p.createFont("Calibri",18); 
		 controlP5.setControlFont(font);
		 
		try{
			introImage = p.loadImage(INTRO_IMAGE_PATH);
		}catch (NullPointerException e){
			System.out.println(e.getMessage());
		}
		
		startButton = controlP5.addButton("Start Environment", 0, p.width/2+360, p.height/2+250, 170, 50);
		startButton.addCallback(startButtonClickHandler());
	}
	
	private CallbackListener startButtonClickHandler() {
		return new CallbackListener(){

			@Override
			public void controlEvent(CallbackEvent e) {
				// TODO Auto-generated method stub
				if (e.getAction() == controlP5.RELEASE){
					isVisible = false;
					startButton.hide();
				}		
			}
			
		};
	}

	public void draw(){
		p.image(introImage, 0, 0);
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return isVisible;
	}
}
