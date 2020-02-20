import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.videoio.*;
import org.opencv.imgproc.*;
import org.opencv.calib3d.*;

class Pair<K, V> {

	private K k;
	private V v;

	public Pair(K k, V v) {
		this.k = k;
		this.v = v;
	}

	public K getKey() { return k; }
	public V getValue() { return v; }

	public void setKey(K k) { this.k = k; }
	public void setValue(V v) { this.v = v; }
}

public class FindBall {

	public static final double R = 3.5; // inches
	
	public static final double X_OFFSET = 0d; // TODO: set this (in inches).
	public static final double Y_OFFSET = 0d; // TODO: set this (in inches).
	public static final double Z_OFFSET = 0d; // TODO: set this (in inches).

	// https://stackoverflow.com/questions/30258163/display-image-in-mat-with-jframe-opencv-3-00
	public static BufferedImage bufferedImage(Mat m) {
    int type = BufferedImage.TYPE_BYTE_GRAY;
    if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
    }
    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
    m.get(0,0,((DataBufferByte)image.getRaster().getDataBuffer()).getData()); // get all the pixels
    return image;
	}

	public static void readCalibrationData(String filename, Mat cameraMatrix, Mat distCoeffs) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			String line;
			int currentRow = -1;
			int maxRows = 0, maxCols = 0;
			boolean usingCameraMatrix = true;
			while((line = bufferedReader.readLine()) != null) {
				if(currentRow == -1) {
					String[] parts = line.split(" ", -1);
					maxRows = Integer.parseInt(parts[0]);
					maxCols = Integer.parseInt(parts[1]);
					currentRow++;
					if(usingCameraMatrix) {
						(new Mat(new Size(maxRows, maxCols), CvType.CV_64F)).copyTo(cameraMatrix);
					}
					else {
						(new Mat(new Size(maxRows, maxCols), CvType.CV_64F)).copyTo(distCoeffs);
					}
				}
				else {
					String[] parts = line.split(" ", -1);
					double[] d = new double[parts.length];
					for(int i=0;i<parts.length;i++) {
						if(parts[i].length() > 0) {
							d[i] = Double.parseDouble(parts[i]);
						}
					}
					if(usingCameraMatrix) {
						cameraMatrix.put(currentRow, 0, d);
					}
					else {
						distCoeffs.put(currentRow, 0, d);
					}
					currentRow++;
					if(currentRow == maxRows) { currentRow = -1; usingCameraMatrix = false; }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Point3 findBall(Mat mat, int width, int height, Mat cameraMatrix, Mat distCoeffs) {
		Mat blurred = new Mat(), hsv = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC2), mask = new Mat();
		Imgproc.GaussianBlur(mat, blurred, new Size(11, 11), 0);
		Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_BGR2HSV);
		Core.inRange(hsv, new Scalar(20, 100, 50), new Scalar(30, 255, 255), mask);
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		Imgproc.erode(mask, mask, element, new Point(), 2);
		Imgproc.dilate(mask, mask, element, new Point(), 2);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mask.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		if(contours.size() == 0) { return null; }
		Collections.sort(contours, new Comparator<MatOfPoint>() {
			@Override public int compare(final MatOfPoint m1, final MatOfPoint m2) {
				return (int)(Imgproc.contourArea(m1) - Imgproc.contourArea(m2));
			}
		});
		float[] radius = new float[1];
		Point center = new Point();
		Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(0).toArray()), center, radius);
		Moments moments = Imgproc.moments(contours.get(0));
		if(moments.get_m00() == 0) { return null; }
		double x = moments.get_m10() / moments.get_m00();
		double y = moments.get_m01() / moments.get_m00();
		if (radius[0] > 10) {
			double f_x = cameraMatrix.get(0, 0)[0];
			double f_y = cameraMatrix.get(1, 1)[0];
			double f = Math.sqrt(f_x * f_x + f_y * f_y);
			double X = x * R / radius[0];
			double Y = y * R / radius[0];
			double Z = f * R / radius[0];
			return new Point3(X, Y, Z);
		}
		return null;
	}

	public static double getBallValue(Mat mat, int width, int height, Mat cameraMatrix, Mat distCoeffs) {
		Point3 ball = getBallValue(mat, width, height, cameraMatrix, distCoeffs);
		ball.x -= FindBall.X_OFFSET;
		ball.y -= FindBall.Y_OFFSET;
		ball.z -= FindBall.Z_OFFSET;
		if(ball != null && ball.z != 0) {
			return Math.atan2(ball.x / ball.z);
		}
		return 0d;
	}

	/*public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture vc = new VideoCapture(0);
		
		double orig_width = vc.get(Videoio.CAP_PROP_FRAME_WIDTH);
		double orig_height = vc.get(Videoio.CAP_PROP_FRAME_HEIGHT);

		int width = 840;
		int height = (int)(orig_height * width / orig_width);

		vc.set(Videoio.CAP_PROP_FRAME_WIDTH, width);
		vc.set(Videoio.CAP_PROP_FRAME_HEIGHT, height);

		Mat cameraMatrix = new Mat();
		Mat distCoeffs = new Mat();

		readCalibrationData("res/new-life-size-vidoe-calib.txt", cameraMatrix, distCoeffs);

		Mat mat = new Mat();
		if(vc.isOpened()) {
			while(true) {
				if(vc.read(mat)) {
					Point3 result = findBall(mat, width, height, cameraMatrix, distCoeffs);
					if (result != null) {
						System.out.println(Double.toString(result.x) + ", " + Double.toString(result.y) + ", " + Double.toString(result.z));
					}
				}
			}
		}
	}*/
}
