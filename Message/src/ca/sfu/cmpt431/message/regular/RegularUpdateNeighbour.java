package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularUpdateNeighbour extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public int [] pos;
	public int port;
	public String ip;

	public RegularUpdateNeighbour(int cid, int [] pos, int port, String ip) {
		super(cid, MessageCodeDictionary.REGULAR_UPDATE_NEIGHBOUR);
		this.pos = pos;
		this.port = port;
		this.ip = ip;
	}

}
