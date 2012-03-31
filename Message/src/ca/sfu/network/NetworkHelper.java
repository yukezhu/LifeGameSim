package ca.sfu.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Network helper class
 * @author	Yuke Zhu
 * @since	2012/03/31
 */

public class NetworkHelper {

	/**
	 * Get static Ip for local host
	 * The function only works in Linux/Unix system with ifconfig
	 * It will return null in other operating systems 
	 * @return	static ip in string format
	 */
	public static String getHostStaticIp()
	{
		boolean en0 = false;
		try {
			Process p = Runtime.getRuntime().exec("ifconfig");
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());  
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr, ipAddr = null;
			// get command line output
			while ((lineStr = inBr.readLine()) != null)  
			{
				if(lineStr.startsWith("en0")) en0 = true;
				if(lineStr.trim().startsWith("inet ") && en0)
				{
					ipAddr = lineStr.trim().substring(5);
					ipAddr = ipAddr.substring(0, ipAddr.indexOf(' ')).trim();
				}
			}
			if (p.waitFor() != 0) {  
				if (p.exitValue() == 1) // p.exitValue() == 0 success, 1£ºexception  
					throw new java.lang.RuntimeException();
			}  
			inBr.close();  
			in.close();  
			return ipAddr;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return null;
	}

}
