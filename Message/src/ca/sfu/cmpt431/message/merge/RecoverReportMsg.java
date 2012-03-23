package ca.sfu.cmpt431.message.merge;

import java.util.ArrayList;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RecoverReportMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	public ArrayList<Integer> pos;
	
	public RecoverReportMsg(int cid, ArrayList<Integer> pos) {
		super(cid, MessageCodeDictionary.RECOVER_REPORT);
		this.pos = pos;
	}

}
