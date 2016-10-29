//Jason is the greatest
package org.usfirst.frc.team5030.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick.RumbleType;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.communication.UsageReporting;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tInstances;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary.tResourceType;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import org.usfirst.frc.team5030.robot.commands.ExampleCommand;
import org.usfirst.frc.team5030.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;
import edu.wpi.first.wpilibj.buttons.*;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.ControllerPower;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Ultrasonic;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{
	static double driverBands = 0.08; //DeadBands for Xbox Controler
	static double operatorBands = 0.05; //DeadBands for Joystick
	static double potDeadband = 0.005; //Deadband around potAngle1 to eliminate oscillation
	final static double dis2Outerworks = 450; //Encoder distance to defenses
	final static double totalDistance = 1800; //Encoder distance to shooting position
	final static double lowbarDistance = 1800; //Possibly used to shoot in side goal from lowbar //TODO decide whether or not to use
	final static double potAngleBatter = 0; //TODO fill variables
	final static double potAngleLowbar = 0;
	static boolean secondStage = false;
	static boolean visionFinished = false;
	static boolean autoShotTaken = false;
	static boolean Flag1 = false;
	static int defenseSelected;
	static int positionSelected;
	static int operatorButtonID;
	static int driverButtonID;
	
	//Create Table
	static NetworkTable table;
	
	//Create Timer instance named timer
	//static Timer timer = new Timer();
	
	//Create Sendable chooser objects
	SendableChooser defenseSelection;
	SendableChooser positionSelection;
	
	//No idea what the fuck this is
	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	//RobotDrive drive;
	OI oi;
	Command autonomousCommand;
	RobotDrive drive;
	
	
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit()
    {
		oi = new OI();
		//Initialize RobotDrive object
		drive = new RobotDrive(RobotMap.lTalonFront, RobotMap.lTalonBack, RobotMap.rTalonFront, RobotMap.rTalonBack);
		//drive.setInvertedMotor(MotorType.kRearRight, true);
		
		//Create Sensor & Limit objects
		RobotMap.topHall = new DigitalInput(9);
		RobotMap.bottomHall = new DigitalInput(8);
		RobotMap.leftLimit = new DigitalInput(7);
		RobotMap.rightLimit = new DigitalInput(4);
		RobotMap.actLimit = new DigitalInput(6);
		RobotMap.middleHall = new DigitalInput(69); //TODO placeholder
		
		//Create Analog UltraSonic
		//RobotMap.ultrasonic = new AnalogInput(2);
		
		//Create Analog Pot
		AnalogInput potInput = new AnalogInput(3);
		RobotMap.pot = new AnalogPotentiometer(potInput);
		
		//Create Encoders
		RobotMap.leftEnc = new Encoder(2,3,false, Encoder.EncodingType.k4X);
		RobotMap.rightEnc = new Encoder(0,1,false, Encoder.EncodingType.k4X);
        
		//Camera for DS
		//CameraServer cam = CameraServer.getInstance();	
		
		//Create & Start Camera
  		//cam.setQuality(100);							
  		//cam.startAutomaticCapture("cam0");				
  		autonomousCommand = new ExampleCommand();
  		
  		//Create and determine Network Table
  		table = NetworkTable.getTable("SmartDashboard");
  		
  		//DEFENSE Sendable Chooser
  		defenseSelection = new SendableChooser();
  		defenseSelection.addDefault("Default", 0);
  		defenseSelection.addObject("Drive Forward", 1);
  		SmartDashboard.putData("Autonomous mode selection", defenseSelection);
  		
  		//POSITION Sendable Chooser
  		positionSelection = new SendableChooser();
  		positionSelection.addObject("Default", 0);
  		positionSelection.addObject("1", 1);
  		positionSelection.addObject("2", 2);
  		positionSelection.addObject("3", 3);
  		positionSelection.addObject("4", 4);
  		positionSelection.addObject("5", 5);
  		SmartDashboard.putData("Defense Position Selection" , positionSelection);
  		
    }
	
	public void disabledPeriodic()
	{
		Scheduler.getInstance().run();
	}
		
    public void autonomousInit()
    {
        // schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();
        
        
        defenseSelected = (int)defenseSelection.getSelected();
        positionSelected = (int)positionSelection.getSelected();
        
        
        //Decide which start function to use based on defense selected
        /*
        if(defenseSelected == 0)
        {
        	
        }
        else if(defenseSelected == 1)
        {
        	lowbarAutoStart();
        }
        else if(defenseSelected == 2)
        {
        	lowbarAutoStart();
        }
        else if(defenseSelected == 7)
        {
        	lowbarAutoStart();
        }
        else if(defenseSelected == 8)
        {
        	lowbarAutoStart();
        }
        else
        {
        	defenseAutoStart();
        }
        */
        lowbarAutoStart();
        
        	
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic()
    {
        Scheduler.getInstance().run();
        
        
        double lEnc = RobotMap.leftEnc.get();
        double rEnc = -RobotMap.rightEnc.get();
        //double autoTime = timer.getMatchTime();
      
        
        //timer.start();
        
        /*if(!autoShotTaken) //== false && autoTime > 12)
        {
        	RobotMap.lFlywheel.set(1.0);
        	RobotMap.rFlywheel.set(-1.0);
        	Timer.delay(2.0);
        	
        	RobotMap.rBelt.set(1.0);
        	RobotMap.lBelt.set(-1.0);
        	Timer.delay(0.5);
        	
        	RobotMap.rBelt.set(0.0);
        	RobotMap.lBelt.set(0.0);
        	RobotMap.rFlywheel.set(0.0);
        	RobotMap.lFlywheel.set(0.0);
        	Timer.delay(0.5);
        }
        else
        {      
        */
	        if(secondStage == false)
	        {
	        	switch(defenseSelected)
	        	{
	        	
	        		case 0: RobotMap.intake.set(0.0);
							RobotMap.rBelt.set(0.0);
			        		RobotMap.lBelt.set(0.0);
			        		RobotMap.rFlywheel.set(0.0);
			        		RobotMap.lFlywheel.set(0.0);
			        		RobotMap.intakeAct.set(0.0);
			        		RobotMap.man.set(0.0);
			        		OI.DriverStick.setRumble(RumbleType.kLeftRumble, (float)0.0);
			        		OI.DriverStick.setRumble(RumbleType.kRightRumble, (float)0.0);
			        		
	        		//PORTCULLIS 
	        				//Driving to Outerworks
	        		case 1:	if(lEnc < totalDistance && rEnc < totalDistance)//1507
	        				{
	                			drive.tankDrive(0.5, 0.0, true);
	        				}
	        				else
	        				{
	        					
	        					drive.tankDrive(0.0, 0.0, true);
	        					        					
	        				}
	        		
	        		break;
	        		
	        				
	        	}
		        
	        }
	        else
	        {
	        	switch(positionSelected)
	        	{
	        		//CASES MATCH POSITIONS
	        				//Complete Stop
	        		case 0: RobotMap.intake.set(0.0);
							RobotMap.rBelt.set(0.0);
			        		RobotMap.lBelt.set(0.0);
			        		RobotMap.rFlywheel.set(0.0);
			        		RobotMap.lFlywheel.set(0.0);
			        		RobotMap.intakeAct.set(0.0);
			        		RobotMap.man.set(0.0);
			        		OI.DriverStick.setRumble(RumbleType.kLeftRumble, (float)0.0);
			        		OI.DriverStick.setRumble(RumbleType.kRightRumble, (float)0.0);
	        		
			        		//CW turn
	        		case 1: if(lEnc < totalDistance + 200)
	        				{
	        					drive.tankDrive(0.5, 0.0);
			    	        }
	        				else
	        				{
	        					drive.tankDrive(0.0, 0.0);
//Code for Middle Hall if necessary
/*
 if(!RobotMap.middleHall.get())
 {
	 RobotMap.dart.set(0.6);
 }
 else
 {
	 RobotMap.dart.set(0.0);
 	IN HERE GOES EVERYTHING FROM 
 	ROBOTMAP.FLASH TO THE END
 */
	        					RobotMap.flash.set( 6 / ControllerPower.getInputVoltage()); 
	                			
	        	        		if(RobotMap.pot.get() < 0.205)
	        					{
	        						RobotMap.dart.set(0.6);
	        					}
	        					else if(RobotMap.pot.get() > 0.215)
	        					{
	        						RobotMap.dart.set(-0.6);
	        					}
	        					else
	        					{
	        						RobotMap.dart.set(0.0);
	        						if(!Flag1)
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.rFlywheel.set(-1.0);
	                				RobotMap.lFlywheel.set(1.0);
	                				Timer.delay(2.2);
	                				RobotMap.rFlywheel.set(-1.0);
	            					RobotMap.lFlywheel.set(1.0);
	            					RobotMap.lBelt.set(-1.0);
	            					RobotMap.rBelt.set(1.0);
	            					Timer.delay(0.5);
	            					Flag1 = true;
	                				}
	                				else
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.flash.set(0.0);
	            					RobotMap.rFlywheel.set(0.0);
	            					RobotMap.lFlywheel.set(0.0);
	            					RobotMap.lBelt.set(0.0);
	            					RobotMap.rBelt.set(0.0);
	                				}
	        					}
	        				}
	        				break;
	        				//CW Turn
	        		case 2: if(lEnc < totalDistance + 150)
	        				{
	        					drive.tankDrive(0.35, 0.0);
	        				}
	        				else
	        				{
	        					drive.tankDrive(0.0, 0.0);
	        					RobotMap.flash.set( 6 / ControllerPower.getInputVoltage()); 
	                			
	        	        		if(RobotMap.pot.get() < 0.205)
	        					{
	        						RobotMap.dart.set(0.6);
	        					}
	        					else if(RobotMap.pot.get() > 0.215)
	        					{
	        						RobotMap.dart.set(-0.6);
	        					}
	        					else
	        					{
	        						RobotMap.dart.set(0.0);
	        						if(!Flag1)
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.rFlywheel.set(-1.0);
	                				RobotMap.lFlywheel.set(1.0);
	                				Timer.delay(2.2);
	                				RobotMap.rFlywheel.set(-1.0);
	            					RobotMap.lFlywheel.set(1.0);
	            					RobotMap.lBelt.set(-1.0);
	            					RobotMap.rBelt.set(1.0);
	            					Timer.delay(0.5);
	            					Flag1 = true;
	                				}
	                				else
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.flash.set(0.0);
	            					RobotMap.rFlywheel.set(0.0);
	            					RobotMap.lFlywheel.set(0.0);
	            					RobotMap.lBelt.set(0.0);
	            					RobotMap.rBelt.set(0.0);
	                				}
	        					}
	        				}
	        				break;
	        				//CW Turn
	        		case 3: if(lEnc < 100)
	        				{
	        					drive.tankDrive(0.35 , 0.0);
	        				}
	        				else
	        				{
	        					drive.tankDrive(0.0, 0.0);
	        					RobotMap.flash.set( 6 / ControllerPower.getInputVoltage()); 
	                			
	        	        		if(RobotMap.pot.get() < 0.205)
	        					{
	        						RobotMap.dart.set(0.6);
	        					}
	        					else if(RobotMap.pot.get() > 0.215)
	        					{
	        						RobotMap.dart.set(-0.6);
	        					}
	        					else
	        					{
	        						RobotMap.dart.set(0.0);
	        						if(!Flag1)
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.rFlywheel.set(-1.0);
	                				RobotMap.lFlywheel.set(1.0);
	                				Timer.delay(2.2);
	                				RobotMap.rFlywheel.set(-1.0);
	            					RobotMap.lFlywheel.set(1.0);
	            					RobotMap.lBelt.set(-1.0);
	            					RobotMap.rBelt.set(1.0);
	            					Timer.delay(0.5);
	            					Flag1 = true;
	                				}
	                				else
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.flash.set(0.0);
	            					RobotMap.rFlywheel.set(0.0);
	            					RobotMap.lFlywheel.set(0.0);
	            					RobotMap.lBelt.set(0.0);
	            					RobotMap.rBelt.set(0.0);
	                				}
	        					}
	        				}
	        				break;
	        				//CCW Turn
	        		case 4: if(rEnc < 100)
	        				{
	        					drive.tankDrive(0.0, 0.35);
	        				}
	        				else
	        				{
	        					drive.tankDrive(0.0, 0.0);
	        					RobotMap.flash.set( 6 / ControllerPower.getInputVoltage()); 
	                			
	        	        		if(RobotMap.pot.get() < 0.205)
	        					{
	        						RobotMap.dart.set(0.6);
	        					}
	        					else if(RobotMap.pot.get() > 0.215)
	        					{
	        						RobotMap.dart.set(-0.6);
	        					}
	        					else
	        					{
	        						RobotMap.dart.set(0.0);
	        						if(!Flag1)
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.rFlywheel.set(-1.0);
	                				RobotMap.lFlywheel.set(1.0);
	                				Timer.delay(2.2);
	                				RobotMap.rFlywheel.set(-1.0);
	            					RobotMap.lFlywheel.set(1.0);
	            					RobotMap.lBelt.set(-1.0);
	            					RobotMap.rBelt.set(1.0);
	            					Timer.delay(0.5);
	            					Flag1 = true;
	                				}
	                				else
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.flash.set(0.0);
	            					RobotMap.rFlywheel.set(0.0);
	            					RobotMap.lFlywheel.set(0.0);
	            					RobotMap.lBelt.set(0.0);
	            					RobotMap.rBelt.set(0.0);
	                				}
	        					}
	        				}
	        				break;
	        				//CCW Turn
	        		case 5: if(rEnc < 100)
	        				{
    							drive.tankDrive(0.0, 0.35);
	        				}
	        				else
	        				{
	        					drive.tankDrive(0.0, 0.0);
	        					RobotMap.flash.set( 6 / ControllerPower.getInputVoltage()); 
	                			
	        	        		if(RobotMap.pot.get() < 0.205)
	        					{
	        						RobotMap.dart.set(0.6);
	        					}
	        					else if(RobotMap.pot.get() > 0.215)
	        					{
	        						RobotMap.dart.set(-0.6);
	        					}
	        					else
	        					{
	        						RobotMap.dart.set(0.0);
	        						if(!Flag1)
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.rFlywheel.set(-1.0);
	                				RobotMap.lFlywheel.set(1.0);
	                				Timer.delay(2.2);
	                				RobotMap.rFlywheel.set(-1.0);
	            					RobotMap.lFlywheel.set(1.0);
	            					RobotMap.lBelt.set(-1.0);
	            					RobotMap.rBelt.set(1.0);
	            					Timer.delay(0.5);
	            					Flag1 = true;
	                				}
	                				else
	                				{
	                				drive.tankDrive(0.0, 0.0);
	                				RobotMap.flash.set(0.0);
	            					RobotMap.rFlywheel.set(0.0);
	            					RobotMap.lFlywheel.set(0.0);
	            					RobotMap.lBelt.set(0.0);
	            					RobotMap.rBelt.set(0.0);
	                				}
	        					}
	        				}
    						break;
    					
	        		default:
	        			drive.tankDrive(0.0, 0.0);
	        			break;
	        	}
	      
	        }
	        
        }
      
    //}
    
    public void teleopInit()
    {
	    if (autonomousCommand != null) autonomousCommand.cancel();
    }

    public void disabledInit()
    {

    }
    public void teleopPeriodic() 
    {
    	
        Scheduler.getInstance().run();
        
        double shoot = OI.OperatorStick.getRawAxis(3); //Shoot, overrides limit switches, Right Trigger
		double spinUp = OI.OperatorStick.getRawAxis(2); //Begin spinning flywheels, Left Trigger 
		double dartPosBatter = OI.OperatorStick.getPOV(); //Position DART for shooting under the ramp	
		double dartPosLowbar = OI.OperatorStick.getPOV();//Change DART to position for shooting at the ra
       
		
        //double CogX = table.getNumber("COG_X" , -1.0);
    	//double CogY = table.getNumber("COG_Y" , -1.0);)
        
        		
    	if(!RobotMap.leftLimit.get() || !RobotMap.rightLimit.get())
    	{
    		SmartDashboard.putBoolean("Intake" , true);
    	}
    	else
    	{
    		SmartDashboard.putBoolean("Intake", false);
    	}
    	
    	if(visionFinished == true)
    	{
    		SmartDashboard.putBoolean("Vision", true);
    	}
    	else
    	{
    		SmartDashboard.putBoolean("Vision", false);
    	}
      	//Button Mapping for Operator Stick
    	if(shoot > 0.95)
    	{
    		operatorButtonID = 1;
    	}
    		else if(OI.lowGoal.get())
    		{
    			operatorButtonID = 2;
    		}
    			else if(OI.intakeIn.get())
    			{
    				operatorButtonID = 3;
    			}
    				else if(spinUp > 0.9)
    				{
    					operatorButtonID = 4;
    				}
    					else if(OI.intakeActUp.get())
    					{
    						operatorButtonID = 5;
    					}
    						else if(dartPosBatter == 0)
    						{
    							operatorButtonID = 6;
    						}
    								else if(dartPosLowbar == 180)
    								{
    									operatorButtonID = 7;
    								}
    									else if(OI.intakeOut.get())
    									{
    										operatorButtonID = 8;
    									}
    										else if(OI.intakeActDown.get())
    										{
    											operatorButtonID = 9;
    										}
    											else
    											{
    												operatorButtonID = 0;
    											}
    		
    		//Button Mapping for driver controller
    	if(OI.manipulatorUp.get())
		{
			driverButtonID = 1;
		}
			else if(OI.manipulatorDown.get())
			{
				driverButtonID = 2;
			}
				else if(OI.dFlash.get() || OI.oFlash.get())
				{
					driverButtonID = 3;
				}
					else
					{
						driverButtonID = 0;
					}
				
    	//Switch to use buttons for operator
    	switch(operatorButtonID)
    	{
    		
    		case 0:	RobotMap.intake.set(0.0);
    				RobotMap.rBelt.set(0.0);
            		RobotMap.lBelt.set(0.0);
            		RobotMap.rFlywheel.set(0.0);
            		RobotMap.lFlywheel.set(0.0);
            		RobotMap.intakeAct.set(0.0);
            		OI.DriverStick.setRumble(RumbleType.kLeftRumble, (float)0.0);
            		OI.DriverStick.setRumble(RumbleType.kRightRumble, (float)0.0);
            		//timer.reset();
            		break;
    		
    		case 1:	RobotMap.rFlywheel.set(-1.0);
    				RobotMap.lFlywheel.set(1.0);
    				RobotMap.lBelt.set(-1.0);
    				RobotMap.rBelt.set(1.0);
    				break;
	    		
    		case 2: RobotMap.rFlywheel.set(0.3);
					RobotMap.lFlywheel.set(0.3);
					RobotMap.lBelt.set(-0.3);
					RobotMap.rBelt.set(0.3);
					break;
    				
    		case 3: if(!RobotMap.leftLimit.get() ||  !RobotMap.rightLimit.get())
					{	
						RobotMap.intake.set(0.0);
						RobotMap.rBelt.set(0.0);
						RobotMap.lBelt.set(0.0);
				
					}
					else
					{
						RobotMap.intake.set(-1.0);
						RobotMap.rBelt.set(0.30);
						RobotMap.lBelt.set(-0.30);
				
					}
    				break;
    		
    		case 4: RobotMap.rFlywheel.set(-1.0);
    				RobotMap.lFlywheel.set(1.0);
    				break;
    		
    		case 5: if(!RobotMap.actLimit.get())
    				 {
    					 RobotMap.intakeAct.set(0.0);
                     }
    				 else
    				 {
    					 RobotMap.intakeAct.set(-1.0);
    				 }
    				 break;
    		
    		case 6: if(!RobotMap.bottomHall.get() || RobotMap.bottomHall.get())
    				{
    					RobotMap.dart.set(0.0);
    				}
    				else
    				{
    					if(RobotMap.pot.get() < potAngleBatter - potDeadband)
	    				{
	    					RobotMap.dart.set(-0.25);
	    				}
	    				else if(RobotMap.pot.get() > potAngleBatter + potDeadband)
	    				{
	    					RobotMap.dart.set(0.25);
	    				}
	    				else
	    				{
	    					RobotMap.dart.set(0.0);
	    				}
    				}
    				break;
    		
    		case 7: if(!RobotMap.bottomHall.get() || RobotMap.bottomHall.get())
					{
						RobotMap.dart.set(0.0);
					}
					else
					{
						if(RobotMap.pot.get() < potAngleLowbar - potDeadband)
						{
							RobotMap.dart.set(-0.25);
						}
						else if(RobotMap.pot.get() > potAngleLowbar + potDeadband)
						{
							RobotMap.dart.set(0.25);
						}
						else
						{
							RobotMap.dart.set(0.0);
						}
					}
					break;
    		
    		case 8: RobotMap.intake.set(1.0);
    				RobotMap.rBelt.set(-0.5);
    				RobotMap.lBelt.set(0.5);
    				break;
    				
    		case 9: RobotMap.intakeAct.set(0.75);
    		break;
    		
    		default:
    		break;
    	}
    	
    	//switch to use buttons for driver
    	switch(driverButtonID)
    	{
    		case 0: RobotMap.man.set(0.0);
    				RobotMap.flash.set(0.0);
    		break;
    		
    		case 1: RobotMap.man.set(0.5);
    		break;
    		
    		case 2: RobotMap.man.set(-0.5);
    		break;														
    		
    		case 3: RobotMap.flash.set( 6 / ControllerPower.getInputVoltage());
    				break;
    		default:
    		break;
    		
    	}

       	double leftJoy; //Xvalue of OperatorStick 
        double rightJoy; //Yvalue of OperatorStick
        double dartValue; //Value of DART linear actuator
        
            
	        //DeadBands
	        if (OI.DriverStick.getY() < driverBands && OI.DriverStick.getY() > -driverBands) 
	        {
	        	leftJoy = 0;
	        }
	        else 
	        {
	
	        	leftJoy = OI.DriverStick.getY();
	        }
	        if (OI.DriverStick.getRawAxis(5) < driverBands && OI.DriverStick.getRawAxis(5) > -driverBands)
	        { 
	        	rightJoy = 0;
	        }
	        else 
	        {
	        	rightJoy = OI.DriverStick.getRawAxis(5);
	        }
        
        
        drive.tankDrive(-leftJoy , -rightJoy, true);
        
        
        //Manually change dart angle using operator stick
        //Deadband for operator stick
        if (OI.OperatorStick.getY() < operatorBands && OI.OperatorStick.getY() > -operatorBands)
        {
        	dartValue = 0;
        }
        else 
        {
        	dartValue = OI.OperatorStick.getY();
        }
        
        //Stop dart if it is trying to go above top hall effect sensor
        if(dartValue < 0.0 && !RobotMap.topHall.get())
        {
        	RobotMap.dart.set(0.0);
        }
        //Stop dart if it is trying to go below the bottom hall effect sensor
        else if(dartValue > 0.0 && !RobotMap.bottomHall.get())
        {
        	RobotMap.dart.set(0.0);
        }
        else
        {
        	RobotMap.dart.set(-dartValue);
        }
        /*
        if(OI.activateStalker.get())
        {
        	//System.out.println(Stalker());
        	Stalker();
        }
        */
    }
    
    /*
    public boolean StalkerAuto()
    {
    	double CogX = table.getNumber("COG_X" , -1.0);
    	double CogY = table.getNumber("COG_Y" , -1.0);
    	
    	//TODO Check that it is not -1, if so fail
    	int center = 467;
    	int buffer = 15;
    	
    	boolean dartDone = false;
    	boolean driveDone = false;
    	
    	double potValue;
    	potValue = (.00000008691 * (CogY *CogY) + 0.0002985 * CogY + 0.085); //1112
    	//System.out.println(RobotMap.pot.get());
    	
    	if(CogX < center - buffer)
    	{
    	
    		//drive.tankdrive.tankDrive(0.0, 0.4);	
    		RobotMap.rTalonBack.set(0.0);
    		RobotMap.rTalonFront.set(0.0);
    		RobotMap.lTalonBack.set(-0.125);
    		RobotMap.lTalonFront.set(-0.125);
    		
    	} 
    	else if(CogX > center + buffer) 
    	{
    		//drive.tankdrive.tankDrive(0.0, -0.4);
    		RobotMap.rTalonBack.set(0.0);
    		RobotMap.rTalonFront.set(0.0);
    		RobotMap.lTalonBack.set(0.175);
    		RobotMap.lTalonFront.set(0.175);
    		
    	}
    	else
    	{
    		//drive.tankdrive.tankDrive(0.0,0.0);
    		RobotMap.rTalonBack.set(0.0);
    		RobotMap.rTalonFront.set(0.0);
    		RobotMap.lTalonBack.set(0.0);
    		RobotMap.lTalonFront.set(0.0);
    		driveDone = true;
    	}
    	
    	if(RobotMap.pot.get() < potValue - .005  && RobotMap.topHall.get())
    	{
    		RobotMap.dart.set(0.375);
    	}
    	else if(RobotMap.pot.get() > potValue + .005 && RobotMap.bottomHall.get())	
    	{
    		RobotMap.dart.set(-0.375);
    	}
    	else
    	{
    		RobotMap.dart.set(0.0);
    		dartDone = true;
    	}
    	
    	if((dartDone == true && driveDone == true) && autoShotTaken == false)
    	{
    		
    		OI.DriverStick.setRumble(RumbleType.kLeftRumble, (float)1.0);
    		OI.DriverStick.setRumble(RumbleType.kRightRumble, (float)1.0);
    		
    		RobotMap.rFlywheel.set(-1.0);
    		RobotMap.lFlywheel.set(1.0);
    		Timer.delay(2.0);
    		
    		RobotMap.rBelt.set(1.0);
			RobotMap.lBelt.set(-1.0);
			Timer.delay(0.5);
			autoShotTaken = true;
		
    	}
    	else
    	{
    		RobotMap.rFlywheel.set(0.0);
    		RobotMap.lFlywheel.set(0.0);
    		RobotMap.rBelt.set(0.0);
    		RobotMap.lBelt.set(0.0);
    		
    	}
    	return false;
    }
    public boolean Stalker()
    {
    	double CogX = table.getNumber("COG_X" , -1.0);
    	double CogY = table.getNumber("COG_Y" , -1.0);
    	
    	//TODO Check that it is not -1, if so fail
    	
    	int center = 494;
    	int buffer = 15;
    	
    	boolean dartDone = false;
    	boolean driveDone = false;
    	
    	double potValue;
    	potValue = (.00000008691 * (CogY *CogY) + 0.0002985 * CogY + 0.10); //1112
    	//System.out.println(RobotMap.pot.get());
    	
    	if(CogX < center - buffer)
    	{
    	
    		//drive.tankdrive.tankDrive(0.0, 0.4);	
    		RobotMap.rTalonBack.set(0.0);
    		RobotMap.rTalonFront.set(0.0);
    		RobotMap.lTalonBack.set(-0.125);
    		RobotMap.lTalonFront.set(-0.125);
    		
    	} 
    	else if(CogX > center + buffer) 
    	{
    		//drive.tankdrive.tankDrive(0.0, -0.4);
    		RobotMap.rTalonBack.set(0.0);
    		RobotMap.rTalonFront.set(0.0);
    		RobotMap.lTalonBack.set(0.175);
    		RobotMap.lTalonFront.set(0.175);
    		
    	}
    	else
    	{
    		//drive.tankdrive.tankDrive(0.0,0.0);
    		RobotMap.rTalonBack.set(0.0);
    		RobotMap.rTalonFront.set(0.0);
    		RobotMap.lTalonBack.set(0.0);
    		RobotMap.lTalonFront.set(0.0);
    		driveDone = true;
    	}
    	
    	if(RobotMap.pot.get() < potValue - .005  && RobotMap.topHall.get())
    	{
    		RobotMap.dart.set(0.375);
    	}
    	else if(RobotMap.pot.get() > potValue + .005 && RobotMap.bottomHall.get())	
    	{
    		RobotMap.dart.set(-0.375);
    	}
    	else
    	{
    		RobotMap.dart.set(0.0);
    		
    		dartDone = true;
    	}
    	
    	if(dartDone == true && driveDone == true)
    	{
    		
    		OI.DriverStick.setRumble(RumbleType.kLeftRumble, (float)1.0);
    		OI.DriverStick.setRumble(RumbleType.kRightRumble, (float)1.0);
    		
			visionFinished = true;
  
    	}
    	else
    	{
    		OI.DriverStick.setRumble(RumbleType.kLeftRumble, (float)0);
    		OI.DriverStick.setRumble(RumbleType.kRightRumble, (float)0);
    		
			visionFinished = false;
    	}
    	return false;
    }
    */
   /* 
    public void tankdrive.tankDrive(double leftValue, double rightValue, boolean squaredInputs) {
        // local variables to hold the computed PWM values for the motors
        

        double leftMotorSpeed;
        double rightMotorSpeed;
        
        leftValue = limit(leftValue);
        rightValue = limit(rightValue);

        if (squaredInputs) {
          // square the inputs (while preserving the sign) to increase fine control
          // while permitting full power
          if (leftValue >= 0.0) {
            leftValue = (leftValue * leftValue);
          } else {
            leftValue = -(leftValue * leftValue);
          }
          if (rightValue >= 0.0) {
            rightValue = (rightValue * rightValue);
          } else {
            rightValue = -(rightValue * rightValue);
          }
        }

        if (leftValue > 0.0) {
          if (rightValue > 0.0) {
            leftMotorSpeed = leftValue - rightValue;
            rightMotorSpeed = Math.max(leftValue, rightValue);
          } else {
            leftMotorSpeed = Math.max(leftValue, -rightValue);
            rightMotorSpeed = leftValue + rightValue;
          }
        } else {
          if (rightValue > 0.0) {
            leftMotorSpeed = -Math.max(-leftValue, rightValue);
            rightMotorSpeed = leftValue + rightValue;
          } else {
            leftMotorSpeed = leftValue - rightValue;
            rightMotorSpeed = -Math.max(-leftValue, -rightValue);
          }
        }

        setLeftRightMotorOutputs(leftMotorSpeed, rightMotorSpeed);
      }
    /**
     * Set the speed of the right and left motors. This is used once an
     * appropriate drive setup function is called such as twoWheeldrive.tankDrive(). The
     * motors are set to "leftSpeed" and "rightSpeed" and includes flipping the
     * direction of one side for opposing motors.
     *$
     * @param leftOutput The speed to send to the left side of the robot.
     * @param rightOutput The speed to send to the right side of the robot.
     
    public void setLeftRightMotorOutputs(double leftOutput, double rightOutput) {
      if (RobotMap.lTalonFront != null) {
    	  RobotMap.lTalonFront.set(limit(leftOutput) * 0.9);
      }
      RobotMap.lTalonBack.set(limit(leftOutput) * 0.9);

      if (RobotMap.rTalonFront != null) {
        RobotMap.rTalonFront.set(-limit(rightOutput) * 1.0);
      }
      RobotMap.rTalonBack.set(-limit(rightOutput) * 1.0);

    }
    
    protected static double limit(double num) {
        if (num > 1.0) {
          return 1.0;
        }
        if (num < -1.0) {
          return -1.0;
        }
        return num;
      }
    */
    //Leaves Dart & defense arm up
    public void defenseAutoStart()
    {
    	/*
    	System.out.println(RobotMap.leftLimit.get());
    	
    	if(!RobotMap.leftLimit.get() || !RobotMap.rightLimit.get())
    	{
    		 RobotMap.rTalonBack.set(0.0);
             RobotMap.rTalonFront.set(0.0);
             RobotMap.lTalonBack.set(0.0);
             RobotMap.lTalonFront.set(0.0);
             RobotMap.man.set(0.0);
             RobotMap.intakeAct.set(0.0);
             RobotMap.rBelt.set(0.0);
             RobotMap.lBelt.set(0.0);
             RobotMap.intakeAct.set(0.0);
    	}
    	else
    	{
    	*/
            RobotMap.rTalonBack.set(0.0);
            RobotMap.rTalonFront.set(0.0);
            RobotMap.lTalonBack.set(0.0);
            RobotMap.lTalonFront.set(0.0);
            //RobotMap.man.set(0.5);
            //timer.delay(0.25);
            RobotMap.intakeAct.set(0.75);
            Timer.delay(0.50);
            RobotMap.intakeAct.set(0.0);
            RobotMap.man.set(0.0);
            /* while(RobotMap.bottomHall.get())
            {
            	RobotMap.dart.set(-0.3);
            }
            */
            RobotMap.rBelt.set(-0.25);
            RobotMap.lBelt.set(0.25);
            Timer.delay(0.5030);
            RobotMap.rBelt.set(0.0);
            RobotMap.lBelt.set(0.0);
            RobotMap.dart.set(0.0);
            RobotMap.rTalonBack.set(0.0);
            RobotMap.rTalonFront.set(0.0);
            RobotMap.lTalonBack.set(0.0);
            RobotMap.lTalonFront.set(0.0);
    	//}  
    }
            //Completely unfolds defense arm and intake
    		public void lowbarAutoStart()
            {
            	
            	System.out.println(RobotMap.leftLimit.get());
            	
            	if(!RobotMap.leftLimit.get() || !RobotMap.rightLimit.get())
            	{
            		 RobotMap.rTalonBack.set(0.0);
                     RobotMap.rTalonFront.set(0.0);
                     RobotMap.lTalonBack.set(0.0);
                     RobotMap.lTalonFront.set(0.0);
                     RobotMap.man.set(0.0);
                     RobotMap.intakeAct.set(0.0);
                     RobotMap.rBelt.set(0.0);
                     RobotMap.lBelt.set(0.0);
                     RobotMap.intakeAct.set(0.0);
            	}
            	else
            	{
            
                    RobotMap.rTalonBack.set(0.0);
                    RobotMap.rTalonFront.set(0.0);
                    RobotMap.lTalonBack.set(0.0);
                    RobotMap.lTalonFront.set(0.0);
                    RobotMap.man.set(0.5);
                    Timer.delay(0.25);
                    RobotMap.intakeAct.set(0.75);
                    Timer.delay(0.50);
                    RobotMap.intakeAct.set(0.0);
                    RobotMap.man.set(0.0);
                    while(RobotMap.bottomHall.get())
                    {
                    	RobotMap.dart.set(-0.25);
                    }
                    
                    RobotMap.rBelt.set(-0.25);
                    RobotMap.lBelt.set(0.25);
                    Timer.delay(0.5030);
                    RobotMap.rBelt.set(0.0);
                    RobotMap.lBelt.set(0.0);
                    RobotMap.dart.set(0.0);
                    RobotMap.rTalonBack.set(0.0);
                    RobotMap.rTalonFront.set(0.0);
                    RobotMap.lTalonBack.set(0.0);
                    RobotMap.lTalonFront.set(0.0);
            	}  
            	
    }
public void testPeriodic()
    {
        LiveWindow.run();
    }
}



