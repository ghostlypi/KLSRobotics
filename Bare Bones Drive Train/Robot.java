package frc.robot;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Joystick;

/**
 * The VM is configured to automatically run this class, and to call the functions
 * corresponding to each mode. If you change the name of this class or the package
 * after creating this project, you must also update the build.gradle file in the project.
 */

public class Robot extends TimedRobot {
 //Defines start for the initialization of the robot.
 long start;
 //Defines motors for later use.
 RobotDrive myDrive;

 //Defines values of the position of the joysticks for later use.
 double joystickLValue;
 double joystickRValue;

 //Joysticks
 Joystick joystick0 = new Joystick( 0 );
 Joystick joystick1 = new Joystick( 1 );

 /**
  * This part is called for defining myDrive so it can be used later.
  */
 @Override
 public void robotInit() {
   //Drive Train
   myDrive = new RobotDrive( 0, 1 );
 }

  /**
   * This part simply starts the robot.
   */

  @Override
  public void autonomousInit() {
    start = System.currentTimeMillis();
  }
  
  /**
   * This function is called for drive control during operator control.
   */
  
  @Override
  public void teleopPeriodic() {
    //Drive Train
    joystickLValue = -joystick0.getRawAxis( 1 ) + joystick0.getRawAxis( 2 );
    joystickRValue = -joystick0.getRawAxis( 1 ) - joystick0.getRawAxis( 2 );
    myDrive.tankDrive( joystickLValue, joystickRValue );
  }
}
