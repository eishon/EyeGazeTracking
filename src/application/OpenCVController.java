package application;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

/**
 *
 * @author EISHON
 */
public class OpenCVController {
    private static CascadeClassifier faceCascade;
    private static CascadeClassifier eyesCascade;
    private static CascadeClassifier leftEyeCascade;
    private static CascadeClassifier rightEyeCascade;
    private static int absoluteFaceSize;
    private static int absoluteEyeSize;
    private static float cannyThreshold = 0f;
    private static int contrast = 1, brightness = 0;

    private static final String faceHaarCasCadePath = "resources/haarcascades/haarcascade_frontalface_alt.xml";
    private static final String faceLBPCasCadePath = "resources/lbpcascades/lbpcascade_frontalface.xml";
    private static final String eyesHaarCasCadePath = "resources/haarcascades/haarcascade_eye.xml";
    private static final String leftEyeHaarCasCadePath = "resources/haarcascades/haarcascade_lefteye_2splits.xml";
    private static final String rightEyeHaarCasCadePath = "resources/haarcascades/haarcascade_righteye_2splits.xml";
    private static final String leftEyeHaarCasCadePath_2 = "resources/haarcascades/eye/left_eye.xml";
    private static final String rightEyeHaarCasCadePath_2 = "resources/haarcascades/eye/right_eye.xml";
    
    private static Mat originalFrame, faceFrame, leftEyeFrame, rightEyeFrame;
    private static Mat leftBinaryFrame, rightBinaryFrame, leftPointerFrame, rightPointerFrame;
    public static FXMLLoader processingLoader;
    public static FXMLLoader trainingLoader;
	public static FXMLLoader resultLoader;
	public static FXMLLoader leftEyeLoader;
	public static FXMLLoader rightEyeLoader;
	
	private static boolean singleFlag = false;
	private static boolean grayHSVProcess = false;
	public static boolean trainingFlag = false;
	private static boolean leftEyeDetect = false;
	private static boolean rightEyeDetect = false;
	
	private static int xMin = 2000, yMin = 2000;
	private static int xMax = 0, yMax = 0;
	private static int posX = 200, posY = 200;
	
	private static int xLeftMin = 2000, yLeftMin = 2000;
	private static int xLeftMax = 0, yLeftMax = 0;
	private static int posLeftX = 200, posLeftY = 200;
	
	private static int xRightMin = 2000, yRightMin = 2000;
	private static int xRightMax = 0, yRightMax = 0;
	private static int posRightX = 200, posRightY = 200;
	
	public static int screenX = 0, screenY = 0;
	
	private static Queue<Integer> xPosVals;
	private static Queue<Integer> yPosVals;
    
	private static Queue<Integer> xLeftPosVals;
	private static Queue<Integer> yLeftPosVals;
	
	private static Queue<Integer> xRightPosVals;
	private static Queue<Integer> yRightPosVals;
	
	private static Robot mouse;
	
    public static void Init() throws IOException, AWTException{
    	mouse = new Robot();
    	
    	originalFrame = new Mat();
    	xPosVals = new LinkedList<Integer>();
    	yPosVals = new LinkedList<Integer>();
    	
    	xLeftPosVals = new LinkedList<Integer>();
    	yLeftPosVals = new LinkedList<Integer>();
    	xRightPosVals = new LinkedList<Integer>();
    	yRightPosVals = new LinkedList<Integer>();
    	
        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        leftEyeCascade = new CascadeClassifier();
        rightEyeCascade = new CascadeClassifier();
        absoluteFaceSize = 0;

        faceCascade.load(faceHaarCasCadePath);
        eyesCascade.load(eyesHaarCasCadePath);
        leftEyeCascade.load(leftEyeHaarCasCadePath);
        rightEyeCascade.load(rightEyeHaarCasCadePath);
        //leftEyeCascade.load(leftEyeHaarCasCadePath_2);
        //rightEyeCascade.load(rightEyeHaarCasCadePath_2);
        
    }
    
    public static void setFrame(Mat img){
    	originalFrame = img;
    	grayHSVProcess = processingLoader.<ProcessingController>getController().getGrayProcessState();
    	doProcessing();
    	
    }
    
