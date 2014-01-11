package Game;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import org.newdawn.slick.opengl.TextureImpl;

import Game.Skitter;
import Intelligence.AStar;
import Menu.GameState;
import Menu.Menu;
import ParticleSystem.ParticleEmitter;
import Utils.*;
import Menu.ScoreScreen;

public class Mazerunner {
	/*
	 * Local Variables
	 */

	protected static Player player;									// The player object.
	private UserInput input;										// The user input object that controls the player.
	public static int[][] maze; 									// The maze.	
	
	private long previousTime;										// Used to calculate elapsed time.
	
	private FloatBuffer lightPosition;								// Where the sun is located		

	protected ArrayList<Pickup> pickuplijst;						// A list of pickup items
	protected ArrayList<levelObject> moveable;								// A list of objects that will be displayed on screen. (immediate mode)
	private ArrayList<VisibleObject> visibleObjects;				// A list of objects that will be displayed on screen. (DLlist mode)
	protected static ArrayList<Monster> monsterlijst;				// Lijst met alle monsters
	protected static ArrayList<Monster> deathlist;
	protected static ArrayList<levelObject> objlijst;				// List of all collidable objects
	protected static int[][] objectindex;							// reference to the arraylist entry
	protected static int SQUARE_SIZE=1;								// Size of a unit block
	protected static int timer = 0;									// Keeps track of the time
	private static int MAXTIME;
	protected static int scorpcount;
	protected int numpickups;										// Amount of pickups that have already been dropped
	protected static StatusBars status = new StatusBars();			// Creates the overlay object/ HUD
	

	private MiniMap minimap;										// The minimap object.
	
	private String level;											// The string/path to the level to be loaded/current level
	
	private int objectDisplayList = glGenLists(1);					// The world (Walls, floor, spikes, obelisk) as a display list
	
	private FloatBuffer projectionWorld, projectionHUD;				// Buffer for the projection matrices
	
	public static boolean pausemonster = true;						// Debug option TODO:remove trigger by F3
	
	private Skitter skitter;										// Skitter scorpion sound
	private EndObelisk eo;
									
