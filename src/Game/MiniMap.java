package Game;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;

public class MiniMap {
	private int[][] maze;

	public MiniMap(int[][] maze) {
		this.maze = maze;
	}

	public void draw(Player player, ArrayList<Monster> monsterlijst, int SQUARE_SIZE) {
		int mapWidth = maze[0].length;
		int mapHeight = maze.length;

		int locX = player.getGridX(SQUARE_SIZE);
		int locZ = player.getGridZ(SQUARE_SIZE);
		

		int size = 10;

		// draw red maze
		glEnable(GL_BLEND);
		glColor4f(1.0f, 0.0f, 0.0f,0.5f);
		
		for (int z=locZ-size;z<locZ+size;z++){
			for (int x=locX-size;x<locX+size;x++){
				if (x >= 0 && z >= 0 && x < mapHeight && z < mapWidth) {
					if (maze[z][x] >= 1 && maze[z][x] <= 10) {
						for(int k=0; k<maze[z][x]; k++){
							drawBlock();
						}
					}
				}
				glTranslatef(10f,0f,0f);
			}
			glTranslatef(-10f*(2*size),10f,0f);
		}
		
		
		
		// draw black box around the minimap
		glPushMatrix();
		glLoadIdentity();
		for (int i = 0; i < size * 2; i++) {
			glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
			drawBlock();
			glTranslatef(10f, 0, 0);
		}
		for (int i = 0; i < size * 2 + 1; i++) {
			drawBlock();
			glTranslatef(0, 10f, 0);
		}
		glLoadIdentity();
		for (int i = 0; i < size * 2; i++) {
			drawBlock();
			glTranslatef(0, 10f, 0);
		}
		for (int i = 0; i < size * 2 + 1; i++) {
			drawBlock();
			glTranslatef(10f, 0, 0);
		}
		glPopMatrix();

		// draw monster dots

		glColor4f(0.2f, 0.2f, 1f,0.5f);
		for (Monster monster : monsterlijst) {
			glColor3f(0.2f, 0.2f, 1f);
			if (monster.playerSight()){
				glColor4f(0.2f, 1.0f, 0.2f,0.5f);
			}

			
			int relX=locX-monster.getGridX(SQUARE_SIZE);
			int relZ=locZ-monster.getGridZ(SQUARE_SIZE);
			if ((Math.abs(relX) < 10) && (Math.abs(relZ) < 10)) {
				glPushMatrix();
				glTranslated(size * 10, -size * 10, 0);
				glTranslated(-relX * size, -relZ * size, 0);
				drawBlock();
				glPopMatrix();
			}

		}

		// draw white player dot
		glPushMatrix();
		glTranslated(size * 10, -size * 10, 0);
		glColor4f(1.0f, 1.0f, 1.0f,0.5f);
		drawBlock();
		glPopMatrix();

	}

	public void drawBlock() {
		glBegin(GL_QUADS);
		glVertex2f(0.0f, 0.0f);
		glVertex2f(10.0f, 0.0f);
		glVertex2f(10.0f, 10.0f);
		glVertex2f(0.0f, 10.0f);
		glEnd();
	}
}
