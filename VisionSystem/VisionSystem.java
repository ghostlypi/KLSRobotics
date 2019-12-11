import org.opencv.core.Core;
import org.opencv.core.Mat;
// https://wpilib.screenstepslive.com/s/currentCS/m/vision/l/708159-using-multiple-cameras
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.CvSink;
import edu.wpi.first.wpilibj.CameraServer;
// https://www.geeksforgeeks.org/java-util-timer-class-java/
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
// https://wpilib.screenstepslive.com/s/currentCS/m/75361/l/843361-what-is-networktables
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

class VisionSystemUtil {
  
  public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
  private Mat previousMat;
  
  public VisionSystemUtil() {
    this.previousMat = null;
  }
  
  private int _rgbToInt(int r, int g, int b) {
    return r * 1000000 + g * 1000 + b;
  }
  
  private String _processZeros(int j) {
    String result = "";
    while(j >= 26) {
      j -= 26;
      result += "z";
    }
    if(result == 0) { return result; }
    return result + Character.toString(ALPHABET.charAt(j - 1));
  }
  
  private String _matToString(Mat mat, boolean compress) {
    String result;
    // https://stackoverflow.com/questions/17035005/using-get-and-put-to-access-pixel-values-in-opencv-for-java
    double[] temp = new double[(int)(mat.total() * mat.channels())];
    mat.get(0, 0, temp);
    if(compress) {
      result = "";
      int j;
      for(int i=0;i<temp.length;) {
        for(j=0;i+j<temp.length&&temp[i+j]==0;j++) {}
        if(j > 0) {
          result += this._processZeros(j);
          i += j;
        }
        else {
          result += Double.toString(temp[i++]);
        }
      }
    }
    else {
      result = Arrays.toString(temp).replace(", ",",");
    }
    return result;
  }
  
	public String compress(Mat mat) {
		String result;
		if(this.previousMat == null) {
		  result = "r" + this._matToString(mat, false);
		} else {
		  // https://stackoverflow.com/questions/18987371/mat-subtraction-with-opencv-in-java
		  Core.subtract(mat, this.previousMat, mat);
		  result = "c" + this._matToString(mat, true);
		}
		this.previousMat = mat;
		return result;
	}
}

class VisionSystemTask extends TimerTask {

	private CvSource source;
	private Mat currentMat;
	private NetworkTableEntry entry;
  private VisionSystemUtil util;

	public VisionSystemTask(CvSource source) {
		super();
		this.source = source;
		this.util = new VisionSystemUtil();

		NetworkTableInstance inst = NetworkTableInstance.getDefault();
		NetworkTable table = inst.getTable("datatable");
		this.entry = table.getEntry("frame");
	}

	public void run() {
		source.putFrame(this.currentMat);
		this.entry.setString(this.util.compress(this.currentMat));
	}
}

public class VisionSystem {

	private static CvSource source;
	private static final long TIMER_DELAY = 100;
	private static final long TIMER_PERIOD = 100;

	public static void start() {
		source = CameraServer.getInstance().getVideo();
		Timer timer = new Timer();
		VisionSystemTask task = new VisionSystemTask(source);
		timer.scheduleAtFixedRate(task, TIMER_DELAY, TIMER_PERIOD);
	}
}
