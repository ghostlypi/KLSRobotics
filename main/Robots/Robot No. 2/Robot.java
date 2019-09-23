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

     Joystick joystick0 = new Joystick( 0 );

     Joystick joystick1 = new Joystick( 1 );



    @Override

     public void robotInit() {

	 new Thread(){
		 
		 UsbCamera camera = new UsbCamera(“cam0”,0);
		 camera.setFPS(15);
		 camera.setResolution(1280, 720);
		 CameraServer.getInstance().startAutomaticCapture(camera);
		 
	 }.start();
	     
         //Drive Train

         myDrive = new RobotDrive( 0, 1 );



         //Arm

         lArmSpark = new Spark( 2 );

         rArmSpark = new Spark( 3 );



         //Intake

         leftTalon = new TalonSRX( 0 );

         rightTalon = new TalonSRX( 1 );



         //encoder stuff

         armEncoder  = new Encoder( 0, 1, true, Encoder.EncodingType.k4X );

         baseLineAngle = armEncoder.getDistance();

         armEncoder.setMaxPeriod( 0.05 );

         armEncoder.setMinRate( 10 );

         armEncoder.setDistancePerPulse( 2.8125 * 15 / 42 );

         armEncoder.setSamplesToAverage( 10 );

         armEncoder.reset();

     }



     @Override

     public void autonomousInit() {

         start = System.currentTimeMillis();

         teleopPeriodic();

     }

     /*
      * This function is called periodically during autonomous
      */

     @Override

     public void autonomousPeriodic() {

           teleopPeriodic();

     }

     /*
      * Function "autoArmUp()" is a function that returns the speed the arm motors should
      * run at based on the target range you want the arm to move to, that being the low
      * and high ends of the range, as j and k respectively.  This function uses "doIntegral()".
      */

     public double autoArmUp( double j, double k ) {

        if( j + baseLineAngle > armEncoder.getDistance() / 128 && armEncoder.getRate() / 128 > k + baseLineAngle ) {

            return -0.2;

        } else if( j + baseLineAngle > armEncoder.getDistance() / 128 ) {

            return doIntegral( -0.5, -0.3, ( j + k ) / 2 );

        } else if( armEncoder.getDistance() / 128 > k + baseLineAngle ) {

            return doIntegral( -0.025, -0.15, ( j + k ) / 2 );

        } else {

            return -0.2;

        }

     }

     /*
      * Function "doIntegral()" is a function that returns the speed the arm motors should
      * run at based on the highest speed you want the motor to run at, the lowest speed,
      * and the target angle.  Slows the arm as it gets closer to the target.
      */

     public double doIntegral( double max, double min, double targetAngle ) {

        distanceToTarget = abs( armEncoder.getDistance() / 128 - targetAngle );


        if( distanceToTarget > 0.1 ) {

            return max;

        } else {

            return 10 * ( max - min ) * distanceToTarget + min;

            /* The formula is effectively this: y = 10( max - min )x + min
             * Once the distance between the arm position and the target is 0.1 or less,
             * it becomes the motor speed as a function of how close the target is.
             */ 

        }

     }

     // The absolute value function from "Math," however it failed to import
     // when trying "Math.abs()," so reverted to using the function pulled from Math.

     public static double abs(double a) {

        return (a <= 0.0F) ? 0.0F - a : a;

    }



     @Override

     public void teleopPeriodic() {



       // Encoder
        // ENCODER VALUE / 128 DEBUG PRINT STATEMENT
        // System.out.println(armEncoder.getDistance()/128);
        /* ARM PRESETS
           Each set of numbers going into the function "autoArmUp()"
           represent the low and high bounds for a range that the arm
           needs to move to.
           Still need the presets for the following:
            - Cargo ship hatch
            - Cargo ship cargo
         */

        double cv = 0.05; // Calibration value

        if( joystick1.getRawButton( 7 ) ) { // TOP ROCKET HATCH

          joystickArmValue = autoArmUp( 0.8, 0.89 );

        } else if( joystick1.getRawButton( 9 ) ) { // MID ROCKET HATCH

          joystickArmValue = autoArmUp( 0.51, 0.6 );

        } else if( joystick1.getRawButton( 11 ) ) { // BOT ROCKET HATCH

          joystickArmValue = autoArmUp( 0.125, 0.23 );

        } else if( joystick1.getRawButton( 8 ) ) { // TOP ROCKET CARGO

          joystickArmValue = autoArmUp( 0.93 + cv, 0.941 + cv );

        } else if( joystick1.getRawButton( 10 ) ) { // MID ROCKET CARGO

          joystickArmValue = autoArmUp( 0.63 + cv, 0.66 + cv );

        } else if( joystick1.getRawButton( 12 ) ) { // BOT ROCKET CARGO

          joystickArmValue = autoArmUp( 0.345 + cv, 0.36 + cv );

        } else if( joystick1.getRawButton( 5 ) ) {

          joystickArmValue = autoArmUp( 0.06, 0.0625 );

        } else {

           joystickArmValue = -joystick1.getRawAxis( 1 );
           
           // Auto Hold Arm
           if( joystickArmValue > -0.2 && joystickArmValue < 0.2 ) {
               
               joystickArmValue = -0.2;

           }

        }

       //Emergency Shutoff

        if( joystick1.getRawButton( 3 ) ) {

          joystickArmValue = -0.1;

        } else if( joystick1.getRawButton( 4 ) ) {

          joystickArmValue = -0.35;

        }

          lArmSpark.set( joystickArmValue );

          rArmSpark.set( joystickArmValue );



         //intake

         if( joystick1.getRawButton( 1 ) ) {

             joystickWheelSpeedValue = 0.85;

         } else if( joystick1.getRawButton( 2 ) ) {

             joystickWheelSpeedValue = -0.85;

         } else {

             joystickWheelSpeedValue = ( -joystick1.getRawAxis( 3 ) + 1 ) / 2 * -0.2;

         }



        leftTalon.set( ControlMode.PercentOutput, -joystickWheelSpeedValue );

        rightTalon.set( ControlMode.PercentOutput, joystickWheelSpeedValue );



         //Drive Train

         /* The purpose of the variable "limitTurnSpeed" is for if the arm is raised
            above a particular ( and low ) height. */

         limitTurnSpeed = 1;

         if( armEncoder.getDistance() / 128 > 0.1 ) { 

            limitTurnSpeed = 0.6;

         }
         
         joystickLValue = ( -joystick0.getRawAxis( 1 ) + joystick0.getRawAxis( 2 ) * limitTurnSpeed );

         joystickRValue = ( -joystick0.getRawAxis( 1 ) - joystick0.getRawAxis( 2 ) * limitTurnSpeed );

         if( joystick0.getRawButton( 1 ) ) { // If calebsTriggerMode ...

             myDrive.tankDrive( 0.6 * joystickLValue, 0.6 * joystickRValue );

         } else if( joystick0.getRawButton( 7 ) ) { // Forwards at 0.3 speed

            myDrive.tankDrive( 0.5 , 0.5 );

         } else if( joystick0.getRawButton( 11 ) ) { // Backwards at -0.3 speed

            myDrive.tankDrive( -0.5, -0.5 );

         } else if( joystick0.getRawButton( 9 ) ) { // Rotate left at 0.3 speed

            myDrive.tankDrive( -0.5, 0.5 );

         } else if( joystick0.getRawButton( 10 ) ) { // Rotate right at 0.3 speed

            myDrive.tankDrive( 0.5, -0.5 );

         } else {

             myDrive.tankDrive( joystickLValue, joystickRValue );
            
         }

     }



     @Override

     public void testPeriodic() {

     }

}
