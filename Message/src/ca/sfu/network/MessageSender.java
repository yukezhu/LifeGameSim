package ca.sfu.network;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class MessageSender{
	
	private Selector selector = null;

	SocketChannel socketChannel;

	public String hostIp;

	public int hostListenningPort;

	public static void main(String[] args) throws IOException{
		MessageSender client = new MessageSender("142.58.35.130",1990);
		String msg = "hello";	
		client.sendMsg(msg);
	}

	public MessageSender(String HostIp, int HostListenningPort) throws IOException{
		this.hostIp=HostIp;
		this.hostListenningPort = HostListenningPort;   

		socketChannel=SocketChannel.open(new InetSocketAddress(hostIp, hostListenningPort));
		socketChannel.configureBlocking(false);

		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	public void sendMsg (Object msg) throws IOException{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bOut);
		byte [] lenbuf = new byte[4];
		out.write(lenbuf);
		out.writeObject(msg);
		out.flush();
		out.close();
		byte [] arr = bOut.toByteArray();
		
		int len = arr.length - 4;
		for(int i = 0; i < 4; i++)
			arr[i] = (byte) (len >> ((3 - i) * 8));
		
		System.out.println("sending message of size " + arr.length);
		
		ByteBuffer bb = ByteBuffer.wrap(arr);
		out.close();
		socketChannel.write(bb);
	}
	
	public boolean isOpen() {
		if(socketChannel == null) return false;
		return socketChannel.isOpen();
	}
	
	public void close() {
		try {
			selector.close();
		} catch (IOException e) {
		}
		try {
			socketChannel.close();
		} catch (Exception e) {
		}
	}
}