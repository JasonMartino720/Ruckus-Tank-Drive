package org.usfirst.frc.team5030.robot;

import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Button;
//import org.usfirst.frc.team5030.robot.commands.ExampleCommand;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI
{
   
	public static Joystick DriverStick = new Joystick(0); //Xbox Controller for Driver
		static Button manipulatorUp = new JoystickButton(DriverStick, 6); //Defense arm up
		static Button manipulatorDown = new JoystickButton(DriverStick, 5); //Defense arm down	
		static Button stackMode = new JoystickButton(DriverStick,1); // Slows drivetrain for fine manuevers
		static Button dFlash = new JoystickButton(DriverStick,2);
		static Button override = new JoystickButton(DriverStick, 3);
	
	public static Joystick OperatorStick = new Joystick(1);
		
		static Button intakeActUp = new JoystickButton(OperatorStick, 4); //intake up
		static Button intakeActDown = new JoystickButton(OperatorStick, 2);//intake down
		static Button intakeIn = new JoystickButton(OperatorStick, 6);
		static Button intakeOut = new JoystickButton(OperatorStick, 5);
		static Button lowGoal = new JoystickButton(OperatorStick, 3);
		static Button dartPosBatter = new JoystickButton(OperatorStick , 8);
		static Button oFlash = new JoystickButton(OperatorStick, 1);
		
		
}