package ca.siat.sfu.RoboBugLife.HUD;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controlP5.Button;
import controlP5.CColor;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import ca.siat.sfu.RoboBugLife.Environment.Land;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawable;
import ca.siat.sfu.RoboBugLife.User.User;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

/**
 * The display HUD for the user displaying resources, interactable controls to spend resoruces, and more.
 * @author Kristofer Ken Castro
 *
 */
public class ControlHUD implements IDrawable{

	private ControlP5 controlP5; // declare variable of ControlP5
	private PVector position;
	private PApplet p;
	private PImage resourceLogo;
	private final String RESOURCE_LOGO_PATH = "/ca/siat/sfu/RoboBugLife/Resources/resource_logo.png";
	private PFont resourceCountFont;
	private int resourceCountText;
	
	private PImage workerCard;
	private final String WORKER_CARD_PATH = "/ca/siat/sfu/RoboBugLife/Resources/worker_card.png";
	
	private PImage soldierCard;
	private final String SOLDIER_CARD_PATH = "/ca/siat/sfu/RoboBugLife/Resources/soldier_card.png";
	
	private PImage lightningCard;
	private final String LIGHTNING_CARD_PATH = "/ca/siat/sfu/RoboBugLife/Resources/lightning_card.png";
	
	private int seconds;
	
	ArrayList<PImage> cards;
	
	Button buyWorker;
	Button buySoldier;
	Button toggleStorm;
	
	private ArrayList<ChangeListener> createWorkerListeners;
	private ArrayList<ChangeListener> createSoldierListeners;
	private ArrayList<ChangeListener> toggleStormListeners;
	
	private Minim minim;
	private final String PURCHASE_SOUND_PATH = "/ca/siat/sfu/RoboBugLife/resources/sounds/coins.wav";
	private AudioPlayer purchaseSoundPlayer;

		
	public ControlHUD(PApplet p, PVector position){
		this.p = p;
		controlP5 = new ControlP5(p);
		this.position = position;
		cards = new ArrayList<PImage>();
		resourceLogo = p.loadImage(RESOURCE_LOGO_PATH);
		resourceLogo.resize(50, 0);
		resourceCountFont = p.createFont("Arial", 44, true);
		minim = new Minim(p);
		try{
			purchaseSoundPlayer = minim.loadFile(PURCHASE_SOUND_PATH);
		}catch(Exception e){
			System.out.println("something wrong with purchase sound in ControlHUD.java");
		}
		
		workerCard = p.loadImage(WORKER_CARD_PATH);
		soldierCard = p.loadImage(SOLDIER_CARD_PATH);
		lightningCard = p.loadImage(LIGHTNING_CARD_PATH);
		lightningCard.resize(60, 0);
		workerCard.resize(60, 0);
		soldierCard.resize(60,0);
		cards.add(workerCard);
		cards.add(soldierCard);
		cards.add(lightningCard);
		
		buyWorker = controlP5.addButton("create worker", 0, (int)position.x+65, (int)position.y+120, 70, 15);
		buyWorker.hide();
		buyWorker.setColorForeground(p.color(0));
		buyWorker.setColorBackground(p.color(43, 64, 18));
		buyWorker.addCallback(createWorkerOnClick());
		
		buySoldier = controlP5.addButton("create soldier", 0, (int)position.x+165, (int)position.y+120, 70, 15);
		buySoldier.hide();
		buySoldier.setColorForeground(p.color(0));
		buySoldier.setColorBackground(p.color(43, 64, 18));
		buySoldier.addCallback(createSoldierOnClick());
		
		toggleStorm = controlP5.addButton("toggle storm!", 0,(int)position.x+265, (int)position.y+120, 70, 15 );
		toggleStorm.hide();
		toggleStorm.setColorForeground(p.color(0));
		toggleStorm.setColorBackground(p.color(43, 64, 18));
		toggleStorm.addCallback(toggleSoldierClick());
		
		createWorkerListeners = new ArrayList<ChangeListener>();
		createSoldierListeners = new ArrayList<ChangeListener>();
		toggleStormListeners = new ArrayList<ChangeListener>();
		
		Timer timer = new Timer();
	        timer.scheduleAtFixedRate( new TimerTask(){

				@Override
				public void run() {
					seconds += 1;
				}
	        	
	        },100, 1000);
	

	}


	@Override
	public void draw() {
		p.fill(0);
		p.noStroke();
		p.rect(position.x, position.y, p.width, 500);
		drawResourceLogo();
		drawResourceText();
		drawCards();
		drawTimer();
		update();
	}

