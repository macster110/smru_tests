package PAMGuardTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 * Class for calculating errors from localisation data. An error ellipse describes N dimensional error based on a scatter of points or 
 * chi2 surface. The dimensions and rotation of the ellipse describe the distribution of error. Note that although an ellipse will often be a satisfactory
 * description of an error surface, in some cases it will not adequately represent errors. e.g. a linear array produces a doughnut shaped error 
 * surface which would not be described well by an ellipse. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ErrorEllipse {
	
	public static final int PLANE_XY=0; 
	
	public static final int PLANE_ZY=1; 

	public static final int PLANE_ZX=2; 
	
	/**
	 * A 2D projection of the ellipsoid in the XY plane. 
	 * Only usaed if this is a 3D ellipse (ellipsoid). 
	 */
	public ErrorEllipse errorEllipseXY; 
	
	/**
	 * A 2D projection of the ellipsoid in the ZY plane. 
	 * Only usaed if this is a 3D ellipse (ellipsoid). 
	 */
	public ErrorEllipse errorEllipseZY; 

	/**
	 * A 2D projection of the ellipsoid in the ZX plane. 
	 * Only usaed if this is a 3D ellipse (ellipsoid). 
	 */
	public ErrorEllipse errorEllipseZX; 


	/**
	 * For 95% confidence interval is 2.4477
	 */
	double chisquare_val=2.4477; 

	/**
	 * The dimensions of the ellipse/ellipsoid. This is generally a,b for 2D and a, b, c for 3D 
	 * the ellipse/ellipsoid is described by x^2/a + y^2/b+ z^2/c =1. c is -1 if a 2D ellipse. 
	 */
	private double[] ellipseDim; 

	/**
	 * The angle of the ellipsoid in RADIANS. Angles are euler angles and in order heading pitch and roll.  
	 */
	private double[] angles; 


	/**
	 * Generate an error ellipse from a set of points. The dimensions of the error ellipse is by
	 * default the 95% confidence interval of the points. The location of the ellipse is the mean of the points. 
	 * @param points a scatter of points. Can be 2D or 3 set of points. 
	 */
	ErrorEllipse(double[][] points){
		calcErrorEllipse(points);
	}
	
	
	/**
	 * Construct an error ellipse from 3 eigenvectors. 
	 * @param eigenvectors 3 eigenvectors describing the magnitude and direction of the error. 
	 */
	ErrorEllipse(ArrayList<Vector3D> eigenvectors){
		
		
		double[][] eigenVecdbl= new double[3][3];
		for (int i=0; i<eigenVecdbl.length; i++){
			eigenVecdbl[0][i]=eigenvectors.get(i).getX();
			eigenVecdbl[1][i]=eigenvectors.get(i).getY();
			eigenVecdbl[2][i]=eigenvectors.get(i).getZ();
		}
		
		Array2DRowRealMatrix eigenVec=new Array2DRowRealMatrix(eigenVecdbl); 
		
		double[][] eigenValdbl= new double[3][3];
		for (int i=0; i<eigenValdbl.length; i++){
			//this is just because the eigenvectors are compensated to get chi2 value in calcErrorEllipse. This keeps
			//the values of a b and c the szame as the magnitude of the vectors. 
			eigenValdbl[i][i]=Math.pow(eigenvectors.get(i).distance(new Vector3D(0,0,0))/chisquare_val,2);
			//eigenValdbl[i][i]=eigenvectors.get(i).distance(new Vector3D(0,0,0));
		}
		
		Array2DRowRealMatrix eigenVal=new Array2DRowRealMatrix(eigenValdbl); 

		calcErrorEllipse(eigenVal, eigenVec);
		
	}
	
	/**
	 * Create an error ellipse. This can be a 3D or 2D ellipse. For a 2D ellipse, depending on it's orientation one dimension should be -1. 
	 * @param ellipseDim - the dimensions of the ellipse. 
	 * @param angles - the rotation of the ellipse, heading pitch and roll in RADIANS. 
	 */
	ErrorEllipse(double[] ellipseDim, double[] angles){
		this.ellipseDim=ellipseDim;		
		this.angles=angles;
	}


	/**
	 * Calculate the error for a set of points. 
	 * @param points
	 */
	public void calcErrorEllipse(double[][] points){
		if (points==null){
			return; 
		}

		Covariance cov= new Covariance(points, true); 
		
		System.out.println("Coveriance"); 
		printMatrix(cov.getCovarianceMatrix());
		
		//calculate the eigenvectors and eigenvalues 
		EigenDecomposition eign=new EigenDecomposition(cov.getCovarianceMatrix());
		RealMatrix eigenVal = eign.getD();
		RealMatrix eigenVec = eign.getV();
		
		System.out.println("Eigenvalues"); 
		printMatrix(eigenVal); 
	
		System.out.println("Eigenvectors"); 
		printMatrix(eigenVec);

		calcErrorEllipse(eigenVal, eigenVec);
	}
	
	/**
	 * Calculate the error ellipse from eigenvalues and eigenvectors. 
	 * @param eigenVal - matrix the eigenvalues - this is the size of the ellipse
	 * @param eigenVec - matrix eigenvectors- the direction of the ellispe. Not necassarily in order. 
	 */
	private void calcErrorEllipse(RealMatrix eigenVal, RealMatrix eigenVec){
		
		//find the largest eigenvector and eigenvalue and sort in order 
		double[] maxValArray=new double[eigenVal.getColumnDimension()];
		for (int i=0; i<eigenVal.getColumnDimension(); i++){

			//find the largest eigenvalue and it's index in the matrix. 
			double maxEigenVal=Double.MIN_VALUE;

			for (int j=0; j<eigenVal.getRowDimension(); j++){
				double val=eigenVal.getEntry(j, i);
				if (val>maxEigenVal){
					maxEigenVal=val; 
					//maxInd=i; 
				}
			}
			maxValArray[i]=maxEigenVal;
		}
		
		//System.out.println("maxValArray" + maxValArray[0] +" " +maxValArray[1]+" "+maxValArray[2]); 

		//create list of indexes()
		final Integer[] idx = new Integer[maxValArray.length];
		for (int i=0; i<maxValArray.length; i++){
			idx[i]=i; 
		}
		
		//sort array from largest to smallest. 
		//would have been nicer to have done this using the streams API in Java 8 but PG
		//not quite ready for 8 yet. 
		Arrays.sort(idx, new Comparator<Integer>() {
			@Override public int compare(final Integer o1, final Integer o2) {
				return Double.compare(maxValArray[o2], maxValArray[o1]);
			}
		});
		
		//System.out.println("idx " + idx[0] +" " +idx[1]+" "+idx[2]); 

		//now have indexes of largest to smallest eigenvalues. These will make up the error ellipse. 
		double[] first_eignevector=eigenVec.getColumn(idx[0]); //need for direction
		double[] second_eigenvector=eigenVec.getColumn(idx[1]); //need for roll


		/**Size of errors**/

		//work out the shape of the ellipse. This is generally a,b for 2D and a, b, c for 3D 
		// the ellipse/ellipsoid is described by x^2/a + y^2/b+ z^2/c =1
		ellipseDim=new double[maxValArray.length];
		for (int i=0; i<ellipseDim.length; i++){
			ellipseDim[i]=chisquare_val*Math.sqrt(maxValArray[idx[i]]);
		}

		/**Direction of errors**/

		//here's where it gets a bit more complex. For a 2D ellipse only one rotation is needed. 
		//For a 3D ellipse a full set of angles are required. So best to describe with a vector 
		//i.e. a Quaternion as need 4 elements to describe roll. 

		//bit messy here at the moment. 
		double heading=0; 
		double pitch=0; 
		double roll=0; 
		if (maxValArray.length==3){
			heading = Math.atan2(first_eignevector[1], first_eignevector[0]);
			pitch 	= Math.atan2(Math.sqrt((Math.pow(first_eignevector[1],2) +Math.pow(first_eignevector[0],2))), first_eignevector[2])-Math.PI/2;
			roll 	= Math.atan2(Math.sqrt((Math.pow(second_eigenvector[1],2) +Math.pow(second_eigenvector[0],2))), second_eigenvector[2])-Math.PI/2;
		}
		else if (maxValArray.length==2){
			heading = Math.atan2(maxValArray[idx[1]], maxValArray[idx[0]]);
		}

		double[] angles={heading, pitch, roll};
		this.angles=angles; 
		
	}
	
	/**
	 * Get the magnitude of the error in a particular direction. 
	 * @param unitVec - a unit vector (Note: must be a UNIT vector)
	 * @return the magnitude of the error in the direction of the unit vector. 
	 */
	public double getErrorMagnitude(double[] unitVec){
		
			//work out if 2D or 3D ellipse
			boolean threeD=true; 
			if (ellipseDim[2]==-1) threeD=false; //Temp
			
			Rotation rotation;
			Vector3D newPoint; 
			if (threeD){ 
				rotation = new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR, -getAngles()[2],
					-getAngles()[1], -getAngles()[0]);
				newPoint=new Vector3D(unitVec[0], unitVec[1], unitVec[2]);
			}
			else {
				//if 2D need to only rotate by heading and ignore and Z component. 
				rotation = new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR, 0,
					0, -getAngles()[0]);
				newPoint=new Vector3D(unitVec[0], unitVec[1],0);
			}

			newPoint=rotation.applyTo(newPoint);

			//figure out magnitude of vector
			double t=Math.sqrt(1/((Math.pow(newPoint.getX(),2)/Math.pow(ellipseDim[0],2) + Math.pow(newPoint.getY(),2)/Math.pow(ellipseDim[1],2)+ Math.pow(newPoint.getZ(),2)/Math.pow(ellipseDim[2],2))));

			return t; 
	}
	
	/**
	 * Get the intersection point on the surface of the ellipsoid for a vector 
	 * @param intersectionVector - the intersection vector. 
	 * @return the point of intersection. 
	 */
	public double[][] getIntersection(double[] intersectionVector){
		
		//needs to be a unit vector. 
		double magnitude=Math.sqrt(Math.pow(intersectionVector[0], 2)+Math.pow(intersectionVector[1], 2)+Math.pow(intersectionVector[2], 2));
		
		//unit vector. 
		double[] unitVec= {intersectionVector[0]/magnitude, intersectionVector[1]/magnitude, intersectionVector[2]/magnitude};
		
		//calculate the magnitude of the vector. 
		double t= getErrorMagnitude(unitVec);
		 
		//the magnitude multiplied by the vector gives the intersection points. 
		double[][] intersectionPoint = {{unitVec[0]*t, unitVec[1]*t, unitVec[2]*t},
										{-unitVec[0]*t, -unitVec[1]*t, -unitVec[2]*t}};
		
		return intersectionPoint;
		
	}
	
	
	/**
	 * Get the ellipse projected onto a 3D plane. 
	 * @return an array. array [0] is the first radii. array[1] is the second radii. array[2] is the rotation irelative to the plane in radians. 
	 */
	public double[] getErrorEllipse2D(int planeType){
	
		double[] data = new double[3]; 
		switch (planeType){
		case PLANE_XY:
			if (ellipseDim[2]==-1){
				//already have a 2D ellipse so just add angles
				data[0]=ellipseDim[0];
				data[1]=ellipseDim[1];
				data[2]=angles[0];
			}
			else if(errorEllipseXY==null){
				//here we calculate and save the prjection- once saved the projection is never
				//recalculated. 
				errorEllipseXY = calc2DEllipse(planeType);
				data[0]=errorEllipseXY.getEllipseDim()[0];
				data[1]=errorEllipseXY.getEllipseDim()[1];
				data[2]=errorEllipseXY.getAngles()[0]; 

			}
			System.out.println("ErrorEllipse: Herrow: "+ellipseDim[0]+" "+ ellipseDim[1]);

			break;
		case PLANE_ZY:
			//TODO
			break;
		case PLANE_ZX:
			//TODO
			break; 
			default:
			break;
		
		}
		
		return data;
		
	}
	
	private int nAngles=100; 
	
	
	/**
	 * Generate a projection of the ellipse through a plane. . 
	 * @param planeType - the type of plane. PLANE_XY, PLANE_ZY, PLANE_ZX
	 * @return
	 */
	private ErrorEllipse calc2DEllipse(int planeType){

		int dim1=0;
		int dim2=1; 
		switch (planeType){
		case PLANE_XY:
			dim1=0;
			dim2=1;
			break;
		case PLANE_ZY:
			dim1=1;
			dim2=2;
			break;
		case PLANE_ZX:
			dim1=0;
			dim2=2;
			break; 
		}

		//list of unit vectors
		double[][] unitVectors=new double[nAngles][3];

		//range between 0 and 2pi
		double angle; 
		for (int i=0; i<nAngles; i++){
			angle=(Math.PI/nAngles)*i; 
			double[] unitVec=new double[3]; 
			unitVec[dim1]=Math.sin(angle); 
			unitVec[dim2]=Math.cos(angle); 
			unitVectors[i]=unitVec;
		}

		Double max=Double.MIN_VALUE; 
		double magnitude; 
		int ind=-1; 
		for (int i=0; i<unitVectors.length; i++){
			magnitude=getErrorMagnitude(unitVectors[i]); 
			if (magnitude>max){
				max=magnitude; 
				ind=i;
			}
		}

		
		//now have a 2D vector. 
		double ellipseAngle=Math.atan2(unitVectors[ind][dim2], unitVectors[ind][dim1]);
		ellipseAngle = Math.PI/2. - ellipseAngle; //PAMGuard convention for heading angle. 
		double[] angles={ellipseAngle, 0,0}; 

		double[] dim=new double[3];
		dim[dim1]=max;
		double[] orthogUnitVec=new double[3];
		orthogUnitVec[dim1]=-unitVectors[ind][dim2];
		orthogUnitVec[dim2]=unitVectors[ind][dim1];
		dim[dim2]=getErrorMagnitude(orthogUnitVec); 

		ErrorEllipse errorEllipse2D=new ErrorEllipse(dim, angles); 

		return errorEllipse2D; 
	}

	
	/**
	 * 
	 * @return
	 */
	public double[] getEllipseDim() {
		return ellipseDim;
	}


	/**
	 * The euler angles.,
	 * @return
	 */
	public double[] getAngles() {
		return angles;
	}
	
	
	public void printMatrix(RealMatrix m){
	    try{
	        int rows = m.getRowDimension();
	        int columns = m.getColumnDimension();
	        String str = "|\t";

	        for(int i=0;i<rows;i++){
	            for(int j=0;j<columns;j++){
	                str += m.getEntry(i, j) + "\t";
	            }

	            System.out.println(str + "|");
	            str = "|\t";
	        }

	    }catch(Exception e){System.out.println("Matrix is empty!!");}
	}

}