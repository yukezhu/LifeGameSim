package ca.sfu.client;

public class LifeGameClient {

	public static void main(String[] args)
	{
		try {
			if(args.length < 2)
			{
				System.out.println("Usage: client.jar [server ip] [host ip]");
			} else
			{
				Client c = new Client();
				c.startClient(args[0], args[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
