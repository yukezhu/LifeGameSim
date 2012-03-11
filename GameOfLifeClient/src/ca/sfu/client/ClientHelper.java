package ca.sfu.client;

import java.util.ArrayList;
import java.util.List;


public class ClientHelper {

	
	/**
	 * 	@param 	neighbor information
	 *  @return	corresponding neighbor information with respect to the node
	 */
	public static List<Integer> ClientNeighbor(final List<Integer> neighbourInfo)
	{
		List<Integer> info = null;
		
		if(ClientHelper.checkClockwise(0, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(6, 1);
		if(ClientHelper.checkClockwise(1, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(6, 3);
		if(ClientHelper.checkClockwise(2, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(7, 3);
		if(ClientHelper.checkClockwise(1, 2, neighbourInfo))
			info = ClientHelper.fillClockwise(7, 2);
		if(ClientHelper.checkClockwise(0, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(7, 1);
		if(ClientHelper.checkClockwise(1, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(8, 1);
		if(ClientHelper.checkClockwise(3, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(9, 1);
		if(ClientHelper.checkClockwise(4, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(9, 3);
		if(ClientHelper.checkClockwise(5, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(10, 3);
		if(ClientHelper.checkClockwise(3, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(10, 1);
		if(ClientHelper.checkClockwise(4, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(11, 1);
		if(ClientHelper.checkClockwise(4, 2, neighbourInfo))
			info = ClientHelper.fillClockwise(10, 2);
		
		if(ClientHelper.checkClockwise(6, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(0, 1);
		if(ClientHelper.checkClockwise(6, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(1, 1);
		if(ClientHelper.checkClockwise(7, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(2, 1);
		if(ClientHelper.checkClockwise(7, 2, neighbourInfo))
			info = ClientHelper.fillClockwise(1, 2);
		if(ClientHelper.checkClockwise(7, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(0, 3);
		if(ClientHelper.checkClockwise(8, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(1, 3);
		if(ClientHelper.checkClockwise(9, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(3, 1);
		if(ClientHelper.checkClockwise(9, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(4, 1);
		if(ClientHelper.checkClockwise(10, 3, neighbourInfo))
			info = ClientHelper.fillClockwise(5, 1);
		if(ClientHelper.checkClockwise(10, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(3, 3);
		if(ClientHelper.checkClockwise(11, 1, neighbourInfo))
			info = ClientHelper.fillClockwise(4, 3);
		if(ClientHelper.checkClockwise(10, 2, neighbourInfo))
			info = ClientHelper.fillClockwise(4, 2);
		
		return info;
	}
	
	/**
	 * Generate clockwise integers with certain length
	 * @param starting value
	 * @param length of sequences
	 * @return generated sequence
	 */
	private static List<Integer> fillClockwise(int start, int length)
	{
		List<Integer> info = new ArrayList<Integer>();
		int k = start, n = 12;
		for(int i=0; i<length; i++)
		{
			info.add(k);
			k = (k + 1) % n;
		}
		return info;
	}
	
	/**
	 * Check if the sequence is clockwise values
	 * @return true or false
	 */
	private static boolean checkClockwise(int start, int length, final List<Integer> info)
	{
		int n = 12;
		if(info.size() != length) return false;
		int k = start;
		for(int i=0; i<length; i++)
		{
			if(info.get(i) != k) return false;
			k = (k + 1) % n;
		}
		return true;
	}
	
	public static void main(String[] args)
	{
		for(int k = 0; k<12; k++)
		{
			List<Integer> info = new ArrayList<Integer>();
			info.add(k);
			info = ClientHelper.ClientNeighbor(info);
			System.out.print(k + " --> ");
			for(int i=0; i<info.size(); i++)
				System.out.print(info.get(i) + " ");
			System.out.println();
		}
	}
	
}
