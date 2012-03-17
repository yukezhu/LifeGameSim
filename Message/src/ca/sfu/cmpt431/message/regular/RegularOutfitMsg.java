package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularOutfitMsg extends Message {
	private static final long serialVersionUID = 1L;

	public int myPort;
	public Outfits yourOutfits;
	
	public RegularOutfitMsg(int cid, int myPort, Outfits yourOutfits) {
		super(cid, MessageCodeDictionary.REGULAR_OUTFIT);
		this.myPort  = myPort;
		this.yourOutfits = yourOutfits;
	}
	
	public RegularOutfitMsg(int cid, Outfits yourOutfits) {
		super(cid, MessageCodeDictionary.REGULAR_OUTFIT);
		this.myPort  = -1;
		this.yourOutfits = yourOutfits;
	}
}
