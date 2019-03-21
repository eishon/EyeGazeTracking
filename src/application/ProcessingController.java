package application;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ProcessingController {
	Thread trainingThread;
	
	public Stage trainingStage;
	
	@FXML
	private ImageView faceImageView;
	@FXML
	private ImageView leftEyeImageView;
	@FXML
	private ImageView rightEyeImageView;
	@FXML
	private ImageView leftBinaryImageView;
	@FXML
	private ImageView rightBinaryImageView;
	@FXML
	private ImageView leftPointerImageView;
	@FXML
	private ImageView rightPointerImageView;
	
	@FXML
	public Slider hueMinSlider;
	@FXML
	public Slider hueMaxSilder;
	@FXML
	public Slider saturationMinSlider;
	@FXML
	public Slider saturationMaxSilder;
	@FXML
	public Slider valueMinSlider;
	@FXML
	public Slider valueMaxSilder;
	@FXML
	public Slider brightnesSlider;
	@FXML
	private Label hsvValueLabel;
	
	@FXML
	private Button trainingBtn;
	@FXML
	private Button startBtn;
	
	@FXML
	private CheckBox grayProcess;
	@FXML
	private CheckBox mouseControl;
	
	@FXML
	private Label trainingTargetLabel;
	private ObjectProperty<String> trainingValuesProps;
	
	@FXML
	private void training(){
		if(OpenCVController.trainingFlag){
			trainingBtn.setDisable(false);
			OpenCVController.trainingFlag = false;
		}else{
			OpenCVController.resetTrainingVals();
			TrainingStart();
			trainingBtn.setDisable(true);
			OpenCVController.trainingFlag = true;
		}
	}
	
	@FXML
	private void start(){
		
	}
	
	public boolean getGrayProcessState(){
		return grayProcess.isSelected();
	}
	
	public boolean getMouseControlState(){
		return mouseControl.isSelected();
	}
	
	private ObjectProperty<String> hsvValuesProp;
	
	public void init() throws IOException{
		hsvValuesProp = new SimpleObjectProperty<>();
		this.hsvValueLabel.textProperty().bind(hsvValuesProp);
		startTrainingTargetStage();
	}
	
	public void setFace(Mat img){
		OpenCVController.onFXThread(faceImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	public void setLeftEye(Mat img){
		OpenCVController.onFXThread(leftEyeImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	public void setRightEye(Mat img){
		OpenCVController.onFXThread(rightEyeImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	public void setLeftBinary(Mat img){
		OpenCVController.onFXThread(leftBinaryImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	public void setRightBinary(Mat img){
		OpenCVController.onFXThread(rightBinaryImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	public void setLeftPointer(Mat img){
		OpenCVController.onFXThread(leftPointerImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	public void setRightPointer(Mat img){
		OpenCVController.onFXThread(rightPointerImageView.imageProperty(), OpenCVController.mat2Image(img));
	}
	
	// remember: H ranges 0-180, S and V range 0-255
	public Scalar getMinHSV(){
		Scalar min = new Scalar(hueMinSlider.getValue(), saturationMinSlider.getValue(), valueMinSlider.getValue());
		return min;
	}
	
	public Scalar getMaxHSV(){
		Scalar max = new Scalar(hueMaxSilder.getValue(), saturationMaxSilder.getValue(), valueMaxSilder.getValue());
		return max;
	}
	
	public int getBrightness(){
		int b = (int) brightnesSlider.getValue();
		return b;
	}
	
	public void sethsvValue(String hsv){
		OpenCVController.onFXThread(this.hsvValuesProp, hsv);
	}
	
	public void TrainingStart(){
		trainingStage.toFront();
        trainingThread = new Thread(new Runnable() {
            public void run() {
            	System.out.println("Init Training Seq");
                try {
                    Thread.sleep(7000);
                    setTrainingStagePos(0, 0);
                    Thread.sleep(5000);
                    setTrainingStagePos(0, OpenCVController.screenY - 300);
                    Thread.sleep(5000);
                    setTrainingStagePos(OpenCVController.screenX - 200, OpenCVController.screenY - 300);
                    Thread.sleep(5000);
                    setTrainingStagePos(OpenCVController.screenX - 200, 0);
                    Thread.sleep(5000);
                    setTrainingStagePos(OpenCVController.screenX/2 - 200,  OpenCVController.screenY/2 - 200);
                    training();
                    stopTraining();
                	
                } catch (Exception ex) {
                    System.out.println("Interrupted");
                }
            }
        });

        trainingThread.start();	
	}
	
	private void setTrainingStagePos(double x, double y){
		trainingStage.setX(x);
        trainingStage.setY(y);
	}
	
	public void initTraining() {
		trainingValuesProps = new SimpleObjectProperty<>();
		this.trainingTargetLabel.textProperty().bind(trainingValuesProps);
	}
	
	public void stopTraining(){
		trainingStage.toBack();
		OpenCVController.trainingLoader.<ProcessingController>getController().trainingStage.close();
	}
	
	public void startTrainingTargetStage() throws IOException{
		trainingStage = new Stage();
		trainingStage.setTitle("Training");
		
    	FXMLLoader trainingLoader = new FXMLLoader(getClass().getResource("TrainingTarget.fxml"));
		BorderPane mainLayout = trainingLoader.load();
		
		Scene scene = new Scene(mainLayout);
		trainingStage.setScene(scene);
		trainingStage.setOnCloseRequest(evt -> {
			trainingStage.close();
		});
		OpenCVController.trainingLoader = trainingLoader;
		trainingStage.show();
		trainingLoader.<ProcessingController>getController().initTraining();
	}
	
	public void setTrainingValue(String value){
		OpenCVController.onFXThread(this.trainingValuesProps, value);
	}
}
