package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularBoardReturnMsg extends Message{

	private static final long serialVersionUID = 1L;

	public boolean isLeaving;
	public int top;
	public int left;
	public Board board;
	
	public RegularBoardReturnMsg(boolean lv, int cid, int top, int left, Board newBoard) {
		super(cid, MessageCodeDictionary.REGULAR_BOARD_RETURN);
		isLeaving = lv;
		this.top = top;
		this.left = left;
		board = newBoard;
	}
	
}
