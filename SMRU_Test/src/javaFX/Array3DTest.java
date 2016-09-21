package javaFX;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class Array3DTest extends Application {
	
	
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
   
    /**
     * This is the group which rotates 
     */
	Group root3D;
	
	/**
	 * The camnera to 
	 */
	private Rotate rotateY;
	private Rotate rotateX;
	private Translate translate;
	
	public Array3DTest( ){
	
	
	}
	
	public Group createScene(){
		
        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFarClip(15000);
        camera.setNearClip(0.1);
        camera.setDepthTest(DepthTest.ENABLE);
        camera.getTransforms().addAll(
                rotateY=new Rotate(0, Rotate.Y_AXIS),
                rotateX=new Rotate(0, Rotate.X_AXIS),
                translate=new Translate(250, 250, -1000));
		
		root3D=new Group(); 
		root3D.getChildren().add(camera);
		root3D.setDepthTest(DepthTest.ENABLE);

		
		final PhongMaterial redMaterial = new PhongMaterial();
	       redMaterial.setSpecularColor(Color.ORANGE);
	       redMaterial.setDiffuseColor(Color.RED);		
	       
		for (int i=0; i<50; i++){
			 Shape3D mySphere;
			if (i%2==0)    mySphere = new Box(100,100, 100);
			else mySphere= new Sphere(30);
			mySphere.setTranslateX(Math.random()*500);
			mySphere.setTranslateY(Math.random()*500);
			mySphere.setTranslateZ(Math.random()*200);
			mySphere.setMaterial(redMaterial);
			mySphere.setDepthTest(DepthTest.ENABLE);
	        root3D.getChildren().add(mySphere);
		}

        // Use a SubScene to mix 3D and 3D stuff.        
		//SubScene subScene = new SubScene(root3D, 500,500);   
        SubScene subScene = new SubScene(root3D, 500,500, true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.WHITE);
		subScene.setCamera(camera);
		subScene.setDepthTest(DepthTest.ENABLE);
		Group group = new Group();
	    group.getChildren().add(subScene);
        
        handleMouse(subScene); 

        return group;
        
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
	            	System.out.println("Scroll Event: "+event.getDeltaX() + " "+event.getDeltaY()); 
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

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println(
				"3D supported? " + 
						Platform.isSupported(ConditionalFeature.SCENE3D)
				);  
		Group group=createScene();
		primaryStage.setResizable(false);
		Scene scene = new Scene(group, 500,500, true);
		primaryStage.setScene(scene);
		primaryStage.show();

	}


   
    public static void main(String[] args) {
        launch(args);
    }

}
