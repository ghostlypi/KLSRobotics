package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
// import edu.wpi.first.wpilibj.networktables.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.DriverStation;

import com.revrobotics.ColorSensorV3;

public class Robot extends TimedRobot {

     //time
     long start;
     long now;

     //Drive
     DifferentialDrive myDrive;
     Spark lBank;
     Spark rBank;

     //Camera
     private UsbCamera camera;

     //Encoders
     Encoder encoder1;
     Encoder encoder2;

     //Joysticks
     Joystick joystick0 = new Joystick(0);
     Joystick joystick1 = new Joystick(1);
     double joystickLValue;
     double joystickRValue;

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
         //Color Sensor
         lastColor = "";
         //Encoder
         encoder1 = new Encoder(0,1);
         encoder1.reset();
         encoder2 = new Encoder(2,3);
         encoder2.reset();
     }

     @Override
     public void autonomousInit() {
         start = System.currentTimeMillis();
     }
     /**
      * This function is called periodically during autonomous
      */
     @Override
     public void autonomousPeriodic() {
        myDrive.tankDrive(1, 1);
     }

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
         //Automated Spinner
         if(joystick0.getRawButton(11)) colorCount = 0;
         if(colorCount <= 32 && joystick0.getRawButton(7)){
            joystickLValue = 0.5;
            joystickRValue = 0.5;
         }else if(joystick0.getRawButton(8) && !colorString.equals(ds.getGameSpecificMessage())){
            joystickLValue = 0.5;
            joystickRValue = 0.5;
         }else{
            joystickLValue = 0;
            joystickRValue = 0;
         }

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