    private static void doProcessing(){
    	faceFrame = detectFace(originalFrame);
    	
    	leftEyeFrame = detectLeftEye(faceFrame);
    	rightEyeFrame = detectRightEye(faceFrame);
    	
    	if(leftEyeFrame.channels() > 1 && grayHSVProcess)
    		leftEyeFrame = doGray(leftEyeFrame);
    	if(rightEyeFrame.channels() > 1 && grayHSVProcess)
    		rightEyeFrame = doGray(rightEyeFrame);
    	
    	leftEyeFrame = setBrightness(leftEyeFrame);
    	rightEyeFrame = setBrightness(rightEyeFrame);
    	
    	if(grayHSVProcess){
    		leftBinaryFrame = doMaskGray(leftEyeFrame);
    		rightBinaryFrame = doMaskGray(rightEyeFrame);
    	}else{
    		leftBinaryFrame = doMask(leftEyeFrame);
    		rightBinaryFrame = doMask(rightEyeFrame);
    	}
    	
    	leftBinaryFrame = doMorfOp(leftBinaryFrame);
    	rightBinaryFrame = doMorfOp(rightBinaryFrame);
    	
    	if(leftEyeDetect){
    		//leftPointerFrame = findAndDrawContours(leftBinaryFrame, leftEyeFrame);
    		leftPointerFrame = findAndDrawLeftEye(leftBinaryFrame, leftEyeFrame);
    	}
    	if(rightEyeDetect){
    		//rightPointerFrame = findAndDrawContours(rightBinaryFrame, rightEyeFrame);
    		rightPointerFrame = findAndDrawRightEye(rightBinaryFrame, rightEyeFrame);
    	}
    	
    	if(trainingFlag){
    		//processTrainingVal();
    		trainingLoader.<ProcessingController>getController().setTrainingValue(processTrainingVal_2());
    	} else{
    		//processPositionVal();
    		processLeftPositionVal();
    		processRightPositionVal();
    		processPositionLR();
    	}
    	
    	processingLoader.<ProcessingController>getController().setFace(faceFrame);
    	processingLoader.<ProcessingController>getController().setLeftEye(leftEyeFrame);
    	processingLoader.<ProcessingController>getController().setRightEye(rightEyeFrame);
    	processingLoader.<ProcessingController>getController().setLeftBinary(leftBinaryFrame);
    	processingLoader.<ProcessingController>getController().setRightBinary(rightBinaryFrame);
    	processingLoader.<ProcessingController>getController().setLeftPointer(leftPointerFrame);
    	processingLoader.<ProcessingController>getController().setRightPointer(rightPointerFrame);
    	
    	resultLoader.<ResultController>getController().setPosition(posX, posY);
    	leftEyeLoader.<ResultController>getController().setLeftPos(posLeftX, posLeftY);
    	rightEyeLoader.<ResultController>getController().setRightPos(posRightX, posRightY);
    	
    	if(processingLoader.<ProcessingController>getController().getMouseControlState())
    		setMousePointer(posX, posY);
    	
    	leftEyeDetect = false;
    	rightEyeDetect = false;
    }
    
