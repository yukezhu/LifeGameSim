package ca.sfu.cmpt431.facility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author	Yuke
 * @since	2012/03/07
 */

public class BoardCut {

	/**
	 * @param	Input the large board to be cut	
	 * @return	A pair of vertically cut board (first left, then right) 
	 */
	public static List<Board> verticalCut(final Board b)
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
	 * @param	Input the large board to be cut	
	 * @return	A pair of vertically cut board (first up, then down) 
	 */
	public static List<Board> horizontalCut(final Board b)
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

	public static void main(String argv[])
	{
		Board b = new Board(4, 5);
		Random rand = new Random();
		for(int i=0; i<4; i++){
			for(int j=0; j<5; j++)
			{
				b.bitmap[i][j] = rand.nextBoolean();
				System.out.print(b.bitmap[i][j]);
			}
			System.out.println();
		}

		System.out.println();
		List<Board> list = BoardCut.verticalCut(b);
		for(int i=0; i<list.get(0).height; i++)
		{
			for(int j=0; j<list.get(0).width; j++)
				System.out.print(list.get(0).bitmap[i][j]);
			System.out.println();
		}

		System.out.println();
		for(int i=0; i<list.get(1).height; i++)
		{
			for(int j=0; j<list.get(1).width; j++)
				System.out.print(list.get(1).bitmap[i][j]);
			System.out.println();
		}
	}

}