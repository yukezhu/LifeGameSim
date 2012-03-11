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
	
	//status name
	public final static int ORIGINAL_STATUS= 0;
	public final static int GET_CLIENT_ID_STATUS= 1;
	public final static int RECEIVE_NEIGHBORS_CONFIRM_STATUS= -1;
	public final static int WAIT_FOR_COMMAND_STATUS= 2;
	public final static int UPDATE_NEIGHBOR_STATUS= -2;
	public final static int SEND_BORDER_STATUS= 3;
	public final static int REVEIVE_BORDER_OR_CONFIRM_STATUS= -3;
	public final static int SPLIT_STATUS= 4;
	public final static int WAIT_FOR_PAIR_CONFIRM_STATUS= -4;
	public final static int COMPUTING_STATUS= 5;
	
}
