package ca.sfu.cmpt431.facility;

import java.util.Random;

public class BoardOperation {

	public static Board NextMoment(Board b, boolean[] up, boolean[] down, boolean[] left, boolean[] right) throws IllegalArgumentException
	{
		IllegalArgumentException exception = new java.lang.IllegalArgumentException();
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

		//		auto1.width = width - width/2; //what if width is odd
		//		auto1.height = height;
		//		int offset = width/2;
		//
		//		auto1.bitmap = new int[auto1.height][auto1.width];
		//		for(int i=0; i<auto1.height; i++)
		//			for(int j=0; j<auto1.width; j++){
		//				System.out.println(i+" "+j);
		//				auto1.bitmap[i][j] = bitmap[i][offset+j];
		//			}
		//		return auto1;
		return null;
	}

	/**
	 * @param 	Input board
	 * @param 	Dense factor ranged from [0, 1) --> More dense, More cells
	 * @return	Randomly filled board
	 */
	public static Board Randomize(Board b, double dense)
	{
		Random random = new Random();
		for(int i=0; i<b.height; i++)
			for(int j=0; j<b.width; j++)
			{
				double sample = random.nextDouble();
				if(sample < dense)
					b.bitmap[i][j] = true;
				else
					b.bitmap[i][j] = false;
			}
		return b;
	}

	public static void main(String args[])
	{

	}


}
