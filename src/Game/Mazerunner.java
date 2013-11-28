package Game;


import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.glu.GLU;

import Menu.GameState;
import Menu.Menu;


public class Mazerunner {
	/*
	 * Local Variables
	 */
	
	private int screenWidth = 1280, screenHeight = 720;		// Deprecated
	public Player player;									// The player object.
	private Camera camera;									// The camera object.
	private UserInput input;								// The user input object that controls the player.
	public static int[][] maze; 									// The maze.
	private ArrayList<VisibleObject> visibleObjects;		// A list of objects that will be displayed on screen.
	
	private long previousTime = Calendar.getInstance().getTimeInMillis(); // Used to calculate elapsed time.
	private Wall wall;										// Wall Class, used to put one wall in for test TODO remove
	private Floor grond;									// Floor class used to put the floor in
	private FloatBuffer lightPosition;		
	
	protected static ArrayList<Monster> monsterlijst;				// Lijst met alle monsters
	protected static ArrayList<levelObject> objlijst;				// List of all collidable objects
	protected static int[][] objectindex;							// reference to the arraylist entry
	protected static int SQUARE_SIZE=1;								// Size of a unit block
	
	private MiniMap minimap;								// The minimap object.
	private String level = "levels/test5.maze";
	private int objectDisplayList = glGenLists(1);
	private int skyboxDL = glGenLists(1);
	/*
	 *  *************************************************
	 *  * 					Main Loop					*
	 *  *************************************************
	 */
	
public void start() throws ClassNotFoundException, IOException{
	new Game.Textures();			// Initialize textures
	new Graphics();					// Initialize graphics
	
	// TODO remove
	Display.setResizable(true);
										
	initObj();
	initGL();
	initDisp();
	
	
	while(!Display.isCloseRequested() && player.locationY>-20){
		
		// If the window is resized, might not be implemented
		if(Display.getWidth()!=screenWidth || Display.getHeight()!=screenHeight) reshape();
		
		// Check for Input
		input.poll();
		
		// Check if pause menu is requested
		if(!Menu.getState().equals(GameState.GAME)){
			
			glPushMatrix();
			glPushAttrib(GL_ENABLE_BIT);
			cleanup();
			Menu.run();
			glPopAttrib();
			glPopMatrix();
			initGL();
			previousTime = Calendar.getInstance().getTimeInMillis();
			Menu.setState(GameState.GAME);
		}
		
		// Draw objects on screen
		display();
		
		// Location print player location
		if(input.view_coord==true)System.out.println(player.getGridX(SQUARE_SIZE)+" "+player.getGridZ(SQUARE_SIZE));
			
		Display.update();
		Display.sync(70);
		
	}
	cleanup();
	Menu.setState(GameState.MAIN);
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
	
	minimap=new MiniMap(maze);		//load the minimap
	
	for(int j = 0; j < maze.length; j++){
		for(int i = 0; i<maze[0].length; i++){
			if(maze[j][i]>0 && maze[j][i]<11){
				levelObject lvlo = new Wall(i*SQUARE_SIZE+SQUARE_SIZE/2.0, 0, j*SQUARE_SIZE+SQUARE_SIZE/2.0, SQUARE_SIZE, maze[j][i],SQUARE_SIZE);
				visibleObjects.add(lvlo);
				objlijst.add(lvlo);
				objectindex[j][i]=visibleObjects.size()-1;
			}else{
				objectindex[j][i]=-200;
			}
		}
	}

	
}
/*
 *  *************************************************
 *  * 			Initialization methods				*
 *  *************************************************
 */
	public void initGL(){		
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glViewport(0, 0, Display.getWidth(), Display.getHeight());
		// Now we set up our viewpoint.
		glMatrixMode(GL_PROJECTION);					// We'll use orthogonal projection.
		glLoadIdentity();									// REset the current matrix.
		GLU.gluPerspective(60, (float)Display.getWidth()/Display.getHeight(), 0.001f, 1000);	// Set up the parameters for perspective viewing. 
		glMatrixMode(GL_MODELVIEW);
		
		// Enable back-face culling.
		glCullFace(GL_BACK);
		glEnable(GL_CULL_FACE);
		
		// Enable Z-buffering
		glEnable(GL_DEPTH_TEST);
		
		
		// Set and enable the lighting.
		
		 	lightPosition = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(maze[0].length*SQUARE_SIZE/2.0f).put(150.0f).put(maze.length*SQUARE_SIZE/2.0f).put(1.0f).flip();	// High up in the sky!
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
		glDisable(GL_LIGHTING);
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHT0);
		glDisable(GL_BLEND);
		Mouse.setGrabbed(false);
	}  
	/**
	 * Initialize all objects
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	   public void initObj() throws ClassNotFoundException, IOException{  
		   
	     // We define an ArrayList of VisibleObjects to store all the objects that need to be
			// displayed by MazeRunner.
			visibleObjects = new ArrayList<VisibleObject>();
			objlijst = new ArrayList<levelObject>();
		 // Initialize Maze ( Loading in and setting the objects in the maze
			initMaze();

	     // Initialize the player.
			player = new Player( 6 * SQUARE_SIZE + SQUARE_SIZE / 2.0, 	// x-position
								 SQUARE_SIZE *30/ 2.0 ,					// y-position
								 5 * SQUARE_SIZE + SQUARE_SIZE / 2.0, 	// z-position
								 0, 0 ,									// horizontal and vertical angle
								 0.25*SQUARE_SIZE,SQUARE_SIZE* 3/2.0);	// player width and player height							

			camera = new Camera( player.getLocationX(), player.getLocationY(), player.getLocationZ(), 
					             player.getHorAngle(), player.getVerAngle() );
			
			input = new UserInput();
			player.setControl(input);
			
			/*
			 * adding test objects
			 */
			
			wall = new Wall(10, 10, 0, 5, 2,SQUARE_SIZE);
			grond = new Floor(0, 0, 0, maze[0].length*SQUARE_SIZE, maze.length*SQUARE_SIZE,1,SQUARE_SIZE);	
			objlijst.add(wall);
			
			objlijst.add(grond);
			monsterlijst.add(new Monster(1+0.5*SQUARE_SIZE, 0.5*SQUARE_SIZE, 1+0.5*SQUARE_SIZE,SQUARE_SIZE,SQUARE_SIZE,SQUARE_SIZE));
			
		
	}
	/**
	 * Display function, draw all visible objects
	 */
	public void display(){
				// Calculating time since last frame.
				Calendar now = Calendar.getInstance();		
				long currentTime = now.getTimeInMillis();
				int deltaTime = (int)(currentTime - previousTime);
				previousTime = currentTime;
				// TODO remove
				Display.setTitle("dt: "+ deltaTime);
	
				//Update any movement since last frame.
				Monster.setPlayerloc(new Vector(player.locationX, player.locationY, player.locationZ));
								
				updateMovement(deltaTime);
								
				updateCamera();		
				
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
				
				glLoadIdentity();
				
		        GLU.gluLookAt( (float)camera.getLocationX(), (float)camera.getLocationY(),(float) camera.getLocationZ(), 
		        		(float)camera.getVrpX(), (float)camera.getVrpY(), (float)camera.getVrpZ(),
		        		(float)camera.getVuvX(), (float)camera.getVuvY(), (float)camera.getVuvZ() );
				
		        drawSkybox();
		        // Display all the visible objects of MazeRunner.
		        if(!input.debug){ 	glCallList(objectDisplayList); }
		        
		        //update light positions
		        glLight( GL_LIGHT0, GL_POSITION, lightPosition);	
		        
		        // Monsters		        
		        for(Monster mo: monsterlijst){			        	
		        	mo.display();		        	
		        }
		        
		        glPushMatrix();
		        if(input.minimap){drawHUD();}
		        glPopMatrix();
		        player.draw();



	}
	
	/**
	 * Switches to 2D orthogonal view to project the HUD, after drawing the HUD, the Matrixmode is set back to 3D view
	 */
	private void drawHUD(){
		// Switch to 2D
		
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(0.0, Display.getWidth(), Display.getHeight(), 0.0, -1.0, 1.0);
		
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glPushAttrib(GL_ENABLE_BIT);
		glLoadIdentity();
		glDisable(GL_CULL_FACE);
		glDisable(GL_LIGHTING);
		glDisable(GL_TEXTURE_2D);
		glClear(GL_DEPTH_BUFFER_BIT);

		minimap.draw(player,SQUARE_SIZE);

		// Making sure we can render 3d again
		glMatrixMode(GL_PROJECTION);
		glPopAttrib();
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
	}
	/**
	 * Draw a unit cube around the camera
	 */
	public void drawSkybox(){

		camera.setLocationX( 0 );
		camera.setLocationY( 0 );  
		camera.setLocationZ( 0 );
		camera.setHorAngle( player.getHorAngle()+(input.lookback? 180:0) );
		camera.setVerAngle( player.getVerAngle() * (input.lookback? -1:1) );
		camera.calculateVRP();
	 // Store the current matrix
		glPushMatrix();
	 
	    // Reset and transform the matrix.
	    glLoadIdentity();
	    
	    GLU.gluLookAt(
	        0f,0f,0f,																			// Set camera to origin
	        (float) camera.getVrpX(), (float) camera.getVrpY(),(float) camera.getVrpZ(),		// set the view reference point
	        0f,1f,0f);																			// View up vector
	 
	    glCallList(skyboxDL);
	    glPopMatrix();

	}

	/**
	 * if the window is reshaped, change accordingly
	 */
	public void reshape(){
		screenWidth = Display.getWidth();
		screenHeight = Display.getHeight();
		glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
		// Now we set up our viewpoint.
		glMatrixMode(GL_PROJECTION);					// We'll use orthogonal projection.
		glLoadIdentity();									// REset the current matrix.
		GLU.gluPerspective(60, (float)Display.getWidth()/(float)Display.getHeight(), 0.001f, 1000);	// Set up the parameters for perspective viewing. 
		glMatrixMode(GL_MODELVIEW);
		
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
		private void updateMovement(int deltaTime)
		{
			player.update(deltaTime);						// Updating velocity vector

			/*
			 * Monsters
			 */
			for(Monster mon: monsterlijst){
				mon.update(deltaTime);				
			}
			
			
		}

		/**
		 * updateCamera() updates the camera position and orientation.
		 * <p>
		 * This is done by copying the locations from the Player, since MazeRunner runs on a first person view.
		 */
		private void updateCamera() {
			camera.setLocationX( player.getLocationX() );
			camera.setLocationY( player.getLocationY() );  
			camera.setLocationZ( player.getLocationZ() );
			camera.setHorAngle( player.getHorAngle()+(input.lookback? 180:0) );
			camera.setVerAngle( player.getVerAngle() * (input.lookback? -1:1) );
			camera.calculateVRP();
		}
		
		/**
		 * Setting up the displayLists
		 */
		public void initDisp(){
			/*
			 * Walls and ground
			 */			
			
			glNewList(objectDisplayList, GL_COMPILE);
			 
			glMaterial( GL_FRONT, GL_DIFFUSE, Graphics.white);
			 for(VisibleObject vo:visibleObjects){
		        	if(vo instanceof Wall){
		        		Textures.ingamewall.bind();
		        		}
		        	vo.display();
		        }
			 
		        glMaterial( GL_FRONT, GL_DIFFUSE, Graphics.white);	  
		        glMaterial(GL_FRONT, GL_AMBIENT, Graphics.darkgrey);
		        glMaterialf(GL_FRONT, GL_SHININESS, -1f);
		        wall.display();		
				Textures.ground.bind();
				grond.display();
				
								
				glMaterial( GL_FRONT, GL_SPECULAR, Graphics.white);
				glMaterialf(GL_FRONT, GL_SHININESS,5f);
				
				glPushMatrix();

				glTranslatef(0.5f, 1f, 0.5f);
				Graphics.renderSpike(0.5f, 1);
				glPopMatrix();
				
				 
			 glEndList();
			
			 /*
			  * SkyBox	
			  */
			 glNewList(skyboxDL, GL_COMPILE);
			    // Enable/Disable features
			    glPushAttrib(GL_ENABLE_BIT);
			    glEnable(GL_TEXTURE_2D);
			    
			    glDisable(GL_DEPTH_TEST);
			    glDisable(GL_LIGHTING);
			    glDisable(GL_BLEND);
			    
			 float smallnumber = 0.002f;
			    // Just in case we set all vertices to white.
			    glColor4f(1,1,1,1);
			 
			    // Render the front quad
			    Textures.front.bind();
			    glBegin(GL_QUADS);
			        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f, -0.5f );
			        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f, -0.5f );
			        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f, -0.5f );
			        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f, -0.5f );			        
			        
			    glEnd();
			 
			    // Render the left quad
			    Textures.left.bind();
			    glBegin(GL_QUADS);
			    	glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f,  0.5f );
			    	glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f, -0.5f );
			    	glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f, -0.5f );	
			    	glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f,  0.5f );			        
			       
			    glEnd();
			 
			    // Render the back quad
			    Textures.back.bind();
			    glBegin(GL_QUADS);
			    	
			        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f,  0.5f );
			        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f,  0.5f );
			        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f,  0.5f );
			        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f,  0.5f );
			 
			    glEnd();
			 
			    // Render the right quad
			    Textures.right.bind();
			    glBegin(GL_QUADS);
			        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f, -0.5f );
			        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f, -0.5f );
			        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f,  0.5f );
			        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f,  0.5f );
			        
			    glEnd();
			 
			    // Render the top quad
			    Textures.top.bind();
			    glBegin(GL_QUADS);
			        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f,  0.5f, -0.5f );
			        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f,  0.5f, -0.5f );
			        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f,  0.5f );
			        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f,  0.5f );
			        
			    glEnd();
			 
			    // Render the bottom quad
			    Textures.bottom.bind();
			    glBegin(GL_QUADS);
			    	glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f,  0.5f );
			    	glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f, -0.5f, -0.5f );
			        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f, -0.5f, -0.5f );  		       
			        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f,  0.5f );
			        
			    glEnd();
			    // Restore enable bits and matrix
			    glPopAttrib();

			    glEndList();
			    
			   
			   
		}
	}
