package org.usfirst.frc.team6962.robot;

import com.ctre.CANTalon;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	long start;
	RobotDrive myDrive;
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	int i = 0;

	WPI_TalonSRX leftTalon = new WPI_TalonSRX(0);
	WPI_TalonSRX rightTalon = new WPI_TalonSRX(1);
	double joystickLValue;
	double joystickRValue;
	double joystickArmValue1, joystickArmValue2;
	double joystickIntakeVale;
	double joystickShoot;

	Joystick joystick0 = new Joystick(0);
	Joystick joystick1 = new Joystick(1);
	Spark armSpark = new Spark(2);
	Encoder armEncoder = new Encoder(0, 1, false, Encoder.EncodingType.k2X);
	PIDController armPID = new PIDController(0.1, 0.0, 0, armEncoder, armSpark);
	
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>(); 

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
		leftTalon.setInverted(true);
		armEncoder.reset();
		chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("My Auto", customAuto);
		SmartDashboard.putData("Auto choices", chooser);
		myDrive = new RobotDrive(0, 1);
		armPID.disable();
		armPID.setContinuous(false);
		armPID.setInputRange(0, 100);
		armPID.setAbsoluteTolerance(10);
		CameraServer.getInstance().startAutomaticCapture().setResolution(640, 480);
	}
 
	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
	 * remove all of the chooser code and uncomment the getString line to get the
	 * auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the SendableChooser
	 * make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		start = System.currentTimeMillis();
	}
	/**
	 * This function is called periodically during autonomous
	 */
	@SuppressWarnings("deprecation")
	@Override
	
	public void autonomousPeriodic() {
		long current = System.currentTimeMillis();
		if(current - start <= 2000) {
			myDrive.arcadeDrive(0.75,0.1);
		}
		else
		{
			myDrive.arcadeDrive(0,0);
		}	
	}
	
	@Override
	public void teleopInit() {
		//armPID.enable();
	}
	
	@Override
	public void robotPeriodic() {
		SmartDashboard.putNumber("Encoder", armEncoder.get());
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		joystickLValue = -joystick1.getRawAxis(1);
		joystickRValue = -joystick1.getRawAxis(2);
		joystickArmValue1 = -joystick0.getRawAxis(5);
		joystickArmValue2 = joystick0.getRawAxis(1);
		double inTalon = joystick0.getRawAxis(3);
		double outTalon = -joystick0.getRawAxis(2);
		double armCount = armEncoder.get();
			
		armSpark.set((joystickArmValue1 - joystickArmValue2)/2);
		
		//armPID.setSetpoint(0);
		
		
		if (Math.abs(inTalon + outTalon) < 0.1) {
		    leftTalon.set(0.15);
		    rightTalon.set(0.15);
		} else {
		    leftTalon.set(((inTalon * 0.9) + outTalon));
		    rightTalon.set(((inTalon * 0.9) + outTalon));
		}
		//robot drives w/ out input
		myDrive.arcadeDrive(joystickLValue, joystickRValue);
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}
