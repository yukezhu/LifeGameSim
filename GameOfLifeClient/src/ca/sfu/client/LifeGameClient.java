package ca.sfu.client;
import javax.swing.JFrame;

import ca.sfu.network.NetworkHelper;

public class LifeGameClient extends JFrame {

	private static final long serialVersionUID = 1L;
//	private static final String serverIp = "127.0.0.1";
	private static final String serverIp = "142.58.35.179";

	public static void main(String[] args)
	{
		Client c = new Client();
		try {
			if(args.length < 2)
			{
				System.out.println(NetworkHelper.getHostStaticIp());
				c.startClient(serverIp, NetworkHelper.getHostStaticIp());
//				System.out.println("Usage: client.jar [server ip] [host ip]");
			} else
			{
				c.startClient(args[0], args[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}