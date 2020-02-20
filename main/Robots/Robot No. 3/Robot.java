package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
// import edu.wpi.first.wpilibj.networktables.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.DriverStation;

import com.revrobotics.ColorSensorV3;

public class Robot extends TimedRobot {

     //Drive
     DifferentialDrive myDrive;
     Spark lBank;
     Spark rBank;
     boolean calebsTriggerMode;

     //Winch
     Spark winch;
     double winchValue;
     TalonSRX hook;

     //Gun
      Spark gun;
      VictorSPX bottom;
      VictorSPX top;
      VictorSPX intake;
      TalonSRX holding;

     //Encoders
     Encoder encoder1;
     Encoder encoder2;

     //Joysticks
     Joystick joystick0 = new Joystick(0);
     Joystick joystick1 = new Joystick(1);
     double joystickLValue;
     double joystickRValue;
     //Contorl Panel
      TalonSRX panelMotor;
      //Color Sensor
        int colorCount;
        String lastColor;
        I2C.Port i2cPort = I2C.Port.kOnboard;
        ColorSensorV3 colorSensor = new ColorSensorV3(i2cPort);
     
    @Override
     public void robotInit() {
         //Drive Train
         rBank = new Spark(0);
         lBank = new Spark(1);
         myDrive = new DifferentialDrive(rBank, lBank);
         //Winch + hook
         winch = new Spark(2);
         //MAKE SURE TO CHANGE WITH CORRECT DEVICE NUMBER BELOW
         hook = new TalonSRX(0);
         //Gun
         intake = new VictorSPX(3);
         top = new VictorSPX(4);
         bottom = new VictorSPX(5);
         //Color Sensor
         lastColor = "";
         panelMotor = new TalonSRX(1);
         //Encoder
         encoder1 = new Encoder(0,1);
         encoder1.reset();
         encoder2 = new Encoder(2,3);
         encoder2.reset();
     }

     @Override
     public void autonomousInit() {
     }
     /**
      * This function is called periodically during autonomous
      */
     @Override
     public void autonomousPeriodic() {
        myDrive.tankDrive(1, 1);
     }
     /* What the lines mean below:
     Each color has a percentage that we calculated via the color sensor & the smart dashboard.
     After collecting that data, we made it so that the approximated value (the detected one)
     was compared with the plus and minus of the range, which is defined as double range.
     
     An example:
     If the detected red value from the sensor is less than or equal to 15.51%+the pre-defined range, and greater than
     or equal to 15.51%-the range.
     This goes through the red value, green value, and the blue value, if it is true, say that the color is Red on the smart dashboard.
     
     This happens for the detection of red, blue, yellow, and green, each with their individual numbers, but the range remains all the same.
     */
     public String getColor(){
        String colorString;
        double range = 0.05;
        Color detectedColor = colorSensor.getColor();
         if ((detectedColor.red <= 0.1551+range && detectedColor.red >= 0.1551-range)&&(detectedColor.green <= 0.4444+range && detectedColor.green >= 0.4444-range)&&(detectedColor.blue <= 0.4001+range && detectedColor.blue >= 0.3901-range)) {
            colorString = "Red";
         } else if ((detectedColor.red <= 0.5173+range && detectedColor.red >= 0.4073-range)&&(detectedColor.green <= 0.3488+range && detectedColor.green >= 0.3488-range)&&(detectedColor.blue <= 0.134+range && detectedColor.blue >= 0.134-range)) {
            colorString = "Blue";
         } else if ((detectedColor.red <= 0.1899+range && detectedColor.red >= 0.1899-range)&&(detectedColor.green <= 0.5598+range && detectedColor.green >= 0.5598-range)&&(detectedColor.blue <= 0.2501+range && detectedColor.blue >= 0.2501-range)) {
            colorString = "Yellow";
         } else if ((detectedColor.red <= 0.3271+range && detectedColor.red >= 0.3271-range)&&(detectedColor.green <= 0.5385+range && detectedColor.green >= 0.5385-range)&&(detectedColor.blue <= 0.134+range && detectedColor.blue >= 0.134-range)) {
            colorString = "Green";
         } else {
            colorString = "Unknown";
         }
        return colorString;
     }

