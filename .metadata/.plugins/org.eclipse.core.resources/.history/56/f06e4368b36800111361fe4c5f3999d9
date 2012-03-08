package ca.sfu.cmpt431.message.join;

import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class JoinOutfitsMsg extends Message {
	
	private static final long serialVersionUID = 1L;
	private Outfits yourOutfits;
	
	public JoinOutfitsMsg(int cid, Outfits yo) {
		super(cid, MessageCodeDictionary.JOIN_OUTFITS);
		yourOutfits = yo;
	}
	
	public Outfits getMyOutFits() {
		return yourOutfits;
	}

}
