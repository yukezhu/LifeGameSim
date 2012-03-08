package ca.sfu.cmpt431.message.join;

import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class JoinOutfitsMsg extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public int myPort;
	public Outfits yourOutfits;
	
	public JoinOutfitsMsg(int cid, int myPort, Outfits yourOutfits) {
		super(cid, MessageCodeDictionary.JOIN_OUTFITS);
		this.myPort  = myPort;
		this.yourOutfits = yourOutfits;
	}

}
