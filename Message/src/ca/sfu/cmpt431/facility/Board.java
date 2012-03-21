package ca.sfu.cmpt431.facility;

import java.io.Serializable;

public class Board implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public int height;
	public int width;
	public boolean [][] bitmap;
	
	public Board(int h, int w) {
		height = h;
		width = w;
		bitmap = new boolean[h][w];
	}
	
	public Board(){
		
	}
	
}
