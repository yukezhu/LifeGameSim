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

	private static final int BufferSize = 165536;
	private static byte [] tmpbuf = new byte[BufferSize];
	
	private Selector selector = null;

	SocketChannel socketChannel;

	public String hostIp;

	public int hostListenningPort;

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
		out.writeObject(msg);
		out.flush();
		
//		long ts, te;
//		ts = System.currentTimeMillis();
//		System.out.println("start compressing,");
		
		byte [] arr = MessageCompressor.compress(bOut.toByteArray());
		
//		te = System.currentTimeMillis();
//		System.out.println("compression finished. used time:" + (te - ts) / 1000.0 + "  after compression size is:" + arr.length);
		
		
		int len = arr.length;
		for(int i = 0; i < 4; i++)
			tmpbuf[i] = (byte) (len >> ((3 - i) * 8));
//		for(int i = 0; i < arr.length; i++)
//			tmpbuf[i+4] = arr[i];
		System.arraycopy(arr, 0, tmpbuf, 4, arr.length);
		
		ByteBuffer bb = ByteBuffer.wrap(tmpbuf, 0, len + 4);
		
//		ts = System.currentTimeMillis();
//		System.out.println("start sending,");
		int written = 0;
		while(written < len + 4) {
			written += socketChannel.write(bb);
		}
		
//		te = System.currentTimeMillis();
//		System.out.println("sending finished. used time:" + (te - ts) / 1000.0);
		
		out.close();
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