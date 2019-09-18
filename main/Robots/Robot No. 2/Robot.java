package frc.robot;



import edu.wpi.first.wpilibj.Joystick;

import edu.wpi.first.wpilibj.RobotDrive;

import edu.wpi.first.wpilibj.Spark;

import edu.wpi.first.wpilibj.TimedRobot;

import com.ctre.phoenix.motorcontrol.ControlMode;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.CameraServer;

import edu.wpi.cscore.CvSink;

import edu.wpi.cscore.CvSource;

import org.opencv.core.Mat;

import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.UsbCamera;

import edu.wpi.first.wpilibj.Encoder;



public class Robot extends TimedRobot {



     //time

     long start;

     long now;



     //Motors

     RobotDrive myDrive;

     Spark lArmSpark;

     Spark rArmSpark;

     TalonSRX leftTalon;

     TalonSRX rightTalon;



     //Encoder

     Encoder armEncoder;

     double baseLineAngle;



     //Stored Values

     double joystickLValue;

     double joystickRValue;

     double joystickArmValue;

     double joystickWheelSpeedValue;

     boolean lowerArm = false;

     double limitTurnSpeed = 1;

     double distanceToTarget;



     //Camera

     private UsbCamera camera;



     //Joysticks

     Joystick joystick0 = new Joystick(0);

     Joystick joystick1 = new Joystick(1);



    @Override

     public void robotInit() {

	 new Thread(() -> {
                UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
                camera.setResolution(640, 480);
                
                CvSink cvSink = CameraServer.getInstance().getVideo();
                CvSource outputStream = CameraServer.getInstance().putVideo("Blur", 640, 480);
                
                Mat source = new Mat();
                Mat output = new Mat();
                
                while(!Thread.interrupted()) {
                    cvSink.grabFrame(source);
                    Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
                    outputStream.putFrame(output);
                }
            }).start();
	     
         //Drive Train

         myDrive = new RobotDrive(0, 1);



         //Arm

         lArmSpark = new Spark(2);

         rArmSpark = new Spark(3);



         //Intake

         leftTalon = new TalonSRX(0);

         rightTalon = new TalonSRX(1);



         //encoder stuff

         armEncoder  = new Encoder(0, 1, true, Encoder.EncodingType.k4X);

         baseLineAngle = armEncoder.getDistance();

         armEncoder.setMaxPeriod(0.05);

        //preset angles from starting angle 0.0

        //ROCKET HATCHES

        //rh bot is 0.23 to 0.125

        //rh mid is 0.6 to 0.51

        //rh top is 0.89 to 0.8

         armEncoder.setMinRate(10);

         armEncoder.setDistancePerPulse(2.8125 * 15/42);

         armEncoder.setSamplesToAverage(10);

         armEncoder.reset();

     }



     @Override

     public void autonomousInit() {

         start = System.currentTimeMillis();

         teleopPeriodic();

     }

     /**
      * This function is called periodically during autonomous
      */

     @Override

     public void autonomousPeriodic() {

           teleopPeriodic();

     }



     public double autoArmUp( double j, double k ) {

        // tweak these for more control over the arm.
        double x = 0.0; // this is the amount of "time" that the arm moves at minimum speed.
        double y = 1.0; // this is the scale factor for the duration of acceleration
        double z = 1.0; // this is the scale for the amount above the minimum speed that the arm travels.

        if( j + baseLineAngle > armEncoder.getDistance() / 128 && armEncoder.getRate() / 128 > k + baseLineAngle ) {

            return -0.2;

        } else if( j + baseLineAngle > armEncoder.getDistance() / 128 ) {

            return doIntegral( -0.5, -0.3, ( j + k ) / 2 ); //-0.4; //-0.3+((((armEncoder.getDistance() / 128)-(baseLineAngle + j - x))/y))*z; //Formerly -0.4; x = length of minimum speed; y = scale for distance of acceleration; z = 

        } else if( armEncoder.getDistance() / 128 > k + baseLineAngle ) {

            return doIntegral( -0.025, -0.15, ( j + k ) / 2 );  //+((((armEncoder.getDistance() / 128)-(baseLineAngle + k + x))/y))*z;

        } else {

            return -0.2;

        }

     }

     public double doIntegral( double max, double min, double targetAngle ) {

        distanceToTarget = abs( armEncoder.getDistance() / 128 - targetAngle );

        if( distanceToTarget > 0.1 ) {

            return max;

        } else {

            return 10 * ( max - min ) * distanceToTarget + min;

        }

     }

     public static double abs(double a) {

        return (a <= 0.0F) ? 0.0F - a : a;

    }



     @Override

     public void teleopPeriodic() {



       //Encoder

        System.out.println(armEncoder.getDistance()/128);

        if( joystick1.getRawButton( 7 ) ) { // TOP ROCKET HATCH

          joystickArmValue = autoArmUp( 0.8, 0.89 );

        } else if( joystick1.getRawButton( 9 ) ) { // MID ROCKET HATCH

          joystickArmValue = autoArmUp( 0.51, 0.6 );

        } else if( joystick1.getRawButton( 11 ) ) { // BOT ROCKET HATCH

          joystickArmValue = autoArmUp( 0.125, 0.23 );

        } else if( joystick1.getRawButton( 8 ) ) { // TOP ROCKET CARGO

          joystickArmValue = autoArmUp( 0.93, 0.941 );

        } else if( joystick1.getRawButton( 10 ) ) { // MID ROCKET CARGO

          joystickArmValue = autoArmUp( 0.63, 0.66 );

        } else if( joystick1.getRawButton( 12 ) ) { // BOT ROCKET CARGO

          joystickArmValue = autoArmUp( 0.345, 0.36 );

        } else {

           joystickArmValue = -joystick1.getRawAxis(1);
           
           // Auto Hold Arm
           if( joystickArmValue > -0.2 && joystickArmValue < 0.2 ) {
               
               joystickArmValue = -0.2;

           }

        }

       //Emergency Shutoff

        if(joystick1.getRawButton(3)){

          joystickArmValue = -0.1;

        }

          lArmSpark.set( joystickArmValue );

          rArmSpark.set( joystickArmValue );



         //intake

         if(joystick1.getRawButton(1)){

             joystickWheelSpeedValue = 0.85;

         }else if(joystick1.getRawButton(2)){

             joystickWheelSpeedValue = -0.85;

         }else{

             joystickWheelSpeedValue = (-joystick1.getRawAxis(3)+1)/2 * -0.2;

         }



        leftTalon.set(ControlMode.PercentOutput,-joystickWheelSpeedValue);

        rightTalon.set(ControlMode.PercentOutput,joystickWheelSpeedValue);



         //Drive Train

         limitTurnSpeed = 1;

         if( armEncoder.getDistance() / 128 > 0.1 ) { 

            limitTurnSpeed = 0.6;

         }
         
         joystickLValue = (-joystick0.getRawAxis(1) + joystick0.getRawAxis(2) * limitTurnSpeed );

         joystickRValue = (-joystick0.getRawAxis(1) - joystick0.getRawAxis(2) * limitTurnSpeed );



		     boolean calebsTriggerMode = joystick0.getRawButton(1);

		     if(calebsTriggerMode)

			      myDrive.tankDrive(0.6 * joystickLValue, 0.6 * joystickRValue);

		     else

		 	      myDrive.tankDrive(joystickLValue, joystickRValue);

     }



     @Override

     public void testPeriodic() {

     }

}
