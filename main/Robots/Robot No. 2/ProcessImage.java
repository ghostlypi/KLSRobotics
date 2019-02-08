package frc.robot;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class ProcessImage {

	public static final int GAUSSIAN_BLUR_KERNEL_SIZE = 55;
	public static final int THRESHOLD_MIN_VALUE = 127;
	public static final int THRESHOLD_CVT_VALUE = 255;

	public static final int CANNY_MIN_LINE_LEN = 400;
	public static final int CANNY_MAX_LINE_GAP = 1000;

	private static String output = "";

	// setOutput -> System.out.println without spamming.
	// simply returns early if the output message has already just been printed to the console.
	public static void setOutput(String message) {
		if(message == null || output.equals(message)) {
			return;
		}
		output = message;
		System.out.println(output);
	}

	// gets the midpoint of the list of contours.
	// does this by making a bounding rect around each contour and adding it's corner's x and y values.
	// then it divides the x and y values by the number of contours.
	public static MyVector getMidPointOfContours(List<MatOfPoint> contours) {
		int x = 0;
		int y = 0;
		for(int i=0;i<contours.size();i++) {
			// http://answers.opencv.org/question/100989/finding-center-of-rect/
			Rect rect = Imgproc.boundingRect(contours.get(i));
			x += (rect.tl().x + rect.br().x) * 0.5;
			y += (rect.tl().y + rect.br().y) * 0.5;
		}
		x /= contours.size();
		y /= contours.size();
		return new MyVector(x,y,0);
	}

	// returns a displayable image that we can use to see what's going on.
	public static Mat displayImage(Mat source) {
		Mat nextStep = new Mat();

		Imgproc.cvtColor(source,nextStep,Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(nextStep,nextStep,new Size(GAUSSIAN_BLUR_KERNEL_SIZE,GAUSSIAN_BLUR_KERNEL_SIZE),GAUSSIAN_BLUR_KERNEL_SIZE);
		Imgproc.threshold(nextStep,nextStep,THRESHOLD_MIN_VALUE,THRESHOLD_CVT_VALUE,Imgproc.THRESH_BINARY);
		Imgproc.Canny(nextStep,nextStep,CANNY_MIN_LINE_LEN,CANNY_MAX_LINE_GAP,3,true);

		return nextStep;
	}

	// takes the displayable image from the previous step, finds contours,
	// and tries to account for cases where we have more or fewer than 2 contours.
	// if we have more, we take the two largest ones.
	// if we have fewer, we just stop if we can't detect any lines, and use only one line if that's all we can see.
	public static MyVector processImage(Mat source) {
		List<MatOfPoint> contours = new ArrayList<>();
		Mat nextStep = displayImage(source);

		Imgproc.findContours(nextStep,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

		if(contours.size() == 0) { setOutput("Could not find any lines. Are you sure you are near the gaffers tape?"); }
		
		else if(contours.size() == 1) {

			MyVector vec = getMidPointOfContours(contours);
			RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));
			vec.setAngle(90 - rect.angle);
			return vec;
			
		}
		else if(contours.size() > 2){
			// uses arcLength to find the two largest contours.
			List<MatOfPoint> trimmedContours = new ArrayList<>();
			int maxAreaIdx = 0;
			int maxAreaIdx2 = 1;
			double length;
			for(int i=0;i<contours.size();i++){
				length = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()),true);
				if(length > Imgproc.arcLength(new MatOfPoint2f(contours.get(maxAreaIdx).toArray()),true)) { maxAreaIdx = i; }
				else if(length > Imgproc.arcLength(new MatOfPoint2f(contours.get(maxAreaIdx2).toArray()),true)) { maxAreaIdx2 = i; }
			}
			trimmedContours.add(contours.get(maxAreaIdx));
			trimmedContours.add(contours.get(maxAreaIdx2));

			MyVector vec = getMidPointOfContours(trimmedContours);
			RotatedRect rect1 = Imgproc.minAreaRect(new MatOfPoint2f(trimmedContours.get(0).toArray()));
			RotatedRect rect2 = Imgproc.minAreaRect(new MatOfPoint2f(trimmedContours.get(1).toArray()));
			double angle = (rect1.angle + rect2.angle) / 2; // average angle.
			vec.setAngle(90 - angle);
			return vec;
		}

		else {
			MyVector vec = getMidPointOfContours(contours);
			RotatedRect rect1 = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));
			RotatedRect rect2 = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(1).toArray()));
			double angle = (rect1.angle + rect2.angle) / 2; // average angles.
			vec.setAngle(90 - angle);
			return vec;
		}
		return null;
	}
}