     @Override
     public void teleopPeriodic() {

         //Color Sensor
         Color detectedColor = colorSensor.getColor();
         String colorString = getColor();
         DriverStation ds = DriverStation.getInstance();

         if(!lastColor.equals(colorString) && (colorString == "Red"||colorString == "Blue"||colorString == "Yellow"||colorString == "Green")){
            colorCount++;
            lastColor = colorString;
         }
         
         //Essentially put the specified values on the smart dashboard
         SmartDashboard.putNumber("Red", detectedColor.red);
         SmartDashboard.putNumber("Green", detectedColor.green);
         SmartDashboard.putNumber("Blue", detectedColor.blue);
         SmartDashboard.putString("Detected Color", colorString);
         SmartDashboard.putNumber("colorCount", colorCount);
         SmartDashboard.putString("End Color", DriverStation.getInstance().getGameSpecificMessage());
         SmartDashboard.putNumber("Encoder", encoder1.getDistance());
         SmartDashboard.putNumber("Encoder", encoder2.getDistance());

         //Drive Train
         joystickLValue = (-joystick0.getRawAxis(1) + joystick0.getRawAxis(2));
         joystickRValue = (-joystick0.getRawAxis(1) - joystick0.getRawAxis(2));
         
         //Winch Up/down
         winchValue = (-joystick1.getRawAxis(1));
         winch.set(winchValue);

         //Hook up and down
         if (joystick1.getRawButton(1)){
          hook.set(ControlMode.PercentOutput, 0.8);
         } else if (joystick1.getRawButton(2)){
          hook.set(ControlMode.PercentOutput, -0.8);
         }

         //Fire
         gun.set(joystick0.getRawAxis(3));
         if(joystick0.getRawButton(1)) holding.set(ControlMode.PercentOutput,1);
         
         //Intake
         intake.set(ControlMode.PercentOutput, 1);
         top.set(ControlMode.PercentOutput, 1);
         bottom.set(ControlMode.PercentOutput, 1);

         //Automated Spinner
         //If there are 32 color changed (~4 rotations),
         //it makes the wheel for the control panel no longer spin.
         //Otherwise, spin at 1/2 speed. 
         if(joystick1.getRawButton(11)) colorCount = 0;
         if(colorCount <= 32 && joystick1.getRawButton(7)){
            panelMotor.set(ControlMode.PercentOutput, 0.5);
         }else if(joystick1.getRawButton(8) && !colorString.equals(ds.getGameSpecificMessage())){
            panelMotor.set(ControlMode.PercentOutput, 0.5);
         }else{
            panelMotor.set(ControlMode.PercentOutput, 0);
         }

         //Possible align to ball
         //Todo, Abhi has to program getBallValue
         double degreesForBall = getBallValue();
         double rangeForBall = 10;
         if (joystick1.getRawButton(3) || joystick1.getRawButton(5)){
           if (degreesForBall >= rangeForBall){
              joystickLValue = -0.2;
              joystickRValue = 0.2;
           } else if (degreesForBall <= -rangeForBall){
              joystickLValue = 0.2;
              joystickRValue = -0.2;
           }
         }

         //Possible allign to target
         //NOTE: if proven to work well, replace getAngleFrontPortValue() with getAngleBackPortValue().
         double degreesForTarget = getAngleFrontPortValue();
         double rangeForTarget = 2;
         if (joystick1.getRawButton(4) || joystick1.getRawButton(6)){
            if (degreesForTarget >= rangeForTarget) {
               joystickLValue = -0.2;
               joystickRValue = 0.2;
            } else if (degreesForTarget <= -rangeForTarget) {
               joystickLValue = 0.2;
               joystickRValue = -0.2;
            }
         }
         
         //Reset Encoders
         if (joystick0.getRawButton(9)){
            encoder1.reset();
            encoder2.reset();
         }

         if(joystick0.getRawButton(11)) calebsTriggerMode = true;
         if(joystick0.getRawButton(12)) calebsTriggerMode = false;
         if(calebsTriggerMode)
            myDrive.tankDrive(0.6 * joystickLValue, 0.6 * joystickRValue);
         else
            myDrive.tankDrive(joystickLValue, joystickRValue);
     }

     @Override
     public void testPeriodic() {
     }
}
