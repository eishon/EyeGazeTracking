package application;
	
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	private Stage primaryStage;
	private BorderPane cameraLayout;
	
	public static void main(String[] args) throws IOException, AWTException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		getScreenSize();
		OpenCVController.Init();
		
		launch(args);
	}
	
	private static void getScreenSize(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screen_width = OpenCVController.screenX = (int) screenSize.getWidth();
		int screen_height = OpenCVController.screenY = (int) screenSize.getHeight();
		System.out.println("Screen Size: "+screen_width+" x "+screen_height);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Camera Feed");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("CameraFeed.fxml"));
		cameraLayout = loader.load();
		Scene scene = new Scene(cameraLayout);
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(evt -> {
			System.exit(0);
		});
		primaryStage.show();
	}
	
}
