package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.CameraServer;
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
         armEncoder.setMaxPeriod(0.05);
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

     public void armUp(double d){
          if(joystick1.getRawButton(3))
            return;
          joystickArmValue += (joystickArmValue - d)*0.01;
          lArmSpark.set(joystickArmValue*0.65);
          rArmSpark.set(joystickArmValue*0.65);
     }

     @Override
     public void teleopPeriodic() {

         //Encoder
          System.out.println(armEncoder.getDistance()/128);
         //Arm
`	  joystickArmValue = -joystick1.getRawAxis(1);
	  //This is the code to edit
	  /*
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  *
	  */
	  if(joystickArmValue > -0.05 || joystickArmValue < 0.05){
	       //Here is the number to edit
	       joystickArmValue = 0.1
	  }
          lArmSpark.set(joystickArmValue);
          rArmSpark.set(joystickArmValue);

         //intake
         if(joystick1.getRawButton(1)){
             joystickWheelSpeedValue = 0.85;
         }else if(joystick1.getRawButton(2)){
             joystickWheelSpeedValue = -0.85;
         }else{
             joystickWheelSpeedValue = -0.2;
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
