package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularBoardReturnMsg extends Message{

	private static final long serialVersionUID = 1L;

	public Board board;
	
	public RegularBoardReturnMsg(int cid, Board newBoard) {
		super(cid, MessageCodeDictionary.REGULAR_BOARDRET);
		board = newBoard;
	}
	
}