    public static BufferedImage matToBufferedImage(Mat original){
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1){
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else{
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
    
    public static Image mat2Image(Mat frame) {
    	if(frame != null){
			try {
				return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
			} catch (Exception e) {
				System.err.println("Cannot convert the Mat obejct: " + e);
			}
    	}
    	return null;
	}
	
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(() -> {
			property.set(value);
		});
	}
    
    public static Mat detectFace(Mat frame){
        MatOfRect faces = new MatOfRect();
        Mat faceMat = new Mat();
        Mat grayFrame = new Mat();

        faceMat = frame;
        // convert the frame in gray scale
        Imgproc.cvtColor(faceMat, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteFaceSize == 0){
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }
        // detect faces
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 4, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        //for (int i = 0; i < facesArray.length; i++)
        //    Imgproc.rectangle(faceMat, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 1);

        if(facesArray.length > 0) faceMat = faceMat.submat(facesArray[0]);

        //System.out.println("Face Detected");
        return faceMat;
    }
    
    public static Mat detectEyes(Mat frame){
        MatOfRect eye = new MatOfRect();
        Mat eyesMat = new Mat();
        Mat gray = new Mat();

        eyesMat = frame;
        // convert the frame in gray scale
        Imgproc.cvtColor(eyesMat, gray, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(gray, gray);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteEyeSize == 0){
            int height = gray.rows();
            if (Math.round(height * 0.01f) > 0){
                    absoluteEyeSize = Math.round(height * 0.01f);
            }
        }

        // detect left eye
        eyesCascade.detectMultiScale(gray, eye, 1.05, 10, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(absoluteEyeSize, absoluteEyeSize), new Size());

        Rect[] eyesArray = eye.toArray();

        for (int i = 0; i < eyesArray.length; i++)
            Imgproc.rectangle(eyesMat, eyesArray[i].tl(), eyesArray[i].br(), new Scalar(0, 0, 255), 1);

        //if(eyesArray.length > 0) leftEyeMat = faceMat.submat(eyesArray[0]);
        //if(eyesArray.length > 1) rightEyeMat = faceMat.submat(eyesArray[1]);
        //System.out.println("Eye Detected");
        return eyesMat;
    }
	
    public static Mat detectLeftEye(Mat frame){
        MatOfRect leftEye = new MatOfRect();
        Mat leftGray = new Mat();
        Mat leftEyeMat = new Mat();

        leftEyeMat = frame;
        // convert the frame in gray scale
        Imgproc.cvtColor(leftEyeMat, leftGray, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(leftGray, leftGray);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteEyeSize == 0){
            int height = leftGray.rows();
            if (Math.round(height * 0.01f) > 0){
                absoluteEyeSize = Math.round(height * 0.01f);
            }
        }

        // detect left eye
        leftEyeCascade.detectMultiScale(leftGray, leftEye, 1.05, 10, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(absoluteEyeSize, absoluteEyeSize), new Size());

        Rect[] leftEyesArray = leftEye.toArray();

        for (int i = 0; i < leftEyesArray.length; i++)
            Imgproc.rectangle(leftEyeMat, leftEyesArray[i].tl(), leftEyesArray[i].br(), new Scalar(0, 0, 255), 1);
        
        //if(leftEyesArray.length > 0) leftEyeMat = faceMat.submat(leftEyesArray[0]);
        if(leftEyesArray.length > 0) {
        	if(grayHSVProcess){
        		leftEyeMat = leftGray.submat(leftEyesArray[0]);
        	}else{
        		leftEyeMat = leftEyeMat.submat(leftEyesArray[0]);
        	}
            leftEyeMat = resizeEyeImg(leftEyeMat);

            leftEyeDetect = true;
        }
        //System.out.println("Left Eye Detected");
        return leftEyeMat;
    }

    public static Mat detectRightEye(Mat frame){
        MatOfRect rightEye = new MatOfRect();
        Mat rightGray = new Mat();
        Mat rightEyeMat = new Mat();

        rightEyeMat = frame;
        // convert the frame in gray scale
        Imgproc.cvtColor(rightEyeMat, rightGray, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(rightGray, rightGray);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteEyeSize == 0){
            int height = rightEye.rows();
            if (Math.round(height * 0.01f) > 0){
                absoluteEyeSize = Math.round(height * 0.01f);
            }
        }

        // detect right eye
        rightEyeCascade.detectMultiScale(rightGray, rightEye, 1.05, 6, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(absoluteEyeSize, absoluteEyeSize), new Size());

        // each rectangle in eyes is a eye: draw them!
        Rect[] rightEyesArray = rightEye.toArray();

        for (int i = 0; i < rightEyesArray.length; i++)
            Imgproc.rectangle(rightEyeMat, rightEyesArray[i].tl(), rightEyesArray[i].br(), new Scalar(0, 0, 255), 1);

        //if(rightEyesArray.length > 0) rightEyeMat = faceMat.submat(rightEyesArray[0]);
        if(rightEyesArray.length > 0) {
        	if(grayHSVProcess){
        		rightEyeMat = rightGray.submat(rightEyesArray[0]);
        	}else{
        		rightEyeMat = rightEyeMat.submat(rightEyesArray[0]);
        	}
            rightEyeMat = resizeEyeImg(rightEyeMat);

            rightEyeDetect = true;
        }
        //System.out.println("Right Eye Detected");
        return rightEyeMat;
    }
    
    private static Mat resizeEyeImg(Mat frame){
        frame = frame.submat(new Rect(0,(int)(frame.height()*0.5f), frame.width(), (int)(frame.height()*0.4f)));
        //System.out.println("Image Resized");
        return frame;
    }
    
    public static Mat doGray(Mat frame){
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        //System.out.println("Gray Done");
        return gray;
    }
    
    public static Mat doEqualizeHistogram(Mat frame){
        Mat eqHist = new Mat();
        Imgproc.equalizeHist(frame, eqHist);
        //System.out.println("Equalize Histogram Done");
        return eqHist;
    }
    
    public static Mat setBrightness( Mat frame){
    	Mat img = new Mat();
    	brightness = processingLoader.<ProcessingController>getController().getBrightness();
    	frame.convertTo(img, -1, contrast, brightness);
    	if(grayHSVProcess)
    		img = doEqualizeHistogram(img);
    	
    	//System.out.println("Brightness Done");
    	return img;
    }
    
    public static Mat doCanny(Mat frame){
        Mat detectedEdges = new Mat();

        // reduce noise with a 3x3 kernel
        Imgproc.blur(frame, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, cannyThreshold, cannyThreshold * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);
        //System.out.println("Canny Done");
        return dest;
    }
	
    public static Mat doMask(Mat frame){
        Mat blurredImage = new Mat();
        Mat hsvImage = new Mat();
        Mat mask = new Mat();

        // remove some noise
        Imgproc.blur(frame, blurredImage, new Size(7, 7));

        // convert the frame to HSV
        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // get thresholding values from the UI
        // remember: H ranges 0-180, S and V range 0-255
        Scalar minValues = processingLoader.<ProcessingController>getController().getMinHSV();
        Scalar maxValues = processingLoader.<ProcessingController>getController().getMaxHSV();

        // show the current selected HSV range
        String valuesToPrint = "Hue:\n" + minValues.val[0] + "\n" + maxValues.val[0]
                        + "\nSaturation:\n" + minValues.val[1] + "\n" + maxValues.val[1] + "\nValue:\n"
                        + minValues.val[2] + "\n" + maxValues.val[2];

        processingLoader.<ProcessingController>getController().sethsvValue(valuesToPrint);
        // threshold HSV image
        Core.inRange(hsvImage, minValues, maxValues, mask);
        //System.out.println("Mask Done");
        return mask;
    }
    
    public static Mat doMaskGray(Mat frame){
        Mat blurredImage = new Mat();
        Mat hsvImage = new Mat();
        Mat mask = new Mat();

        if(frame.channels()==1)
        Imgproc.cvtColor(frame, blurredImage, Imgproc.COLOR_GRAY2BGR);
        // remove some noise
        Imgproc.blur(blurredImage, blurredImage, new Size(7, 7));

        // convert the frame to HSV
        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // get thresholding values from the UI
        // remember: H ranges 0-180, S and V range 0-255
        Scalar minValues = processingLoader.<ProcessingController>getController().getMinHSV();
        Scalar maxValues = processingLoader.<ProcessingController>getController().getMaxHSV();

        // show the current selected HSV range
        String valuesToPrint = "Hue:\n" + minValues.val[0] + "\n" + maxValues.val[0]
                        + "\nSaturation:\n" + minValues.val[1] + "\n" + maxValues.val[1] + "\nValue:\n"
                        + minValues.val[2] + "\n" + maxValues.val[2];

        processingLoader.<ProcessingController>getController().sethsvValue(valuesToPrint);
        // threshold HSV image
        Core.inRange(hsvImage, minValues, maxValues, mask);
        //System.out.println("Gray Mask Done");
        return mask;
    }
	
    public static Mat doMorfOp(Mat mask){
        Mat morphOutput = new Mat();

        // morphological operators
        // dilate with large element, erode with small ones
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Mat closingElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));

