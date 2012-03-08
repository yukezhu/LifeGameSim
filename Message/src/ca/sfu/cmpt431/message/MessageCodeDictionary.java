package ca.sfu.cmpt431.message;

public class MessageCodeDictionary {

	public final static int ID_SERVER = -1;
	public final static int ID_INVALID = -2;

	public final static int REGULAR_NEXTCLOCK = 1;
	public final static int REGULAR_CONFIRM = 2;
	public final static int REGULAR_BORDER_EXCHANGE = 3;
	public final static int REGULAR_BOARD_RETURN = 4;
	public final static int REGULAR_UPDATE_NEIGHBOUR = 5;
	
	public final static int JOIN_REQUEST = 41;
	public final static int JOIN_OUTFITS = 42;
	public final static int JOIN_SPLIT = 43;
	
	public final static int SPLIT_MODE_VERTICAL = 0;
	public final static int SPLIT_MODE_HORIZONTAL = 1;
	
}
