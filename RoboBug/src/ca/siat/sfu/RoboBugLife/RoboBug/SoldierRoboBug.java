package ca.siat.sfu.RoboBugLife.RoboBug;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawableWithImage;
import ca.siat.sfu.RoboBugLife.Utility.Layer;

/**
 * Soldier bug that protects the other bugs from being attacked by enemy objects.  
 * 
 * @author Kristofer Ken Castro
 * @date 7/29/2013
 */
public class SoldierRoboBug extends FlyingRoboBug implements IDrawableWithImage{

	private PImage image;
	private final String IMAGE_PATH = "/ca/siat/sfu/RoboBugLife/resources/soldier.png";
	private final int MIN_ATTACK_DAMAGE = 1;
	private final int MAX_ATTACK_DAMAGE = 3;
	
	public SoldierRoboBug(PApplet p, PVector position, Layer layer) {
		super(p, position, layer);
		try{
			image = p.loadImage(IMAGE_PATH);
		}catch(NullPointerException e){
			System.out.println(e.getStackTrace());
			System.out.println("Somethign wrong with Soldier bug image");
		}
		this.startFlying();
		this.velocity = new PVector(3,3);
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
		drawImage();
		p.popMatrix();
	}

	@Override
	public void drawImage() {
		p.imageMode(p.CENTER);
		p.tint(255, this.tint);
		p.image(image, 0, 0);
		p.imageMode(p.CORNERS);
		p.noTint();
	}

}
