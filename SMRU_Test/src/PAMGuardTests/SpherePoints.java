package PAMGuardTests;

import java.util.ArrayList;
import java.util.List;

public class SpherePoints {
	
	/**
	 * Copy of the linspace function from MATLAB 
	 * (generates linearly spaced vectors. It is similar to the colon operator ":" but gives direct control over the number of points and always includes the endpoints.)
	 * @param start - the first value
	 * @param stop - the end value
	 * @param n - the number of evenly spaced points to return
	 * @return a list of evenly spaced points. 
	 */
	public static List<Double> linspace(double start, double stop, int n)
	{
	   List<Double> result = new ArrayList<Double>();

	   double step = (stop-start)/(n-1);

	   for(int i = 0; i <= n-2; i++)
	   {
	       result.add(start + (i * step));
	   }
	   result.add(stop);

	   return result;
	}
	
	
	/**
	 * Get evenly spaced points of the surface of a sphere. These can be normalised for evenly spaced vectors. 
	 * Uses a spiral algorithm and golden ratio. 
	 * <br>
	 * See http://blog.marmakoide.org/?p=1 (accessed 30/06/2016) for algorithm. 
	 * @param n The number of points to scater on the surface of the sphere. 
	 * @param r the radius of the sphere.  
	 * @param an array of evenly spaced points on the surface of the sphere. 
	 */
	public static double[][] getSpherePoints(int n, double r){

		//golden_angle = pi * (3 - sqrt(5));
		double goldenAngle= Math.PI*( 0.7639);

		double[][] points=new double[n][3]; 

		List<Double> zLin=linspace(1 - 1.0 / n,  1.0 / n - 1,  n);
		double z;
		double theta;
		double radius;
		for (int i=0; i<n ;i++){					
			z=zLin.get(i);
			radius=Math.sqrt(1-z*z); 
			theta= goldenAngle*i;

			points[i][0]=r*radius* Math.cos(theta);
			points[i][1]=r*radius*Math.sin(theta);
			points[i][2]=r*z; 
		}
		
		return points; 
	}
	
	public static void main(String[] args) {
		double[][] points=getSpherePoints(256, 1);
		
		for (int i=0; i<points.length; i++){
			System.out.println(points[i][0] +" "+points[i][1] +" "+points[i][2]);
		}
		
		
	}


}
