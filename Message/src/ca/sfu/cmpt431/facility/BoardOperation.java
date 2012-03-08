package ca.sfu.cmpt431.facility;

import java.util.Random;

public class BoardOperation {

	public static Board NextMoment(Board b, boolean[] up, boolean[] down, boolean[] left, boolean[] right,
			boolean upperLeft, boolean upperRight, boolean lowerLeft, boolean lowerRight) throws IllegalArgumentException
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

		int height = b.height, width = b.width;
		boolean[][] prebitmap = new boolean[height+2][width+2]; 
		final int[][] move = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

		for(int i=1; i <= height; i++)
			for(int j=1; j <= width; j++)
				prebitmap[i][j] = b.bitmap[i][j];
		for(int j=1; j <= width; j++)
		{
			prebitmap[0][j] = up[j];
			prebitmap[height+1][j] = down[j];
		}
		for(int i=1; i <= height; i++)
		{
			prebitmap[i][0] = left[i];
			prebitmap[i][width+1] = right[i];
		}
		prebitmap[0][0] = upperLeft;
		prebitmap[0][width+1] = upperRight;
		prebitmap[height+1][0] = lowerLeft;
		prebitmap[height+1][width+1] = lowerRight;

		for(int i=1; i<=height; i++)
			for(int j=1; j<=width; j++)
			{
				int counter = 0;
				for(int k=0; k<8; k++)
				{
					int x = i + move[k][0], y = j + move[k][1];
					if(prebitmap[x][y])
					{
						counter ++;
					}
				}
				if(counter == 3)
				{
					b.bitmap[i-1][j-1] = true;
				}else if(counter != 2)
				{
					b.bitmap[i-1][j-1] = false;
				}
			}
		return b;
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

	/**
	 * Print the board to command line
	 * @param Board to be printed
	 */
	public static void Print(final Board b)
	{
		for(int i=0; i<b.height; i++)
		{
			for(int j=0; j<b.width; j++)
			{
				if(b.bitmap[i][j])
					System.out.print(' ');
				else
					System.out.print('@');
			}
			System.out.println();
		}
	}
	
	public static void main(String args[])
	{
		Board b = new Board(5, 5);
		b = BoardOperation.Randomize(b, 0.4);
		BoardOperation.Print(b);
	}


}
