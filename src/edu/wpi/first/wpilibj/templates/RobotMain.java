package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.smartdashboard.*;

public class RobotMain extends SimpleRobot {

    Compressor compressor = new Compressor(1, 1);
    RobotDrive chassis;
    Joystick leftStick = new Joystick(1);
    Joystick rightStick = new Joystick(2);
    AxisCamera camera;
    Servo servoTest;
    DriverStation driverStation;

    Talon fl;
    Talon bl;
    Talon fr;
    Talon br;

    private VisionProcessing visionProcessing;

    public RobotMain() {

    }

    public void robotInit() {
        camera = AxisCamera.getInstance("10.14.92.11");

        visionProcessing = new VisionProcessing();
        visionProcessing.init(camera);

        driverStation = DriverStation.getInstance();

        servoTest = new Servo(5);

        fl = new Talon(1);
        bl = new Talon(2);
        br = new Talon(3);
        fr = new Talon(4);
        compressor.start();
        chassis = new RobotDrive(fl, bl, fr, br);
        chassis.setExpiration(2);// since this is a double it's probrably seconds not miliseconds
        chassis.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        chassis.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
    }

    public void autonomous() {
        visionProcessing.autonomousInit();
        BinaryImage filteredImage;

        while (this.isAutonomous() && this.isEnabled()) {
            driveNowhere();

            try {
                filteredImage = visionProcessing.filterImage(camera.getImage());
                visionProcessing.autonomousUpdate(filteredImage);
            } catch (Exception e) {
                
            }

            SmartDashboard.putBoolean("Target Hot", visionProcessing.target.Hot);
            Timer.delay(.01);
        }
    }

    public void operatorControl() {
        chassis.setSafetyEnabled(true);
        SmartDashboard.putString("Alliance", driverStation.getAlliance().name);
        while (this.isOperatorControl() && this.isEnabled()) {
            SmartDashboard.putNumber("Mecanum X", getMecX());
            SmartDashboard.putNumber("Mecanum Y", getMecY());
            SmartDashboard.putNumber("Mecanum Rotation", getMecRot());
            SmartDashboard.putNumber("Front Left", fl.getSpeed());
            SmartDashboard.putNumber("Front Right", fr.getSpeed());
            SmartDashboard.putNumber("Back Left", bl.getSpeed());
            SmartDashboard.putNumber("Back Right", br.getSpeed());

            mecanumDrive(getMecX(), getMecY(), getMecRot());
            //chassis.mecanumDrive_Cartesian(getMecX(), getMecY(), getMecRot(), 0);

            if (rightStick.getRawButton(3)) { //up
                servoTest.setAngle(-360);
            } else if (rightStick.getRawButton(2)) { //down
                servoTest.setAngle(360);
            }
            Timer.delay(.01);
        }

    }

    public void disabled() {

    }

    public void test() {
        //
        // Shoulden't we use Autonomous?
        //
        /*
        visionProcessing.autonomousInit();
        BinaryImage filteredImage;

        try {
            filteredImage = visionProcessing.filterImageTest(camera.getImage());  // Also this should have been IN the loop. I think...
            visionProcessing.autonomousUpdate(filteredImage); // Along whith this
        } catch (AxisCameraException ex) {
        } catch (NIVisionException ex) {
        }
        SmartDashboard.putBoolean("Target Hot", visionProcessing.target.Hot); // And this

        while (this.isTest() && this.isEnabled()) {
            driveNowhere();
            Timer.delay(0.1);
        }
        */
    }

    private double getMecX() {
        return deadZone(rightStick.getAxis(Joystick.AxisType.kX));
    }

    private double getMecY() {
        return deadZone(rightStick.getAxis(Joystick.AxisType.kY));
    }

    private double getMecRot() {
        return deadZone(leftStick.getAxis(Joystick.AxisType.kX));
    }

    private double deadZone(double value) {
        return (abs(value) < .1) ? 0 : value;
    }

    private double abs(double value) {
        return value < 0 ? -value : value;
    }

    private void mecanumDrive(double x, double y, double r) {
        y = -y;

        double frn = 0;
        double fln = 0;
        double brn = 0;
        double bln = 0;

        frn *= 1;
        fln *= 1;
        brn *= 1;
        bln *= 1;

        fln = y + x + r;
        frn = y - x - r;
        bln = y - x + r;
        brn = y + x - r;

        fr.set(-maxAt1(frn));
        fl.set(maxAt1(fln));
        br.set(-maxAt1(brn));
        bl.set(maxAt1(bln));
    }

    private double maxAt1(double n) {
        return n < -1 ? -1 : (n > 1 ? 1 : n);
    }

    private void driveNowhere() {
        chassis.tankDrive(0, 0);
    }

}
