package ca.sfu.message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	
	transient int width, height;
	transient long activationTime;
	
	public Message(int w, int h)
	{
		width = w;
		height = h;
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {  
		oos.defaultWriteObject();  
		oos.writeInt(width);
		oos.writeInt(height);
		System.out.println("session serialized");  
	}

	private void readObject(ObjectInputStream ois) throws IOException,  
	ClassNotFoundException {  
		ois.defaultReadObject();  
		width = ois.readInt();  
		activationTime = System.currentTimeMillis();  
		System.out.println("session deserialized");  
	}
	
	public void printOut()
	{
		System.out.println(width + " " + height);
	}
	
}
