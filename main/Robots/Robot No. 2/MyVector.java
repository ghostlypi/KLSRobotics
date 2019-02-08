package frc.robot;

// super simple vector class that I created.
// contains x,y,and angle.

public class MyVector {

	private double x;
	private double y;
	private double angle;

	public MyVector() {
		this.setX(0);
		this.setY(0);
		this.setAngle(0);
	}

	public MyVector(double x,double y,double angle) {
		this.setX(x);
		this.setY(y);
		this.setAngle(angle);
	}

	public double getX() { return x; }
	public double getY() { return y; }
	public double getAngle() { return angle; }

	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	public void setAngle(double angle) { this.angle = angle; }

	public String toString() { return "x: " + Double.toString(getX()) + ", y: " + Double.toString(getY()) + ", angle: " + Double.toString(getAngle()); }
}
