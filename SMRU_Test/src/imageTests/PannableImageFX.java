package imageTests;

import java.io.File;
import java.net.MalformedURLException;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class PannableImageFX extends Application {
	
	
		public static final int MIN_PIXELS=10; 

	    // Some large image in the order of 4000x3000 pixels.
	    private final static File file = new File("C:\\Users\\macst\\OneDrive\\Pictures\\Mountains1\\P1040400.jpg");

	    ScrollPane scrollPane;

		private BorderPane borderPane;
		
		public PannableImageFX(){
			borderPane= new BorderPane(); 
			Image image = null ; 
            try {
				image = new Image(file.toURI().toURL().toExternalForm());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            setImage(image) ;
			
		}
		
	    
	    private void setImage(Image selectedImage) {
//	        Image selectedImage = new ImageService().getObjectByID(DBResource.Image.SELECT_BY_ID,
//	                imageId);

	        ImageView imageView = setImageView(selectedImage);

	        //zoom
	        double width = imageView.getImage().getWidth();
	        double height = imageView.getImage().getHeight();

	        imageView.setPreserveRatio(true);
	        reset(imageView, width, height);

	        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

	        imageView.setOnMousePressed(e -> {
	            Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
	            mouseDown.set(mousePress);
	        });

	        imageView.setOnMouseDragged(e -> {
	            Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
	            shift(imageView, dragPoint.subtract(mouseDown.get()));
	            mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
	        });

	        imageView.setOnScroll(e -> {
	            double delta = e.getDeltaY();
	            Rectangle2D viewport = imageView.getViewport();

	            double scale = clamp(Math.pow(1.01, delta),

	                    // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
	                    Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

	                    // don't scale so that we're bigger than image dimensions:
	                    Math.max(10*width / viewport.getWidth(), 10*height / viewport.getHeight())

	            );

	            Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

	            double newWidth = viewport.getWidth() * scale;
	            double newHeight = viewport.getHeight() * scale;

	            // To keep the visual point under the mouse from moving, we need
	            // (x - newViewportMinX) / (x - currentViewportMinX) = scale
	            // where x is the mouse X coordinate in the image

	            // solving this for newViewportMinX gives

	            // newViewportMinX = x - (x - currentViewportMinX) * scale

	            // we then clamp this value so the image never scrolls out
	            // of the imageview:

	            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
	                    0, width - newWidth);
	            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
	                    0, height - newHeight);

	            imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
	        });

	        imageView.setOnMouseClicked(e -> {
	            if (e.getClickCount() == 2) {
	                reset(imageView, width, height);
	            }
	        });

	        imageView.setPreserveRatio(true);
	        borderPane.setCenter(imageView);

	        imageView.fitWidthProperty().bind(borderPane.widthProperty());
	        imageView.fitHeightProperty().bind(borderPane.heightProperty());
	    }

	    // reset to the top left:
	    private void reset(ImageView imageView, double width, double height) {
	        imageView.setViewport(new Rectangle2D(0, 0, width, height));
	    }

	    // shift the viewport of the imageView by the specified delta, clamping so
	    // the viewport does not move off the actual image:
	    private void shift(ImageView imageView, Point2D delta) {
	        Rectangle2D viewport = imageView.getViewport();

	        double width = imageView.getImage().getWidth() ;
	        double height = imageView.getImage().getHeight() ;

	        double maxX = width - viewport.getWidth();
	        double maxY = height - viewport.getHeight();

	        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
	        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

	        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
	    }

	    private double clamp(double value, double min, double max) {

	        if (value < min)
	            return min;
	        if (value > max)
	            return max;
	        return value;
	    }

	    // convert mouse coordinates in the imageView to coordinates in the actual image:
	    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
	        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
	        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

	        Rectangle2D viewport = imageView.getViewport();
	        return new Point2D(
	                viewport.getMinX() + xProportion * viewport.getWidth(),
	                viewport.getMinY() + yProportion * viewport.getHeight());
	    }

	    private ImageView setImageView(Image image) {

	        ImageView imageView = new ImageView(image);
	       // imageView.setImage(new javafx.scene.image.Image("file:" + image.getAbsPathI()));

	        double w;
	        double h;

	        double ratioX = imageView.getFitWidth() / imageView.getImage().getWidth();
	        double ratioY = imageView.getFitHeight() / imageView.getImage().getHeight();

	        double reducCoeff;
	        if(ratioX >= ratioY) {
	            reducCoeff = ratioY;
	        } else {
	            reducCoeff = ratioX;
	        }

	        w = imageView.getImage().getWidth() * reducCoeff;
	        h = imageView.getImage().getHeight() * reducCoeff;

	        imageView.setX((imageView.getFitWidth() - w) / 2);
	        imageView.setY((imageView.getFitHeight() - h) / 2);

	        return imageView;
	    }
	    
	   	    

	    @Override
	    public void start(Stage primaryStage) {
	        BorderPane root = new BorderPane();
			
			root.setPrefSize(1200, 800);
			root.setCenter(borderPane);

			primaryStage.setScene(new Scene(root));
			primaryStage.show();
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }

}