	/*
	 *  *************************************************
	 *  * 					Main Loop					*
	 *  *************************************************
	 */
	
public void start(String levelname) throws Exception{
	Menu.ingame = true;
	Sound.playMusic("background_game");
	new Game.Textures();			// Initialize textures
	new Graphics();					// Initialize graphics
	new Models();
	level = "levels/"+levelname;
	timer = 0;

						// needs a better way to implement this
	// TODO remove
	Display.setResizable(false);
										
	initObj();
	initGL();
	initDisp();
	
	// Pathfinding initialiseren
	AStar.loadMaze(maze);
	
	initTimer();
	
	
	previousTime = Calendar.getInstance().getTimeInMillis();
	
	while(!Display.isCloseRequested() && !player.isDead){
		
		// If the window is resized, might not be implemented
		if(Display.wasResized()) reshape();
		
		// Check for Input
		input.poll();
		
		// Check if pause menu is requested				
		checkPause();
		// If the option to main menu is selectd in the pause menu
		if(Menu.getState()==GameState.TOMAIN)break;
		Menu.setState(GameState.GAME);
		
		// Update all objects in the maze
		updateMovement();
		if(Menu.getState()==GameState.GAMEOVER)break;
		// Update intelligence
		Intelligence.update();
		
		// Draw objects on screen
		display();
		
		// Location print player location
		if(input.view_coord==true)System.out.println(player.getGridX(SQUARE_SIZE)+" "+player.getGridZ(SQUARE_SIZE));
			
		Display.update();
		Display.sync(60);
		
	}
	cleanup();
	Menu.ingame = false;
	System.out.println(Menu.getState().toString());
	if(Menu.getState().equals(GameState.GAME) || player.isDead){
		System.out.println("hier");
		Menu.setState(GameState.GAMEOVER);
		changetoHUD();
		ScoreScreen.initview();
		ScoreScreen.displayScoreatGO(-200, 0, 0);
	} 
	else if(Menu.getState()==GameState.TOMAIN){
		Menu.setState(GameState.MAIN);
	}else{
		changetoHUD();
		ScoreScreen.initview();
		ScoreScreen.displayScoreatGO(status.getScore(), Math.max(0, MAXTIME-timer), player.getHealth().getHealth());
	}

}

/*
 * **************************************************
 * *                 Load Maze                      *
 * **************************************************
 */
public void initMaze() throws ClassNotFoundException, IOException{
	maze = IO.readMaze(level);
	objectindex = new int[maze.length][maze[0].length];
	monsterlijst = new ArrayList<Monster>();
	deathlist = new ArrayList<Monster>();
	moveable = new ArrayList<levelObject>();
	pickuplijst = new ArrayList<Pickup>();
	Pickup.initPickup(maze);
	Intelligence.init();
	
	minimap = new MiniMap(maze);		//load the minimap
	int ID=1;
		
	for(int j = 0; j < maze.length; j++){
		for(int i = 0; i<maze[0].length; i++){
						
			// Parsing the walls
			if(maze[j][i]>0 && maze[j][i]<11){
				levelObject lvlo = new Wall(i*SQUARE_SIZE+SQUARE_SIZE/2.0, 0, j*SQUARE_SIZE+SQUARE_SIZE/2.0, SQUARE_SIZE, maze[j][i],SQUARE_SIZE);
				visibleObjects.add(lvlo);
				objlijst.add(lvlo);
				objectindex[j][i]=objlijst.size()-1;
			}
			// Parsing the begin point
			else if(maze[j][i]==11){
				// Initialize the player.
				player = new Player( i * SQUARE_SIZE + SQUARE_SIZE / 2.0, 	// x-position
									 SQUARE_SIZE *20/ 2.0 ,					// y-position
									 j * SQUARE_SIZE + SQUARE_SIZE / 2.0, 	// z-position
									 0, 0 ,									// horizontal and vertical angle
									 0.25*SQUARE_SIZE,SQUARE_SIZE* 3/2.0);	// player width and player height			
			}
			// Parsing the end point
			else if(maze[j][i]==12){
				eo = new EndObelisk(i * SQUARE_SIZE + SQUARE_SIZE / 2.0, 	// x-position
									 0,													// y-position
									 j * SQUARE_SIZE + SQUARE_SIZE / 2.0);				// z- position
				objlijst.add(eo);
//				visibleObjects.add(eo);
				objectindex[j][i]=objlijst.size()-1;

			}
			// Parsing the spikes
			else if(maze[j][i]==13){				
				Spikes s = new Spikes(i*SQUARE_SIZE, 0, j*SQUARE_SIZE);
				objlijst.add(s);
				visibleObjects.add(s);
				objectindex[j][i]=objlijst.size()-1;
			}
			// Parsing the scorpions
			else if(maze[j][i]==14){
				monsterlijst.add(new Monster(i * SQUARE_SIZE + SQUARE_SIZE / 2.0, 	// x-position
									 SQUARE_SIZE*0.7/2.0 ,							// y-position
									 j * SQUARE_SIZE + SQUARE_SIZE / 2.0, 			// z position
									 SQUARE_SIZE*0.7, SQUARE_SIZE*0.7, SQUARE_SIZE,ID)); // Width, height, squae size
				ID++;
			}
			// parsing the hatch
			else if(maze[j][i]==16){
				Hatch h = new Hatch(i*SQUARE_SIZE, 0, j*SQUARE_SIZE);
				moveable.add(h);
				objlijst.add(h);
				objectindex[j][i]=objlijst.size()-1;
			}
			// Parsing movable wall
			else if(maze[j][i]==17){
				MoveableWall mw = new MoveableWall(i*SQUARE_SIZE+SQUARE_SIZE/2.0, 0, j*SQUARE_SIZE+SQUARE_SIZE/2.0);
				moveable.add(mw);
				objlijst.add(mw);
				objectindex[j][i]=objlijst.size()-1;
			}
			// Parsing the floor
			if((maze[j][i]==11|| maze[j][i]==0 || maze[j][i]==14)){
			levelObject lvlo = new Floor(i*SQUARE_SIZE, 0, j*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, 1);
			visibleObjects.add(lvlo);
			objlijst.add(lvlo);
			objectindex[j][i]=objlijst.size()-1;
			}

		}
	}
	
	skitter=new Skitter();
	skitter.execute(monsterlijst);
	status.init( 0, player);

}
/*
 *  *************************************************
 *  * 			Initialization methods				*
 *  *************************************************
 */
	public void initGL(){		
		// Initialize projection buffers
		projectionWorld = BufferUtils.createFloatBuffer(16);
		projectionHUD = BufferUtils.createFloatBuffer(16);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glViewport(0, 0, Display.getWidth(), Display.getHeight());
		// Now we set up our viewpoint.
		
		// World projection matrix
		glMatrixMode(GL_PROJECTION);					// We'll use orthogonal projection.
		glLoadIdentity();
		gluPerspective(60, (float)Display.getWidth()/Display.getHeight(), 0.001f, 100f);
		glGetFloat(GL_PROJECTION_MATRIX, projectionWorld);
		
		// HUD projection matrix
		glLoadIdentity();
		glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
		glGetFloat(GL_PROJECTION_MATRIX, projectionHUD);
		
		glMatrixMode(GL_MODELVIEW);
		
		// Enable back-face culling.
		glCullFace(GL_BACK);
		glEnable(GL_CULL_FACE);
		
		// Enable Z-buffering
		glEnable(GL_DEPTH_TEST);
		
		// Enable normalize of normals
		glEnable(GL_NORMALIZE);
		
		// Set and enable the lighting.
		
		 	lightPosition = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(maze[0].length*SQUARE_SIZE+500).put(210f).put(maze.length*SQUARE_SIZE/2f).put(1.0f).flip();	// High up in the sky!

		 	FloatBuffer lightColour = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();		// White light!
	        glLight( GL_LIGHT0, GL_POSITION, lightPosition);	// Note that we're setting Light0.
	        glLight( GL_LIGHT0, GL_AMBIENT, lightColour);
	        
	        glEnable( GL_LIGHTING );
	        glEnable( GL_LIGHT0 );
	        
	     // Set the shading model.
	        glShadeModel( GL_SMOOTH );
	        
			glClearDepth(1.0f);			
			glDepthFunc(GL_LEQUAL);

	        
	}
	/**
	 * Cleanup after shut down
	 */
	public void cleanup(){	
		TextureImpl.unbind();
		glDisable(GL_LIGHTING);
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHT0);
//		glDisable(GL_BLEND);
		Sound.playMusic("background_menu");
		skitter.cleanup();
	
	}  
	/**
	 * Initialize all objects
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	   public void initObj() throws Exception{  
		   
	     // We define an ArrayList of VisibleObjects to store all the objects that need to be
			// displayed by MazeRunner.
			visibleObjects = new ArrayList<VisibleObject>();
			objlijst = new ArrayList<levelObject>();
		 // Initialize Maze ( Loading in and setting the objects in the maze )
			initMaze();	     			
			scorpcount = 0;
			for(int j = 0 ; j<maze.length; j++){
				for( int i = 0 ; i < maze[0].length; i++){
					if(maze[j][i]==14) scorpcount++;
				}
			}
			
			input = new UserInput();
			player.setControl(input);
			
				
		
	}
	/**
	 * Display function, draw all visible objects
	 */
	public void display(){
				if(Display.wasResized())reshape();
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
				
				glLoadIdentity();
				
				glRotated(-player.getVerAngle(), 1, 0, 0);
				glRotated(-player.getHorAngle()+(input.lookback?180:0), 0, 1, 0);
				// Draw sky box, the skybox is always in the origin, so no need to translate
				glCallList(Models.skybox);
				
				glTranslated(-player.locationX, -player.locationY, -player.locationZ);	
				
		        //update light positions
		        glLight( GL_LIGHT0, GL_POSITION, lightPosition);			        
				
		        // Display all the visible objects of MazeRunner.
		        if(!input.debug){ 	glCallList(objectDisplayList); }
		        
		        // Display all movable visible objects (immediate mode)
		        TextureImpl.bindNone();
		        for(VisibleObject vo:moveable){
		        	vo.display();
		        }		        

		        
		        // Monsters		
		        
		        for(Monster mo: monsterlijst){	
		        	if(mo.isDead){
		        		deathlist.add(mo);
		        	}
		        	
		        	double frustum = player.lookat().scale((input.lookback? -1:1),1,(input.lookback? -1:1))
		        			.dotprod(mo.getLocation().add(player.getLocation().scale(-1)).normalize());
		        	
		        	if(frustum>=0.35)
		        	mo.display();		        	
		        }
		        
		        for(Monster mo: deathlist){	
		        	monsterlijst.remove(mo);	        	
		        }
		        deathlist.clear();
		        
		        // Monster sound
		        skitter.updatemonsters(monsterlijst);
		        skitter.play(monsterlijst, player);
		        
		        // Pickups
		        ArrayList<Pickup> rmpu = new ArrayList<Pickup>();
		        for(Pickup pu: pickuplijst){
		        	pu.display();
		        	if(pu.check(player)){
		        		rmpu.add(pu);
		        	}
		        }
		        for (Pickup pu: rmpu){
		        	if (pickuplijst.remove(pu)){
		        		pu.effect();
		        	}
		        }
		        
		        eo.display();
		        
		        // HUD  and glare
		        glPushMatrix();
		        drawglare();
		        if(input.minimap){drawHUD();}
		        glPopMatrix();
		        player.draw();

	}
	
	/**
	 * Switches to 2D orthogonal view to project the HUD, after drawing the HUD, the Matrixmode is set back to 3D view
	 */
	private void drawHUD(){
		// Switch to 2D
		glPushAttrib(GL_ENABLE_BIT);
		changetoHUD();		
		
		status.draw(Math.max(0, MAXTIME - timer));		

		minimap.draw(player,monsterlijst,SQUARE_SIZE);

		// Making sure we can render 3d again	
		glPopAttrib();
		changetoWorld();

	}
	
	public void drawglare(){

		
		Vector playertosun = new Vector(lightPosition.get(0)-player.locationX,lightPosition.get(1)-player.locationY,lightPosition.get(2)-player.locationZ);
		playertosun.normalize();
		
		/*
		 * Please use odd numbers
		 */
		float factor = (float) Math.pow(player.lookat().dotprod(playertosun),25);
		
		changetoHUD();
		glEnable(GL_BLEND);
		glColor4f(1, 1, 1, factor*0.5f);
		glBegin(GL_QUADS);
		glVertex2f(0, 0);
		glVertex2f(0, Display.getHeight());
		glVertex2f(Display.getWidth(), Display.getHeight());
		glVertex2f(Display.getWidth(), 0);
		glEnd();
		glDisable(GL_BLEND);
		changetoWorld();
	}
	
	/**
	 * Pause menu options
	 */
	public void checkPause(){
		if(!Menu.getState().equals(GameState.GAME)){	
			boolean buffer = input.minimap;
			input.minimap = false;
			glPushAttrib(GL_ENABLE_BIT);			
			Menu.run();
			glPopAttrib();			
			changetoWorld();
			// Reset deltaTime
			previousTime = Calendar.getInstance().getTimeInMillis();

			input.minimap = buffer;
		}
	}

	/**
	 * Change projection matrix to ortho
	 */
	public void changetoHUD(){
		glMatrixMode(GL_PROJECTION);		
		glLoadMatrix(projectionHUD);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();		
				
		glDisable(GL_CULL_FACE);
		glDisable(GL_LIGHTING);
		glDisable(GL_TEXTURE_2D);
		glClear(GL_DEPTH_BUFFER_BIT);
	}
	
	/**
	 * Change projection matrix to perspective
	 */
	public void changetoWorld(){
		glMatrixMode(GL_PROJECTION);
		glLoadMatrix(projectionWorld);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glEnable(GL_CULL_FACE);
		glEnable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
	}
	
	/**
	 * if the window is reshaped, change accordingly
	 */
	public void reshape(){
		glViewport(0, 0, Display.getWidth(), Display.getHeight());	
		initGL();
	}
	/*
	 * **********************************************
	 * *				Methods						*
	 * **********************************************
	 */

		/**
		 * updateMovement(int) updates the position of all objects that need moving.
		 * This includes rudimentary collision checking and collision reaction.
		 */
		private void updateMovement()
		{
			// Calculating time since last frame.
			Calendar now = Calendar.getInstance();		
			long currentTime = now.getTimeInMillis();
			int deltaTime = (int)(currentTime - previousTime);
			previousTime = currentTime;
			timer += deltaTime;
			
			// TODO remove
			Display.setTitle("dt: "+ deltaTime);

			//Update any movement since last frame.
			Monster.setPlayerloc(new Vector(player.locationX, player.locationY, player.locationZ));	
				
			
			/*
			 * Movable objects
			 */
			for(levelObject lvlo: moveable){
				lvlo.update(deltaTime);				
			}
			player.update(deltaTime);						// Updating velocity vector
			/*
			 * Monsters
			 */
			if(!pausemonster){
				for(Monster mon: monsterlijst){
					mon.update(deltaTime);				
				}
			}
			
			if (timer>numpickups*10*1000){
				numpickups++;
				pickuplijst.add(new Pickup(false));
			}					
			eo.update(deltaTime);
		}
		
		/**
		 * Setting up the displayLists
		 */
		public void initDisp(){
			/*
			 * Walls, ground and spikes
			 */			
			TextureImpl.bindNone();
			glNewList(objectDisplayList, GL_COMPILE);
			 // Draw Walls			
			 for(VisibleObject vo:visibleObjects){
		      if(vo instanceof Wall){	Material.setMtlWall();vo.display();}		        	
		     }
			 // Draw Floor
			 for(VisibleObject vo:visibleObjects){
				 if(vo instanceof Floor){ Material.setMtlGround();vo.display();}		        	
		     }
			 // Draw Spikes
			 for(VisibleObject vo:visibleObjects){
				 if(vo instanceof Spikes){ Material.setMtlsteel();vo.display();}	        	
		     }
			 // Draw Obelisk
	         for(VisibleObject vo:visibleObjects){
	        	 if(vo instanceof EndObelisk){vo.display();}
	         }		 						
				 
			 glEndList();			 
				   
		}
		
		public void initTimer(){
			MAXTIME = 10*1000 * scorpcount + maze[0].length*maze.length*200;
		}
		
		public Player getPlayer(){return player;}
	}


