package ca.sfu.cmpt431.facility;

import java.io.Serializable;
import java.util.ArrayList;

public class Outfits implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static int NUM_NEIBOURS;
	
	public int myId;
	public int nextClock;
	
	public int top;
	public int left;
	public Board myBoard;
	
	public ArrayList<Neighbour> neighbour;
	public Comrade pair;
	
	public Outfits(int id, int clk, int t, int l, int h, int w) {
		myId = id;
		myBoard = new Board(h, w);
		neighbour = new ArrayList<Neighbour>();
		pair = null;
	}
	
	public Outfits(int id, int clk, int t, int l, Board bd) {
		myId = id;
		myBoard = bd;
		neighbour = new ArrayList<Neighbour>();
		pair = null;
	}
	
}