        Imgproc.erode(mask, morphOutput, erodeElement);
        Imgproc.erode(morphOutput, morphOutput, erodeElement);

        Imgproc.dilate(morphOutput, morphOutput, dilateElement);
        Imgproc.dilate(morphOutput, morphOutput, dilateElement);
        
        Imgproc.morphologyEx(morphOutput, morphOutput, Imgproc.MORPH_CLOSE, closingElement);

        // find the contours and show them
        //frame = this.findAndDrawBalls(morphOutput, frame);

        // show the partial output
        //System.out.println("Morph Done");
        return morphOutput;
    }
    
    public static Mat drawHoughCircle(Mat mask, Mat original){
    	Mat circles = new Mat();
    	Imgproc.HoughCircles(mask, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 100, 0, 500);
        for (int i = 0; i < circles.cols(); i++) {
            double[] vCircle = circles.get(0, i);

            Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int)Math.round(vCircle[2]);

            Imgproc.circle(original, pt, radius, new Scalar(255, 0, 0), 2);
        }
		return original;

    }
    
    public static void setScreenPoints(){
    	Mat contour1 = new Mat(), contour2;
    	
    }
	
    public static Mat findAndDrawContours(Mat maskedImage, Mat frame){
        // init
    	Mat img = new Mat();
    	img = frame;
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0){
            // for each contour, display it in blue
            //for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]){
            //    Imgproc.drawContours(img, contours, idx, new Scalar(0, 255, 0));
                
            //}
            
            List<Moments> mu = new ArrayList<Moments>(contours.size());
            for (int i = 0; i < contours.size(); i++) {
                mu.add(i, Imgproc.moments(contours.get(i), false));
                Moments p = mu.get(i);
                int x = (int) (p.get_m10() / p.get_m00());
                int y = (int) (p.get_m01() / p.get_m00());
                Imgproc.circle(frame, new Point((double) x , (double) y), 2, new Scalar(0, 255, 0, 255));
                posX = setPositionAvg(x, xPosVals);
                posY = setPositionAvg(y, yPosVals);
            }
        }
        return img;
    }
    
    public static Mat findAndDrawLeftEye(Mat maskedImage, Mat frame){
    	Mat img = new Mat();
    	img = frame;
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0){
            // for each contour, display it in blue
            //for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]){
            //    Imgproc.drawContours(img, contours, idx, new Scalar(0, 255, 0));
                
            //}
            
            List<Moments> mu = new ArrayList<Moments>(contours.size());
            for (int i = 0; i < contours.size(); i++) {
                mu.add(i, Imgproc.moments(contours.get(i), false));
                Moments p = mu.get(i);
                int x = (int) (p.get_m10() / p.get_m00());
                int y = (int) (p.get_m01() / p.get_m00());
                Imgproc.circle(frame, new Point((double) x , (double) y), 2, new Scalar(0, 255, 0, 255));
                posLeftX = setPositionAvg(x, xLeftPosVals);
                posLeftY = setPositionAvg(y, yLeftPosVals);
            }
        }
        return img;
    }
    
    public static Mat findAndDrawRightEye(Mat maskedImage, Mat frame){
    	Mat img = new Mat();
    	img = frame;
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0){
            // for each contour, display it in blue
            //for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]){
            //    Imgproc.drawContours(img, contours, idx, new Scalar(0, 255, 0));
                
            //}
            
            List<Moments> mu = new ArrayList<Moments>(contours.size());
            for (int i = 0; i < contours.size(); i++) {
                mu.add(i, Imgproc.moments(contours.get(i), false));
                Moments p = mu.get(i);
                int x = (int) (p.get_m10() / p.get_m00());
                int y = (int) (p.get_m01() / p.get_m00());
                Imgproc.circle(frame, new Point((double) x , (double) y), 2, new Scalar(0, 255, 0, 255));
                posRightX = setPositionAvg(x, xRightPosVals);
                posRightY = setPositionAvg(y, yRightPosVals);
            }
        }
        return img;
    }
    
    private static void processTrainingVal(){
    	if(posX > 1 && posX < xMin)
    		xMin = posX;
    	if(posX > xMax)
    		xMax = posX;
    	if(posY > 1 && posY < yMin)
    		yMin = posY;
    	if(posY > yMax)
    		yMax = posY;
    	
    	System.out.println("X Min:"+xMin+" X Max:"+xMax+" Y Min:"+yMin+" Y Max:"+yMax);
    }
    
    private static String processTrainingVal_2(){
    	if(leftEyeDetect){
	    	if(posLeftX > 1 && posLeftX < xLeftMin)
	    		xLeftMin = posLeftX;
	    	if(posLeftX > xLeftMax)
	    		xLeftMax = posLeftX;
	    	if(posLeftY > 1 && posLeftY < yLeftMin)
	    		yLeftMin = posLeftY;
	    	if(posLeftY > yLeftMax)
	    		yLeftMax = posLeftY;
    	}
    	
    	if(rightEyeDetect){
	    	if(posRightX > 1 && posRightX < xRightMin)
	    		xRightMin = posRightX;
	    	if(posRightX > xRightMax)
	    		xRightMax = posRightX;
	    	if(posRightY > 1 && posRightY < yRightMin)
	    		yRightMin = posRightY;
	    	if(posRightY > yRightMax)
	    		yRightMax = posRightY;
    	}
    	
    	String text = "X Left :("+xLeftMin+","+xLeftMax+")\nY Left:("+yLeftMin+","+yLeftMax+
    						")\nX Right :("+xRightMin+","+xRightMax+")\nY Right:("+yRightMin+","+yRightMax+")";
    	
    	return text;
    }
    
    public static void resetTrainingVals(){
    	xMin = xLeftMin = xRightMin = 2000;
    	yMin = yLeftMin = yRightMin = 2000;
    	xMax = xLeftMax = xRightMax = 0;
    	yMax = yLeftMax = yRightMax = 0;
    }
    
    private static void processPositionVal(){
    	int width = xMax - xMin;
    	int height = yMax - yMin;
    	int xRatio = screenX / width;
    	int yRatio = screenY / height;
    	posX = (((posX - xMin) > 0) ? (posX - xMin) :0) * xRatio;
    	posY = (((posY - yMin) > 0) ? (posY - yMin) :0) * yRatio;
    	posX = (posX <= screenX - 150) ? posX : screenX - 200;
    	posY = (posY <= screenY - 150) ? posY : screenY - 200;
    	System.out.println("X: " + posX +", Y: " + posY);
    }
    
    private static void processLeftPositionVal(){
    	int width = xLeftMax - xLeftMin;
    	int height = yLeftMax - yLeftMin;
    	int xRatio = screenX / width;
    	int yRatio = screenY / height;
    	posLeftX = (((posLeftX - xLeftMin) > 0) ? (posLeftX - xLeftMin) :0) * xRatio;
    	posLeftY = (((posLeftY - yLeftMin) > 0) ? (posLeftY - yLeftMin) :0) * yRatio;
    	posLeftX = (posLeftX <= screenX - 150) ? posLeftX : screenX - 200;
    	posLeftY = (posLeftY <= screenY - 150) ? posLeftY : screenY - 200;
    	//System.out.println("X: " + posLeftX +", Y: " + posLeftY);
    }
    
    private static void processRightPositionVal(){
    	int width = xRightMax - xRightMin;
    	int height = yRightMax - yRightMin;
    	int xRatio = screenX / width;
    	int yRatio = screenY / height;
    	posRightX = (((posRightX - xRightMin) > 0) ? (posRightX - xRightMin) :0) * xRatio;
    	posRightY = (((posRightY - yRightMin) > 0) ? (posRightY - yRightMin) :0) * yRatio;
    	posRightX = (posRightX <= screenX - 150) ? posRightX : screenX - 200;
    	posRightY = (posRightY <= screenY - 150) ? posRightY : screenY - 200;
    	//System.out.println("X: " + posRightX +", Y: " + posRightY);
    }
    
    private static void processPositionLR(){
    	if(leftEyeDetect){
	    	posX = setPositionAvg(posLeftX, xPosVals);
	    	posY = setPositionAvg(posLeftY, yPosVals);
    	}
    	if(rightEyeDetect){
	    	posX = setPositionAvg(posRightX, xPosVals);
	    	posY = setPositionAvg(posRightY, yPosVals);
    	}
    	System.out.println("X: " + posX +", Y: " + posY);
    }
    
    private static int setPositionAvg(int x, Queue<Integer> q){
    	if(q.size() > 30)
    		q.remove();
    	
    	q.add(x);
    	
    	int sum = 0;
    	for (int i: q)
			sum += i;
    	
    	return sum / q.size();
    }
    
    private static void setMousePointer(int x, int y) {
    	if(leftEyeDetect || rightEyeDetect){
    		mouse.mouseMove(x, y);
    	}
	}
    
    private static void mouseClick(){
    	mouse.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }
}
