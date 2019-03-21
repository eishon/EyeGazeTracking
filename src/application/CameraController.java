package application;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class CameraController {
	
	@FXML
	private Button startCameraBtn;
	@FXML
	private ImageView currentFrame;
	@FXML
	private CheckBox mirror;
	@FXML
	private CheckBox debug;
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive = false;
	// the id of the camera to be used
	private static int cameraId = 0;
	
	private Image imageToShow;
	private Mat frame;
	
	private static int width = 0;
	private static int height = 0;
	
	public Mat getFrame(){
		return frame;
	}
	
	@FXML
	protected void startCamera() throws IOException
	{
		if (!this.cameraActive) {
			startResult();
			startProcessing();
			startLeftRightEye();
		}
		
		mirror.setSelected(true);
		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run() {
						// effectively grab and process a single frame
						frame = grabFrame();
						getImageSize(frame);
						OpenCVController.setFrame(frame);
						// convert and show the frame
						imageToShow = OpenCVController.mat2Image(frame);
						updateImageView(currentFrame, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.startCameraBtn.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.startCameraBtn.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
	}
	
	private Mat grabFrame() {
		// init everything
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty()) {
					if (mirror.isSelected()) {
						Core.flip(frame, frame, 1);
					}
				}
				
			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return frame;
	}
	
	private static void getImageSize(Mat img){
		if (width != img.cols()) {
			width = img.cols();
			height = img.rows();
			System.out.println("Captured Image Size: "+width+" x "+height);
		}
	}
	
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
	
	public void startProcessing() throws IOException{
		Stage processingStage = new Stage();
		processingStage.setTitle("Processing");
		
    	FXMLLoader processingLoader = new FXMLLoader(getClass().getResource("Processing.fxml"));
		BorderPane mainLayout = processingLoader.load();
		
		Scene scene = new Scene(mainLayout);
		processingStage.setScene(scene);
		processingStage.setOnCloseRequest(evt -> {
			
			processingStage.close();
		});
		OpenCVController.processingLoader = processingLoader;
		processingStage.show();
		processingLoader.<ProcessingController>getController().init();
	}
	
	public void startResult() throws IOException{
		Stage resultStage = new Stage();
		resultStage.setTitle("Result");
		
		FXMLLoader resultLoader = new FXMLLoader(getClass().getResource("Result.fxml"));
		BorderPane mainLayout = resultLoader.load();
		
		//mainLayout.setPrefSize(OpenCVController.screenX/2, OpenCVController.screenY/2);
		
		Scene scene = new Scene(mainLayout);
		resultStage.setScene(scene);
		resultStage.setOnCloseRequest(evt -> {
			
			resultStage.close();
		});
		OpenCVController.resultLoader = resultLoader;
		resultStage.show();
		resultLoader.<ResultController>getController().init(resultStage);
	}
	
	public void startLeftRightEye() throws IOException{
		Stage leftEyeStage = new Stage();
		Stage rightEyeStage = new Stage();
		leftEyeStage.setTitle("LEFT");
		rightEyeStage.setTitle("RIGHT");
		
		FXMLLoader leftEyeLoader = new FXMLLoader(getClass().getResource("LeftEyePos.fxml"));
		FXMLLoader rightEyeLoader = new FXMLLoader(getClass().getResource("RightEyePos.fxml"));
		BorderPane leftLayout = leftEyeLoader.load();
		BorderPane rightLayout = rightEyeLoader.load();
		
		Scene lScene = new Scene(leftLayout);
		leftEyeStage.setScene(lScene);
		leftEyeStage.setOnCloseRequest(evt -> {
			
			leftEyeStage.close();
		});
		OpenCVController.leftEyeLoader = leftEyeLoader;
		leftEyeStage.show();
		leftEyeLoader.<ResultController>getController().initLeft(leftEyeStage);
		
		Scene rScene = new Scene(rightLayout);
		rightEyeStage.setScene(rScene);
		rightEyeStage.setOnCloseRequest(evt -> {
			
			rightEyeStage.close();
		});
		OpenCVController.rightEyeLoader = rightEyeLoader;
		rightEyeStage.show();
		rightEyeLoader.<ResultController>getController().initRight(rightEyeStage);
	}
	
	private void updateImageView(ImageView view, Image image)
	{
		OpenCVController.onFXThread(view.imageProperty(), image);
	}
	
	protected void setClosed()
	{
		this.stopAcquisition();
	}
}
