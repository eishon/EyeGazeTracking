package application;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResultController {
	public Stage resultstage;
	public Stage leftEyeStage;
	public Stage rightEyeStage;
	@FXML
	private Label positionLabel;
	@FXML
	private Label leftPosLabel;
	@FXML
	private Label rightPosLabel;
	//@FXML
	//private Canvas positionCanvas;
	
	private ObjectProperty<String> positionValuesProp, leftEyePosValProp, rightEyePosValProp;
	private int posX = 0, posY = 0;
	//GraphicsContext  gfx;
	
	public void init(Stage stage){
		resultstage = new Stage();
		resultstage = stage;
		positionValuesProp = new SimpleObjectProperty<>();
		leftEyePosValProp = new SimpleObjectProperty<>();
		rightEyePosValProp = new SimpleObjectProperty<>();
		this.positionLabel.textProperty().bind(positionValuesProp);
		//gfx = positionCanvas.getGraphicsContext2D();
		
	}
	
	public void initLeft(Stage stage){
		leftEyeStage = new Stage();
		leftEyeStage = stage;
		leftEyePosValProp = new SimpleObjectProperty<>();
		this.leftPosLabel.textProperty().bind(leftEyePosValProp);
	}
	
	public void initRight(Stage stage){
		rightEyeStage = new Stage();
		rightEyeStage = stage;
		rightEyePosValProp = new SimpleObjectProperty<>();
		this.rightPosLabel.textProperty().bind(rightEyePosValProp);
	}
	
	public void setPosition(int x, int y){
		posX = x;
		posY = y;
		
		resultstage.setX(posX);
		resultstage.setY(posY);
		
		String postion = "X: " + x + " Y: " + y;
		OpenCVController.onFXThread(this.positionValuesProp, postion);
		
		//gfx.setFill(Color.GREEN);
		//gfx.fillOval(posX, posY, 10, 10);
	}
	
	public void setLeftPos(int x, int y){
		leftEyeStage.setX(x);
		leftEyeStage.setY(y);
		
		String postion = "X: " + x + " Y: " + y;
		OpenCVController.onFXThread(this.leftEyePosValProp, postion);
	}
	
	public void setRightPos(int x, int y){
		rightEyeStage.setX(x);
		rightEyeStage.setY(y);
		
		String postion = "X: " + x + " Y: " + y;
		OpenCVController.onFXThread(this.rightEyePosValProp, postion);
	}
}
