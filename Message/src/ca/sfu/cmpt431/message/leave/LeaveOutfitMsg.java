package ca.sfu.cmpt431.message.leave;

import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class LeaveOutfitMsg extends Message {
	private static final long serialVersionUID = 1L;

	public Outfits outfit;
	
	public LeaveOutfitMsg(int cid, Outfits out) {
		super(cid, MessageCodeDictionary.LEAVE_OUTFIT);
		outfit = out;
	}
}
