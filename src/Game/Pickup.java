package Game;



import static org.lwjgl.opengl.GL11.*;

import java.util.Random;

import Utils.Models;

/**
 * @author Ferdi
 * Class that describes pickups
 */
public class Pickup extends levelObject {
	int type;
	float size=1/2f;
	float origsize;
	boolean on = false;
	static int[][] maze;
	static int [][] alpu;
	static Random random = new Random();
	
	
	/**
	 * Constructor for specific placement and type
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param type type of pickup
	 * @param size size of pickup
	 */
	public Pickup(double x, double y, double z, int type, float size) {
		super(x, y, z);
		this.type = type;
		this.size = size;
		this.origsize = size;
	}
	
	/**
	 * Constructor for random placement and type
	 * @param drop deprecated
	 */
	public Pickup(boolean drop){
		super(random.nextInt(maze[0].length)*Mazerunner.SQUARE_SIZE,
				0,random.nextInt(maze.length)*Mazerunner.SQUARE_SIZE);
		
		this.type=random.nextInt(5);
//		System.out.println(this.getGridZ(Mazerunner.SQUARE_SIZE)+" "+this.getGridX(Mazerunner.SQUARE_SIZE));
//		System.out.println(maze[0].length+" "+maze.length);
		while(maze[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]>0 
				&& maze[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]<10 
				|| alpu[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]==1
				|| maze[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]==15
				|| maze[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]==17){
			locationZ=random.nextInt(maze.length)*Mazerunner.SQUARE_SIZE;
			locationX=random.nextInt(maze[0].length)*Mazerunner.SQUARE_SIZE;
		}
		alpu[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]=1;
		locationX+=0.5;
		locationZ+=0.5;
		if (drop){
			locationY=Mazerunner.SQUARE_SIZE*5f;
		}
		else{
			locationY=Mazerunner.SQUARE_SIZE/2f;
		}
		
		//System.out.println("Pickup op punt "+locationX+", "+locationZ);
		
	}
	
	/* (non-Javadoc)
	 * @see Game.VisibleObject#display()
	 */
	public void display() {
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glTranslated(locationX, locationY, locationZ);		
		glCallList(Models.pickup);
		glDisable(GL_BLEND);
		glEnable(GL_TEXTURE_2D);
		glPopMatrix();
	}

	
	/**
	 * Checks whether the player is close enough for the pickup to be picked up
	 * @param player the player object
	 * @return
	 */
	public boolean check(Player player) {
		return Math.abs(player.locationX - this.locationX)<size 
				&& Math.abs(player.locationZ - this.locationZ) < size 
				&& Math.abs(player.locationY - this.locationY)<=1.5;
		}

	/**
	 * Effect an effect that affects the player
	 */
	public void effect(){
		alpu[this.getGridZ(Mazerunner.SQUARE_SIZE)][this.getGridX(Mazerunner.SQUARE_SIZE)]=0;
		switch(this.type){
		case 0: {Mazerunner.player.getHealth().addHealth(30);break;}
		case 1: {Mazerunner.player.getHealth().addHealth(-30);break;}
		case 2: {Mazerunner.status.addScore(50);break;}
		case 3: {Mazerunner.status.addScore(-50);break;}
		case 4: {Mazerunner.c4Count++;}
		}
	}
	
	
	/**
	 * Initialization function for pickup, creates a similar maze object to remember the squares that already have a pickup
	 * @param mazein the level maze
	 */
	public static void initPickup(int[][] mazein){
		maze = mazein;
		alpu = new int[maze.length][maze[0].length];
	}
	
	/**
	 * get X grid location
	 * @param SQUARE_SIZE
	 * @return
	 */
	public int getGridX(int SQUARE_SIZE){
		return (int) Math.floor(locationX/SQUARE_SIZE);
	}
	
	/**
	 * get Z grid location
	 * @param SQUARE_SIZE
	 * @return
	 */
	public int getGridZ(int SQUARE_SIZE){
		return (int) Math.floor(locationZ/SQUARE_SIZE);
	}
	

	public boolean isCollision(double x, double y, double z) {
		// TODO Auto-generated method stub
		return false;
	}

	public double getmaxDistX(double X) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getmaxDistY(double Y) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getmaxDistZ(double Z) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(int deltaTime) {
		// TODO Auto-generated method stub
	}

	public void change() {
		// TODO Auto-generated method stub
	}
}
