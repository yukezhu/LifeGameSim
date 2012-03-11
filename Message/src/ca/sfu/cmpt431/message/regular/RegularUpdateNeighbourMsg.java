package ca.sfu.cmpt431.message.regular;

import java.util.ArrayList;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularUpdateNeighbourMsg extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Integer> pos;
	public int port;
	public String ip;

	public RegularUpdateNeighbourMsg(int cid, ArrayList<Integer> pos, int port, String ip) {
		super(cid, MessageCodeDictionary.REGULAR_UPDATE_NEIGHBOUR);
		this.pos = pos;
		this.port = port;
		this.ip = ip;
	}

}
