package Game;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.opengl.TextureImpl;

import Menu.*;
import Utils.Text;

public class StatusBars {
	private static int health;
	private static int maxhealth = 100;
	private static float squaresize = 1f;
//	private static Text2 titel;
	
	public static void init(int hp) {
		health = hp;
//		titel = new Text2(15, "Health");
//		titel.initFont();
	}

	public static void draw(){
		
		float barwidth = (maxhealth + 2) * squaresize;		
		
		glPushMatrix();
		glLoadIdentity();
		double height = Text.getHeight(15);
		
		Text.draw(Display.getWidth()/2, (float) (2*height), 15, "Health");
		
		glTranslatef((float)(Display.getWidth()-1.5*barwidth), 50f,0);

		
		for (int i=0; i<(maxhealth +2); i++){
			glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
			drawBorder();
			glTranslatef(1f,0,0);
		}
		glTranslatef(-barwidth, 10.0f, 0.0f);
		
		for (int i=0; i<barwidth; i++){
			if(i<= health && i>0){
				glColor4f(0.0f, 1.0f, 0.0f, 0.7f);
			}else {
				glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
			}
			drawBlock();
			glTranslatef(1f,0,0);
		}
		glTranslatef(-barwidth, 1.0f, 0.0f);
		
		for (int i=0; i<(maxhealth +2); i++){
			glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
			drawBorder();
			glTranslatef(1f,0,0);
		}
		
		glPopMatrix();
	}
	
	public static void drawBlock(){
		glBegin(GL_QUADS);
		glVertex2f(0.0f, 0.0f);
		glVertex2f(squaresize, 0.0f);
		glVertex2f(squaresize, -10.0f);
		glVertex2f(0.0f, -10.0f);
		glEnd();
	}
	public static void drawBorder(){
		glBegin(GL_QUADS);
		glVertex2f(0.0f, 0.0f);
		glVertex2f(squaresize, 0.0f);
		glVertex2f(squaresize, -1.0f);
		glVertex2f(0.0f, -1.0f);
		glEnd();
	}
	
	public static void addHealth(int hp){
		System.out.println("add health");
		if((health + hp) <= maxhealth){
			health += hp;
		}
		else health=maxhealth;
	}
	
	public static void minHealth(int hp){
		System.out.println("min health");
		if((health - hp) >= 0){
			health -= hp;
		} else {
			Mazerunner.isdood = true;
			Menu.setState(GameState.GAMEOVER);
		}
	}
}
