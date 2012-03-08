package ca.sfu.cmpt431.facility;

import java.io.Serializable;

public class Border implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public boolean [] bits;
	
	public Border(int length) {
		bits = new boolean[length];
	}

}
