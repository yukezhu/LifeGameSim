package ca.sfu.cmpt431.facility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author	Yuke Zhu
 * @since	2012/03/07
 */

public class BoardOperation {

	/**
	 * Return the next state of the cellular automata
	 * @param up, down		Upper border, Lower border
	 * @param left, right	Left border, Right border
	 * @param upperLeft		b[0][0]
	 * @param upperRight	b[0][width+1]
	 * @param lowerLeft		b[height+1][0]
	 * @param lowerRight	b[height+1][width+1]
	 * @return the automata with next state
	 */
	public static Board NextMoment(Board b, boolean[] up, boolean[] down, boolean[] left, boolean[] right, boolean upperLeft, boolean upperRight, boolean lowerLeft, boolean lowerRight) throws IllegalArgumentException
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
				prebitmap[i][j] = b.bitmap[i-1][j-1];
		for(int j=1; j <= width; j++)
		{
			prebitmap[0][j] = up[j-1];
			prebitmap[height+1][j] = down[j-1];
		}
		for(int i=1; i <= height; i++)
		{
			prebitmap[i][0] = left[i-1];
			prebitmap[i][width+1] = right[i-1];
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
					System.out.print('@');
				else
					System.out.print(' ');
			}
			System.out.println();
		}
	}

	/**
	 * Merge the smaller board to the larger board at certain position
	 */
	public static void Merge(Board bigBoard, Board smallBoard, int top, int left){
		for(int i=top; i<top+smallBoard.height; i++)
			for(int j=left; j<left+smallBoard.width; j++){
				bigBoard.bitmap[i][j] = smallBoard.bitmap[i-top][j-left];
			}
	}
	
	/**
	 * Evenly vertical cut of the board
	 * @param	Input the large board to be cut	
	 * @return	A pair of vertically cut board (first left, then right) 
	 */
	public static List<Board> VerticalCut(final Board b)
	{
		int height = b.height, width = b.width;
		Board left = new Board(height, width / 2);
		left.bitmap = new boolean[height][width/2];
		for(int i=0; i < left.height; i++)
			for(int j=0; j < left.width; j++)
				left.bitmap[i][j] = b.bitmap[i][j];

		Board right = new Board(height, width - width / 2);
		int offset = width / 2;

		right.bitmap = new boolean[right.height][right.width];
		for(int i=0; i<right.height; i++)
			for(int j=0; j<right.width; j++){
				right.bitmap[i][j] = b.bitmap[i][offset+j];
			}

		List<Board> list = new ArrayList<Board>();
		list.add(left);
		list.add(right);
		return list;
	}

	/**
	 * Evenly horizontal cut of the board
	 * @param	Input the large board to be cut	
	 * @return	A pair of vertically cut board (first up, then down) 
	 */
	public static List<Board> HorizontalCut(final Board b)
	{
		int height = b.height, width = b.width;
		Board up = new Board(height / 2, width);
		up.bitmap = new boolean[height / 2][width];
		for(int i=0; i < up.height; i++)
			for(int j=0; j < up.width; j++)
				up.bitmap[i][j] = b.bitmap[i][j];

		Board down = new Board(height - height / 2, width);
		int offset = height / 2;

		down.bitmap = new boolean[down.height][down.width];
		for(int i=0; i<down.height; i++)
			for(int j=0; j<down.width; j++){
				down.bitmap[i][j] = b.bitmap[offset+i][j];
			}

		List<Board> list = new ArrayList<Board>();
		list.add(up);
		list.add(down);
		return list;
	}

	public static void main(String args[])
	{
		Board b = new Board(5, 5);
		b = BoardOperation.Randomize(b, 0.3);
		BoardOperation.Print(b);
		boolean[] up = new boolean[5];
		boolean[] down = new boolean[5];
		boolean[] left = new boolean[5];
		boolean[] right = new boolean[5];
		int n = 0;
		while(n < 10)
		{
			b = BoardOperation.NextMoment(b, up, down, left, right, false, false, false, false);
			BoardOperation.Print(b);
			n ++;
			System.out.println();
		}
	}

}
