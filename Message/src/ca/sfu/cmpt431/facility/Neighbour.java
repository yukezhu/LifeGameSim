package ca.sfu.cmpt431.facility;

import java.io.Serializable;

public class Neighbour implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public int [] position;
	public Comrade comrade;
	
	public Neighbour(int [] pos, Comrade com) {
		position = pos;
		comrade = com;
	}
	
}
