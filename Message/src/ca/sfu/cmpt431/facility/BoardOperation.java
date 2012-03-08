package ca.sfu.cmpt431.facility;

public class BoardOperation {

	public static Board NextMoment(Board b, boolean[] up, boolean[] down, boolean[] left, boolean[] right)
	{
		Exception exception = new java.lang.IllegalArgumentException();
		if(up.length != b.width || down.length != b.width)
		{
			System.err.println("Up & down border lengths fail to match the board.");
			throw exception;
		}
		if(left.length != b.height || right.length != b.height)
		{
			System.err.println("Left & right border lengths fail to match the board.");
			throw exception;
		}
		
		return 0;
	}
	
	
	public static void main(String args[])
	{
		
	}
	
	
}
