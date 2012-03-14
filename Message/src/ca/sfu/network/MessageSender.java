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
		out.writeInt(0);
		out.writeObject(msg);
		out.flush();
		int len = bOut.toByteArray().length;
		
		out.close();
		out = new ObjectOutputStream(bOut);
		out.writeInt(len - 4);
		out.flush();
		byte [] arr = bOut.toByteArray();
		
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