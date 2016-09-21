package PAMGuardTests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.fxyz.geometry.Point3Dfxy;
import org.fxyz.shapes.Ellipsoid;
import org.fxyz.shapes.composites.PolyLine3D;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class ErrorEllipseTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		 primaryStage.setTitle("Hello World!");
	        StackPane root = new StackPane();
	        root.getChildren().add(new EllipseErrorPane());
	        primaryStage.setScene(new Scene(root, 300, 250));
	        primaryStage.show();
	}
	
	
	public double[][] createErrorData(){
		
		double a=100;
		double b=30;
		double c=15; 
		
		Random fRandom = new Random();
		
		int n=5000;
		double[][] errData=new double[n][3];
		for (int i=0; i<n; i++){
			double[] results={fRandom.nextGaussian() * a, 	fRandom.nextGaussian() * b, fRandom.nextGaussian() * c};
			errData[i]=rotateZ(results, Math.toRadians(40));
			errData[i]=rotateY(errData[i],  Math.toRadians(30)) ;
			errData[i]=rotateX(errData[i],  Math.toRadians(30));
		}
	
		return errData; 
	}
	
	private double[] rotateZ(double[] vector,double angle) {

	      //normalize(vector); // No  need to normalize, vector is already ok...

	      double[] newVector=new double[vector.length]; 

	      newVector[0] 	= (vector[0] * Math.cos(angle) - vector[1] * Math.sin(angle));

	      newVector[1] 	= (vector[0] * Math.sin(angle) + vector[1] * Math.cos(angle));
	      
	      newVector[2]	= vector[2]; 
	      
	      return newVector;

	}
	
	private double[] rotateX(double[] vector,double angle) {

	      //normalize(vector); // No  need to normalize, vector is already ok...

	      double[] newVector=new double[vector.length]; 

	      newVector[0] 	= vector[0];

	      newVector[1] 	= vector[1] * Math.cos(angle) - vector[2] * Math.sin(angle);
	      
	      newVector[2]	=  vector[1] * Math.sin(angle) + vector[2] * Math.cos(angle);
	      
	      return newVector;

	}


	
	private double[] rotateY(double[] vector,double angle) {

	      //normalize(vector); // No  need to normalize, vector is already ok...

	      double[] newVector=new double[vector.length]; 

	      newVector[0] 	= (vector[0] * Math.cos(angle)+vector[2]* Math.sin(angle));

	      newVector[1] 	= vector[1];
	      
	      newVector[2]	= (-vector[0] * Math.sin(angle)+vector[2]* Math.cos(angle));
	      
	      return newVector;

	}
	


	/**
	 * Pane which allows users to visualise a magnetic calibration. 
	 * @author Jamie Macaulay
	 *
	 */
	public class EllipseErrorPane extends BorderPane {

		//keep track of mouse positions
		double mousePosX;
		double mousePosY;
		double mouseOldX;
		double mouseOldY;
		double mouseDeltaX;
		double mouseDeltaY;

		/**
		 * The camera transforms 
		 */
		private Rotate rotateY;
		private Rotate rotateX;
		private Translate translate;
		private Group root3D;
		
		/**
		 * Group containing axis and a reference 
		 */
		private Group axisGroup;
		
		/**
		 * Only show every nth point on the graph- keeps things fast. e..g 4 only show every fourth m3d magnetic point.
		 */
		private int pointFilter=1;

		/**
		 * Button to show compensated values
		 */
		private Button calcCompValues;

		/**
		 * Calibration values 
		 */
		private Label calibrationValues; 

		/**
		 * Current raw magnetometer data.
		 */
		private double[][] magnetomterData;
		
		/**
		 * Contains all data points. 
		 */
		private Group dataPointGroup;
		
		/*
		 *Scaling factor.  
		 */
		private double scaleFactor=1;
		
		/**
		 * Error data 
		 */
		private double[][] errData;
		
		/**
		 * Holds the ellipse.
		 */
		private Group ellipseGroup;
		
		/**
		 * Holds 2D ellipses. 
		 */
		private Group ellipseGroup2D; 
		
		/**
		 * Holds a set of random vectors. 
		 */
		private Group vectorGroup;
		
		/**
		 * The current error ellipse
		 */
		private ErrorEllipse errorEllipse; 


		/**
		 * Create the magnetic calibration pane. 
		 * @param magneticCalibration - magnetic calibration class to calibrate the pane. 
		 */
		public EllipseErrorPane() {
			this.setTop(createTopControls());
			this.setCenter(create3DPane());
		}

		/**
		 * Create top tool bar controls. 
		 * @return top controls.
		 */
		private Pane createTopControls(){
			HBox topControls=new HBox(); 
			topControls.setPadding(new Insets(5,5,5,5));
			topControls.setSpacing(5);
			topControls.setAlignment(Pos.CENTER_LEFT);

			//button to perform extended calibration. 
			Button addErrorData=new Button("Generate Data"); 
			addErrorData.setOnAction((action)->{
				errData= createErrorData(); 
				ellipseGroup.getChildren().clear();
				vectorGroup.getChildren().clear();
				ellipseGroup2D.getChildren().clear(); 

				errorEllipse=null;
				ellipseGroup.getTransforms().clear(); 
				vectorGroup.getTransforms().clear(); 

				addPointData(errData, true);
			});

			//button to perform extended calibration. 
			Button calcEllipse=new Button("Calculate Ellipse"); 
			calcEllipse.setOnAction((action)->{

				ellipseGroup.getChildren().clear();
				ellipseGroup.getTransforms().clear();

				errorEllipse=new ErrorEllipse(errData); 

				System.out.println("Ellipse dim: "+errorEllipse.getEllipseDim()[0]+" "+errorEllipse.getEllipseDim()[1]+" "+errorEllipse.getEllipseDim()[2]);
				System.out.println("Ellipse ang: "+Math.toDegrees(errorEllipse.getAngles()[0])+" "+Math.toDegrees(errorEllipse.getAngles()[1])+" "+Math.toDegrees(errorEllipse.getAngles()[2])); 
				Ellipsoid ellipsoid=new Ellipsoid( 50, errorEllipse.getEllipseDim()[0], errorEllipse.getEllipseDim()[1], errorEllipse.getEllipseDim()[2]); 
				//Ellipsoid ellipsoid=new Ellipsoid( 100, 10, 30, 30); 

				//ellipsoid.setDrawMode(DrawMode.LINE);
				ellipsoid.setDrawMode(DrawMode.LINE);
				ellipsoid.setSpecularColor(Color.AQUA);
				ellipsoid.setDiffuseColor(Color.color(1, 1, 1, 0.5));

				ellipseGroup.getChildren().add(ellipsoid);

				double translationX=0; 
				double translationY=0; 
				double translationZ=0; 

				Translate translate = new Translate(translationX, translationY, translationZ);
				Rotate rotateX = new Rotate(Math.toDegrees(errorEllipse.getAngles()[0]), Rotate.Z_AXIS);
				Rotate rotateY = new Rotate(Math.toDegrees(errorEllipse.getAngles()[1]), Rotate.Y_AXIS);
				Rotate rotateZ = new Rotate(Math.toDegrees(errorEllipse.getAngles()[2]), Rotate.X_AXIS);
				ellipseGroup.getTransforms().addAll(translate, rotateX, rotateY, rotateZ);

			}); 

			//button to perform extended calibration. 
			Button calcRandVector=new Button("Generate Random Vector"); 
			calcRandVector.setOnAction((action)->{

				if (errorEllipse==null) return;

				vectorGroup.getChildren().clear();
				vectorGroup.getTransforms().clear();

				//Create rotation
				Rotation rotation= new Rotation(RotationOrder.ZYX, RotationConvention.VECTOR_OPERATOR, errorEllipse.getAngles()[0],
						errorEllipse.getAngles()[1], errorEllipse.getAngles()[2]);

				Point3Dfxy pOrigin=new Point3Dfxy(0,0,0); 
				for (int i=0; i<50; i++){

					double[] randVector={5*Math.random()-2.5, Math.random()-0.5, Math.random()-0.5};
					double[][] intersectionpoint=errorEllipse.getIntersection(randVector); 
					double[] intersectionPoints1=intersectionpoint[0]; 

					Vector3D newPoint=new Vector3D(intersectionPoints1[0], intersectionPoints1[1], intersectionPoints1[2]);
					//newPoint=rotation.applyTo(newPoint);

					//System.out.println("Intersection point: "+intersectionpoint[0][0]+intersectionpoint[0][1]+intersectionpoint[0][2]);
					Point3Dfxy point3d=new Point3Dfxy((float) newPoint.getX(),(float) newPoint.getY(),(float) newPoint.getZ()); 
					ArrayList<Point3Dfxy> points=new ArrayList<Point3Dfxy>(); 
					points.add(pOrigin);
					points.add(point3d);

					PolyLine3D line=new PolyLine3D(points, 2, Color.RED);
					this.vectorGroup.getChildren().add(line);

				}

				/**This shows correct rotations:**/
				//	            Rotate rotateX = new Rotate(Math.toDegrees(errorEllipse.getAngles()[0]), Rotate.Z_AXIS); 
				//	            Rotate rotateY = new Rotate(Math.toDegrees(errorEllipse.getAngles()[1]), Rotate.Y_AXIS);
				//	            Rotate rotateZ = new Rotate(Math.toDegrees(errorEllipse.getAngles()[2]), Rotate.X_AXIS);
				//	            vectorGroup.getTransforms().addAll(rotateX, rotateY, rotateZ);


			}); 
			
			//button to perform extended calibration. 
			Button generateXYEllipse=new Button("Generate XY Projection"); 
			generateXYEllipse.setOnAction((action)->{
				
				if (errorEllipse!=null){
					double[] ellipse2D=errorEllipse.getErrorEllipse2D(ErrorEllipse.PLANE_XY);
					for (int i=0; i<ellipse2D.length; i++){
						System.out.println("2D Ellipse: "+ellipse2D[i]);
					}
					//ellipseGroup2D.getChildren().add(new Polyline ); 
				}
			
			}); 

			topControls.getChildren().addAll(addErrorData, calcEllipse, calcRandVector, generateXYEllipse); 

			return topControls; 
		}

		/**
		 * 3D pane which allow users to visualise calibration measurements. 
		 * @return 3D pane which shows magnetic measurements and ellipse calibration. 
		 */
		private Pane create3DPane(){

			Pane pane3D=new Pane(); 

			// Create and position camera
			PerspectiveCamera camera = new PerspectiveCamera(true);
			camera.setFarClip(15000);
			camera.setNearClip(0.1);
			camera.setDepthTest(DepthTest.ENABLE);
			camera.getTransforms().addAll (
					rotateY=new Rotate(-45, Rotate.Y_AXIS),
					rotateX=new Rotate(-45, Rotate.X_AXIS),
					translate=new Translate(0, 0, -1000));

			//create main 3D group 
			root3D=new Group();

			//group for raw magnetic measurements 
			dataPointGroup=new Group(); 
			//group for calibrated measurments, 
			//		axisGroup=Array3DPane.buildAxes(100, Color.RED, Color.SALMON, Color.BLUE, Color.CYAN, Color.LIMEGREEN, Color.LIME, Color.WHITE);
			axisGroup=buildAxes(100, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
			
			//group for ellipsoid
			ellipseGroup=new Group(); 
			vectorGroup = new Group(); 
			ellipseGroup2D=new Group(); 

	        PointLight light = new PointLight(Color.WHITE);
	        light.setTranslateX(-500);
	        light.setTranslateY(-500);
	        light.setTranslateZ(-500);
	
			root3D.getChildren().addAll(dataPointGroup, ellipseGroup, vectorGroup, axisGroup, light);


			//Use a SubScene to mix 3D and 2D stuff.        
			//note- make sure depth buffer in sub scene is enabled. 
			SubScene subScene = new SubScene(root3D, 500,500, true, SceneAntialiasing.BALANCED);
			subScene.widthProperty().bind(this.widthProperty());
			subScene.heightProperty().bind(this.heightProperty());
			subScene.setDepthTest(DepthTest.ENABLE);

			subScene.setFill(Color.BLACK);
			subScene.setCamera(camera);

			//handle mouse events for sub scene
			handleMouse(subScene); 

			//create new group to add sub scene to 
			Group group = new Group();
			group.getChildren().add(subScene);

			//add group to window.
			pane3D.getChildren().add(group);
			pane3D.setDepthTest(DepthTest.ENABLE);

			return pane3D;
		}

		/**
		 * Create a mesh sphere for reference. 
		 * @return mesh sphere for reference. 
		 */
		public Sphere createAxisSphere(){
			Sphere sphere=new Sphere(35);
			PhongMaterial material=new PhongMaterial(); 
			material.setSpecularColor(Color.WHITE);
			material.setDiffuseColor(Color.WHITE);
			sphere.setMaterial(material);
			sphere.setDrawMode(DrawMode.LINE);
			return sphere;
		}

		/**
		 * Get the index of a sorted array
		 * @author Jamie Macaulay (from http://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting)
		 *
		 */
		public class ArrayIndexComparator implements Comparator<Integer>
		{
			private final double[] array;

			public ArrayIndexComparator(double[] array)
			{
				this.array = array;
			}

			public Integer[] createIndexArray()
			{
				Integer[] indexes = new Integer[array.length];
				for (int i = 0; i < array.length; i++)
				{
					indexes[i] = i; // Autoboxing
				}
				return indexes;
			}

			@Override
			public int compare(Integer index1, Integer index2)
			{
				// Autounbox from Integer to int to use as array indexes
				return (int) (array[index1]-array[index2]);
			}
		}

		/**
		 * Add raw magnetometer data to the graph. 
		 * @param magnetomterData - magnetomer data - three dimensions.
		 * @param remove - true to remove all previous data. False keeps data. 
		 */
		private  void addPointData(double[][] magnetomterData, boolean remove){

			if (remove) dataPointGroup.getChildren().removeAll(dataPointGroup.getChildren());

			//find max value 
			double max=Double.MIN_VALUE;
			for (int i=0; i<magnetomterData.length; i++){
				if (max<Math.abs(magnetomterData[i][0])){
					max=Math.abs(magnetomterData[i][0]);
				}
			}

			//Add Data
			//JavaFX 8 way
			Sphere sphere;
			for (int i=0; i<magnetomterData.length; i=i+pointFilter){
				sphere=new Sphere(1); 

				sphere.setTranslateX(magnetomterData[i][0]*scaleFactor);
				sphere.setTranslateY(magnetomterData[i][1]*scaleFactor);
				sphere.setTranslateZ(magnetomterData[i][2]*scaleFactor);

				PhongMaterial material=new PhongMaterial(); 
				material.setDiffuseColor(new Color(0, 1-Math.abs(magnetomterData[i][0]/max),  Math.abs(magnetomterData[i][0]/max), 1));
				sphere.setMaterial(material);

				dataPointGroup.getChildren().add(sphere);
			}

			//FXYZ way 
			//		ArrayList<Double> pointX=new ArrayList<Double>();
			//		ArrayList<Double> pointY=new ArrayList<Double>();
			//		ArrayList<Double> pointZ=new ArrayList<Double>();
			//		ArrayList<Color> colours=new ArrayList<Color>();
			//		for (int i=0; i<magnetomterData.length; i=i+pointFilter){
			//			pointX.add(magnetomterData[i][0]*100);
			//			pointY.add(magnetomterData[i][1]*100);
			//			pointZ.add(magnetomterData[i][2]*100);
			//			colours.add(new Color(0, 1-Math.abs(magnetomterData[i][0]/max),  Math.abs(magnetomterData[i][0]/max), 1));
			//		}
			//		
			//		ScatterPlot scatterPlot=new ScatterPlot(100,1,true); 
			//		
			//		scatterPlot.setXYZData(pointX, pointY, pointZ,colours);
			//		magneticMeasurments.getChildren().add(scatterPlot);
		}




		private void handleMouse(SubScene scene) {

			scene.setOnMousePressed(new EventHandler<MouseEvent>() {

				@Override public void handle(MouseEvent me) {
					mousePosX = me.getSceneX();
					mousePosY = me.getSceneY();
					mouseOldX = me.getSceneX();
					mouseOldY = me.getSceneY();
				}
			});

			scene.setOnScroll(new EventHandler<ScrollEvent>() {
				@Override public void handle(ScrollEvent event) {
					//System.out.println("Scroll Event: "+event.getDeltaX() + " "+event.getDeltaY()); 
					translate.setZ(translate.getZ()+  event.getDeltaY() *0.001*translate.getZ());   // + 
				}
			});


			scene.setOnMouseDragged(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent me) {
					mouseOldX = mousePosX;
					mouseOldY = mousePosY;
					mousePosX = me.getSceneX();
					mousePosY = me.getSceneY();
					mouseDeltaX = (mousePosX - mouseOldX);
					mouseDeltaY = (mousePosY - mouseOldY);

					double modifier = 1.0;
					double modifierFactor = 0.1;

					if (me.isControlDown()) {
						modifier = 0.1;
					}
					if (me.isShiftDown()) {
						modifier = 10.0;
					}
					if (me.isPrimaryButtonDown()) {
						rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0);  // +
						rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0);  // -
					}
					if (me.isSecondaryButtonDown()) {
						translate.setX(translate.getX() -mouseDeltaX * modifierFactor * modifier * 5);
						translate.setY(translate.getY() - mouseDeltaY * modifierFactor * modifier * 5);   // +
					}


				}
			});
		}
		
	}

	/**
	 * Create a 3D axis. 
	 * @param- size of the axis
	 */
	public static Group buildAxes(double axisSize, Color xAxisDiffuse, Color xAxisSpectacular,
			Color yAxisDiffuse, Color yAxisSpectacular,
			Color zAxisDiffuse, Color zAxisSpectacular,
			Color textColour) {
		Group axisGroup=new Group(); 
		double length = 2d*axisSize;
		double width = axisSize/100d;
		double radius = 2d*axisSize/100d;
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(xAxisDiffuse);
		redMaterial.setSpecularColor(xAxisSpectacular);
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(yAxisDiffuse);
		greenMaterial.setSpecularColor( yAxisSpectacular);
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(zAxisDiffuse);
		blueMaterial.setSpecularColor(zAxisSpectacular);

		Text xText=new Text("x"); 
		xText.setStyle("-fx-font: 20px Tahoma;");
		xText.setFill(textColour);
		Text yText=new Text("y"); 
		yText.setStyle("-fx-font: 20px Tahoma; ");
		yText.setFill(textColour);
		Text zText=new Text("z"); 
		zText.setStyle("-fx-font: 20px Tahoma; ");
		zText.setFill(textColour);

		xText.setTranslateX(axisSize+5);
		yText.setTranslateY((axisSize+5));
		zText.setTranslateZ(axisSize+5);

		Sphere xSphere = new Sphere(radius);
		Sphere ySphere = new Sphere(radius);
		Sphere zSphere = new Sphere(radius);
		xSphere.setMaterial(redMaterial);
		ySphere.setMaterial(greenMaterial);
		zSphere.setMaterial(blueMaterial);

		xSphere.setTranslateX(axisSize);
		ySphere.setTranslateY(axisSize);
		zSphere.setTranslateZ(axisSize);

		Box xAxis = new Box(length, width, width);
		Box yAxis = new Box(width, length, width);
		Box zAxis = new Box(width, width, length);
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		axisGroup.getChildren().addAll(xText, yText, zText);
		axisGroup.getChildren().addAll(xSphere, ySphere, zSphere);
		return axisGroup;
	}
	

	public static void main(String[] args) {
		launch(args);
	}

}
