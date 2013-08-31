package ca.siat.sfu.RoboBugLife.Environment;

import ca.siat.sfu.RoboBugLife.Interfaces.IDrawable;
import ca.siat.sfu.RoboBugLife.Interfaces.IDrawableWithImage;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * 
 * Class for the ground object.  Uses PImage to use photoshop-made ground tiles.
 * 
 * @author Kristofer Ken Castro
 * @date 7/22/2013
 */
public class Ground implements IDrawableWithImage, IDrawable{
	private PApplet p;
	private final String IMG_PATH = "/ca/siat/sfu/RoboBugLife/resources/ground.png";
	private PImage image;
	private PVector position;
	
	public Ground(PApplet p, PVector position){
		this.p = p;
		this.position = position;
		
		try{
			image = p.loadImage(IMG_PATH);
		}catch(NullPointerException e){
			System.out.println("Something wrong with the ground tile image from Ground.java");
		}
	}
	
	@Override
	public void drawImage(){
		p.pushMatrix();
			p.translate(position.x, position.y);
			p.image(image, 0, 0);
		p.popMatrix();
	}

	@Override
	public void draw() {
		drawImage();
		
	}

	@Override
	public void update() {
		
	}


}
