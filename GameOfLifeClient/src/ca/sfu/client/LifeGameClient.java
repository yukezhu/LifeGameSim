package ca.sfu.client;
import javax.swing.JFrame;

public class LifeGameClient extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args)
	{
//		Client c = new Client();
//		try {
//			if(args.length < 2)
//				c.startClient(serverIp, NetworkHelper.getHostStaticIp());
//			else
//				c.startClient(args[0], args[1]);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}

}