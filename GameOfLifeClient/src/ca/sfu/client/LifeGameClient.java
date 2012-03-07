package ca.sfu.client;
import javax.swing.JFrame;

public class LifeGameClient extends JFrame {

	private static final long serialVersionUID = 1L;
//6561
	public static void main(String[] args)
	{
		try {
			Client c = new Client();
			c.startClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
