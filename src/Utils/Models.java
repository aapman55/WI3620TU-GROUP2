package Utils;

import static org.lwjgl.opengl.GL11.*;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Models {
	public static int monster, skybox;
	static{
		try {
			monster = Model.loadModel("res/Models/", "scorpion.obj").generateDList();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		  * SkyBox	
		  */
		skybox = glGenLists(1);
		 glNewList(skybox, GL_COMPILE);
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
		    Game.Textures.front.bind();
		    glBegin(GL_QUADS);
		        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f, -0.5f );
		        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f, -0.5f );
		        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f, -0.5f );
		        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f, -0.5f );			        
		        
		    glEnd();
		 
		    // Render the left quad
		    Game.Textures.left.bind();
		    glBegin(GL_QUADS);
		    	glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f,  0.5f );
		    	glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f, -0.5f );
		    	glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f, -0.5f );	
		    	glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f,  0.5f );			        
		       
		    glEnd();
		 
		    // Render the back quad
		    Game.Textures.back.bind();
		    glBegin(GL_QUADS);
		    	
		        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f,  0.5f );
		        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f,  0.5f );
		        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f,  0.5f );
		        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f(  0.5f, -0.5f,  0.5f );
		 
		    glEnd();
		 
		    // Render the right quad
		    Game.Textures.right.bind();
		    glBegin(GL_QUADS);
		        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f, -0.5f );
		        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f, -0.5f );
		        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f,  0.5f );
		        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f, -0.5f,  0.5f );
		        
		    glEnd();
		 
		    // Render the top quad
		    Game.Textures.top.bind();
		    glBegin(GL_QUADS);
		        glTexCoord2f(1-smallnumber, 0+smallnumber); glVertex3f( -0.5f,  0.5f, -0.5f );
		        glTexCoord2f(0+smallnumber, 0+smallnumber); glVertex3f(  0.5f,  0.5f, -0.5f );
		        glTexCoord2f(0+smallnumber, 1-smallnumber); glVertex3f(  0.5f,  0.5f,  0.5f );
		        glTexCoord2f(1-smallnumber, 1-smallnumber); glVertex3f( -0.5f,  0.5f,  0.5f );
		        
		    glEnd();
		 
		    // Render the bottom quad
		    Game.Textures.bottom.bind();
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
