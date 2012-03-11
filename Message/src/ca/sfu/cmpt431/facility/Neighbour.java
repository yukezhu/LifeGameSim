package ca.sfu.cmpt431.facility;

import java.io.Serializable;
import java.util.ArrayList;

public class Neighbour implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public ArrayList<Integer> position;
	public Comrade comrade;
	
	public Neighbour(ArrayList<Integer> pos, Comrade com) {
		position = pos;
		comrade = com;
	}
	
}
