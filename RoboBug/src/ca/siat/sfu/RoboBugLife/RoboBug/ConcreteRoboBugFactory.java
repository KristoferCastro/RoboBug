package ca.siat.sfu.RoboBugLife.RoboBug;

import ca.siat.sfu.RoboBugLife.Environment.Land;
import ca.siat.sfu.RoboBugLife.Utility.Layer;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Concrete Bug Factory to create different types of bugs
 * @author Kristofer Ken Castro
 * @date 7/22/2013
 */
public class ConcreteRoboBugFactory extends RoboBugFactory {

	
	public ConcreteRoboBugFactory(PApplet p) {
		super(p);
	}
	
	/**
	 * Factory method that creates bug objects and return sit
	 * 
	 * @param type of RoboBug
	 * @return bug
	 */
	public RoboBug createRoboBug(String type){
		RoboBug bug = null;
		Layer layer = new Layer();
		float randomLane = 0;
		int chance = (int) (Math.random()*3);
		if ( chance == 0){
			randomLane = WorkerRoboBug.BACK_LAND_Y;
			layer.setToBack();
		}else if ( chance == 1){
			randomLane = WorkerRoboBug.MIDDLE_LAND_Y;
			layer.setToMiddle();
		}
		else{
			randomLane = WorkerRoboBug.FRONT_LAND_Y;
			layer.setToFront();
		}
		
		if ( type.equals("Worker")){
			PVector randomGroundPosition = new PVector(p.random(0,p.width), randomLane);
			bug = new WorkerRoboBug(p, randomGroundPosition, layer);
		}
		
		if ( type.equals("Soldier")){
			float randmoLane = 0;
			PVector randomAirPosition = new PVector(p.random(0,p.width), p.random(0, RoboBug.BACK_LAND_Y-200)); 
			bug = new SoldierRoboBug(p, randomAirPosition, layer);
			
			
		}
		return bug;
	}
	
	/**
	 * Overloaded function to create a bug at a specific location
	 * @param type of bug
	 * @param position where to spawn
	 * @return bug
	 */
	public RoboBug createRoboBug(String type, PVector position){
		RoboBug bug = null;
		Layer layer = new Layer();
		float randomLane = 0;
		int chance = (int) (Math.random()*3);
		if ( chance == 0){
			randomLane = WorkerRoboBug.BACK_LAND_Y;
			layer.setToBack();
		}else if ( chance == 1){
			randomLane = WorkerRoboBug.MIDDLE_LAND_Y;
			layer.setToMiddle();
		}
		else{
			randomLane = WorkerRoboBug.FRONT_LAND_Y;
			layer.setToFront();
		}
		
		if ( type.equals("Worker")){
			bug = new WorkerRoboBug(p, position, layer);
		}
		
		if ( type.equals("Soldier")){
			bug = new SoldierRoboBug(p, position, layer);
			
		}
		return bug;
	}
	

}
