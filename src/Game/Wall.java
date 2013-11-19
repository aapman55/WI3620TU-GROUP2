package Game;

import static org.lwjgl.opengl.GL11.* ;

public class Wall extends GameObject{
	
	private double[][] boxvertices;
	private int[][] boxfaces= {{0,1,5,4},
								  {1,2,6,5},
								  {2,3,7,6},
								  {3,0,4,7},
								  {4,5,6,7}};
	private double[][] normals = new double[][]
										{{0,0,1},
										 {1,0,0},
										 {0,0,-1},
										 {-1,0,0},
										 {0,1,0}};

	
	public Wall(double x, double y, double z,double size, double height){
		super(x, y, z);
		this.boxvertices = new double[][]{
							{x-size/2, 0, z+size/2},
							{x+size/2, 0, z+size/2},
							{x+size/2, 0, z-size/2},
							{x-size/2, 0, z-size/2},
							{x-size/2, height, z+size/2},
							{x+size/2, height, z+size/2},
							{x+size/2, height, z-size/2},
							{x-size/2, height, z-size/2}};		
	}
	
	public void draw(){
		glBegin(GL_QUADS);
		for(int i = 0; i < boxfaces.length; i++){
			Utils.glNormalvec(normals[i]);
			for(int j = 0; j < boxfaces[0].length; j++){
				Utils.glVertvec(boxvertices[boxfaces[i][j]]);				
			}
		}
		glEnd();
	}
	
	public double[][] getVert(){ return boxvertices;}
	public double[][] getnorm(){ return normals;}
	
	public static void main(String[] args){
		Wall wall = new Wall(0, 0, 0, 5, 6);
		double[][] vert = wall.getVert();
		double[][] norm = wall.getnorm();
		for(int i = 0 ; i < vert.length;i++){
			for(int j = 0 ; j < vert[0].length;j++){
				System.out.print(vert[i][j]+" ");
			}
			System.out.println();
		}
		
		for(int i = 0 ; i < norm.length;i++){
			for(int j = 0 ; j < norm[0].length; j++){
				System.out.print(norm[i][j]+" ");
			}
			System.out.println();
		}
		
		
	}
}
