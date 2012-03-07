package ca.sfu.cmpt431.facility;

public class NodeInfo {
	
	private int id;
	private String ip;
	
	public NodeInfo(int id, String ip) {
		this.id = id;
		this.ip = ip;
	}
	
	public int getId() {
		return id;
	}
	
	public String getIp() {
		return ip;
	}
}
