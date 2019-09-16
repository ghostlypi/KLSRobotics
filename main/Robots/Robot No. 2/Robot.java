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
        double z = -1.0; // this is the scale for the amount above the minimum speed that the arm travels.

        if( j + baseLineAngle > armEncoder.getDistance() / 128 && armEncoder.getRate() / 128 > k + baseLineAngle ) {

            return 0.0;

        } else if( j + baseLineAngle > armEncoder.getDistance() / 128 ) {

            return -0.3+(((armEncoder.getDistance() / 128)-(baseLineAngle + j - x)/y))*z; //Formerly -0.4; x = length of minimum speed; y = scale for distance of acceleration; z = 

        } else if( armEncoder.getDistance() / 128 > k + baseLineAngle ) {

            return -0.1+(((armEncoder.getDistance() / 128)-(baseLineAngle + k + x)/y))*z;

        } else {

            return 0.0;

        }

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

         joystickLValue = (-joystick0.getRawAxis(1) + joystick0.getRawAxis(2));

         joystickRValue = (-joystick0.getRawAxis(1) - joystick0.getRawAxis(2));



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
