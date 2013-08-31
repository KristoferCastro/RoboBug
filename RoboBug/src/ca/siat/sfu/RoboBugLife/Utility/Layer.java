package ca.siat.sfu.RoboBugLife.Utility;

/**
 * class used to determine which perspective position an object is on the ground
 * @author Kristofer Castro
 * @date 8/3/2013
 */
public class Layer{
	private boolean middle;
	private boolean back;
	private boolean front;

	public Layer(){
		middle = false; 
		back = false;
		front = false;
	}
	
	public String getLayer(){
		if(middle == true)
			return "middle";
		else if (back == true)
			return "back";
		else
			return "front";
	}
	
	public void setToMiddle(){
		back = false;
		front = false;
		middle = true;
	}
	
	public void setToBack(){
		middle=false;
		front=false;
		back = true;
	}
	
	public void setToFront(){
		middle = false;
		back = false;
		front = true;
	}
	
	@Override
	public boolean equals(Object obj){
		if ( obj == null ) return false;
		if ( obj == this ) return true;
		if (!(obj instanceof Layer) ) return false;
		if(this.getLayer().equals(((Layer) obj).getLayer()))
				return true;
		return false;
	}

}
