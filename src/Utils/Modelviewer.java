package Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import static org.lwjgl.util.glu.GLU.*;
import static org.lwjgl.opengl.GL11.*;
/**
 * Test for loading models
 * @author ZL
 *
 */
public class Modelviewer {
	private float horangle=0;
	private float verangle=0;
	private float dX = 0, dY=0, dZ=0;
	
	public void start() throws LWJGLException{
		File path = null;
		try {
			path = choosefile();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "No input, application wil close now","No input",JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
				
		String folderpath = path.getParent();
		String filepath = path.getName();
	
		
		Display.setDisplayMode(Display.getDesktopDisplayMode());
		Display.setFullscreen(true);
		Display.setVSyncEnabled(true);
		Display.create();
		
		Model m = null;
		try {
			m = Model.loadModel(folderpath+"\\",filepath);				// Specify model .obj file

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int drawlist1 = m.generateDList();
		m=null;																// Drop the memory used for the model
		
		initGL();
		while(!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
			glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
			
			glPushMatrix();
			glTranslatef(dX, dY, dZ);
			glRotatef(horangle, 0, 1, 0);
			glRotatef(verangle, 1, 0, 0);
			
			glCallList(drawlist1);
			glPopMatrix();
			glLight(GL_LIGHT0, GL_POSITION, (FloatBuffer) BufferUtils.createFloatBuffer(4).put(0).put(0).put(-10).put(1).flip());
			inputpoll();
			
			Display.update();Display.sync(60);
		}
	}
	/**
	 * Poll for Keyinput
	 */
	public void inputpoll(){
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){horangle+=.5;}
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){horangle-=.5;}
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)){verangle +=.5;}
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){verangle -=.5;}
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){dZ -=.1;}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){dZ +=.1;}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)){dX -=.1;}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){dX += .1;}

	}
	/**
	 * Initialize openGL
	 */
	public void initGL(){
		glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
		// Initialize matrices
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(60, (float)Display.getWidth()/Display.getHeight(), 0.001f, 100f);
		glMatrixMode(GL_MODELVIEW);
		
		// Cullface
		glCullFace(GL_BACK);
		glEnable(GL_CULL_FACE);
		
		//depth
		glEnable(GL_DEPTH_TEST);
		
	     // Set the shading model.
        glShadeModel( GL_SMOOTH );
        
		glClearDepth(1.0f);			
		glDepthFunc(GL_LEQUAL);
		//
		glEnable(GL_LIGHT0);
		glEnable(GL_LIGHTING);
		
		glLight(GL_LIGHT0, GL_POSITION, (FloatBuffer) BufferUtils.createFloatBuffer(4).put(0).put(0).put(-10).put(1).flip());
		
		
		gluLookAt(0, 0, -10, 0, 0, 0, 0, 1, 0);
	}
	
	public File choosefile() throws IOException{
		JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));		
		jfc.setMultiSelectionEnabled(false);
		jfc.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("OBJ Files", "obj");
		jfc.addChoosableFileFilter(filter);
		int res = jfc.showOpenDialog(null);
		
		if(res == JFileChooser.APPROVE_OPTION){
			File file = jfc.getSelectedFile();
			return file;
			
			
		}
		throw new IOException("You pressed cancel or X");
	}
	
	/**
	 * Main program 
	 * @param args
	 */
	public static void main(String[] args){
		try {
			new Modelviewer().start();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