	@Override
	public void update() {
		if (!buySoldier.isVisible()){
			buySoldier.show();
		}
		if (!buyWorker.isVisible()){
			buyWorker.show();
		}
		if (!toggleStorm.isVisible()){
			toggleStorm.show();
		}
	}
	
	/**
	 * add listeners that will observer for the event when the create worker button
	 * is clicked
	 * @param listener
	 */
	public void addCreateWorkerChangeListener(ChangeListener listener){
		this.createWorkerListeners.add(listener);
	}
	
	/**
	 * notify all listeners that the create worker button was clicked
	 */
	private void notifyCreateWorkerChangeListeners(){
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener listener : createWorkerListeners){
			listener.stateChanged(event);
		}
	}
	
	/**
	 * add listeners that will observe for the event when the create soldier button 
	 * is clicked
	 * @param listener
	 */
	public void addCreateSoldierChangeListener(ChangeListener listener){
		this.createSoldierListeners.add(listener);
	}
	
	
	/**
	 * notify all listeners that the create soldier button was clicked
	 */
	private void notifyCreateSoldierChangeListeners(){
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener listener : createSoldierListeners){
			listener.stateChanged(event);
		}
	}
	
	/**
	 * Event handler for when the create worker button is clicked.
	 * Notify all listeners of the event!
	 * @return
	 */
	private CallbackListener createWorkerOnClick(){
		return new CallbackListener(){

			@Override
			public void controlEvent(CallbackEvent e) {
				// TODO Auto-generated method stub
				if (e.getAction() == controlP5.RELEASE){
					notifyCreateWorkerChangeListeners();
				}
			}
		};
	}
	
	/**
	 * Event handler for when the create soldier button is clicked.
	 * Notify all lsiteners of the event!
	 * @return
	 */
	private CallbackListener createSoldierOnClick(){
		return new CallbackListener(){

			@Override
			public void controlEvent(CallbackEvent e) {
				// TODO Auto-generated method stub
				if (e.getAction() == controlP5.RELEASE){
					notifyCreateSoldierChangeListeners();
				}
			}
		};
	}
	
	/**
	 * Event handler for when the toggle storm button is clicked.
	 * @return
	 */
	private CallbackListener toggleSoldierClick() {
		return new CallbackListener(){

			@Override
			public void controlEvent(CallbackEvent e) {
				// TODO Auto-generated method stub
				if (e.getAction() == controlP5.RELEASE){
					notifyToggleStormChangeListeners();
				}
			}
		};
	}
	
	protected void notifyToggleStormChangeListeners() {
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener listener : toggleStormListeners){
			listener.stateChanged(event);
		}		
	}
	
	public void addToggleStormChangeListener(ChangeListener listener){
		this.toggleStormListeners.add(listener);
	}
	
	/**
	 * Draws the resource picture that is seen at the bottom center of the screen, right
	 * next to the resoruce count
	 */
	private void drawResourceLogo(){
		p.pushMatrix();
		p.imageMode(p.CENTER);
		p.translate(position.x+p.width/2, position.y + 75);
		p.image(resourceLogo, 0, 0);
		p.imageMode(p.CORNERS);
		p.popMatrix();
	}
	
	/**
	 * Draws the text that represents the number of resources the user has
	 */
	private void drawResourceText(){
		p.pushMatrix();
		p.translate(position.x+p.width/2-50, position.y+100);
		p.textAlign(p.RIGHT);
		resourceCountText = User.getInstance().resources;
		p.fill(255);
		p.textFont(resourceCountFont, 60);
		p.text(resourceCountText, 0, 0);
		p.popMatrix();
	}
	
	/**
	 * Draws the runtime timer
	 */
	private void drawTimer(){
		p.pushMatrix();
		p.translate(position.x+p.width/2, position.y+30);
		p.textAlign(p.RIGHT);
		p.fill(255);
		p.textFont(resourceCountFont, 20);
		p.text(timeText(), 0, 0);
		p.popMatrix();
	}
	
	private String timeText(){
		int currentSecond = seconds%60;
		int currentMinute = seconds/60;
		return String.format("%02d:%02d", currentMinute, currentSecond);
	}
	
	/**
	 * Draws the bugs creation cards that is seen on the HUD
	 */
	private void drawCards(){
		p.pushMatrix();
		p.imageMode(p.CENTER);
			p.translate(position.x + 100, position.y+75);
			for(PImage card : cards){
				p.image(card, 0, 0);
				p.translate(100, 0);
			}
		p.imageMode(p.CORNERS);
		p.popMatrix();
	}
	
	public void playPurchaseSound(){
		purchaseSoundPlayer.rewind();
		purchaseSoundPlayer.play();
	}
}
