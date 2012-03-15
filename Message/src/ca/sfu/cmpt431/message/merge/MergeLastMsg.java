package ca.sfu.cmpt431.message.merge;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class MergeLastMsg extends Message{

	private static final long serialVersionUID = 1L;
	
	public int newpair;
	public String pairIp;
	public int pairPort;
	
	public MergeLastMsg(int pair, String ip, int port) {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.MERGE_LAST);
		newpair = pair;
		pairIp = ip;
		pairPort = port;
	}
	
}
