package ca.sfu.cmpt431.message.merge;

import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class MergeOutfit extends Message {
	
	private static final long serialVersionUID = 1L;

	public int yourPair;
	public String pairIp;
	public int pairPort;
	public Outfits lastfit;
	
	public MergeOutfit(int cid, Outfits fit, int pair, String ip, int port) {
		super(cid, MessageCodeDictionary.MERGE_OUTFIT);
		yourPair = pair;
		lastfit = fit;
		pairIp = ip;
		pairPort = port;
	}

}
