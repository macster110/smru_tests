package OpenTag;

import java.util.ArrayList;

/**
 * Holds binary data from OpenTag DSG  file. 
 * @author Jamie Macaulay 
 *
 */
public class OpenTagData {
	
	/**Header**/
	public long version; 
	
	public long userID;
	
	public int sec;
	
	public int min;
	
	public int hour;
	
	public int day;
	
	public int mday;
	
	public int month;
	
	public int year; 
	
	public int timezone; 
	
	/**Header OpenTag2- Can be empty**/
	public float lat; 
	
	public float lon;
	
	public float depth; 
	
	public float DSGCal; 
	
	public float hydroCal; 
	
	public float lpFilt;
	
	/** Spec data for each sensor**/
	public ArrayList<SidSpec> sidSpec=new ArrayList<SidSpec>(); 
	
	/**
	 * Holds specification data for a single sensor- e.g. accelerometer. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class SidSpec {
		
		public int SID;
		
		public long  nBytes; 
		
		public long numChan; 
		
		public long storeType; 
		
		public long sensorType;
		
		public long DForm; 
		
		public long SPus; 
		
		public long RECPTS; 

		public long RECINT; 
		
	};
	
	/**
	 * Holds data collected by OpenTag.
	 * @author Jamie Macaulay
	 *
	 */
	public class SidRec {
		
		public int nSID;
		
		public long  chan; 
		
		public long nbytes; 
		
		//version >9999
		public long nbytes_2; 
		
		public int[] data; 
		
	};
	
	
	
	
	
	
	
	
	

}
