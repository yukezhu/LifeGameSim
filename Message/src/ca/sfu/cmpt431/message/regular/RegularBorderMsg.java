package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.facility.Border;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularBorderMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	public Border boarder;
	
	public RegularBorderMsg(int cid, Border boarder) {
		super(cid, MessageCodeDictionary.REGULAR_BORDER_EXCHANGE);
		this.boarder = boarder;
	}

}
