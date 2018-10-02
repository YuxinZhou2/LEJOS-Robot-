/*
 * Navigation.java
 */
package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.motor.EV3LargeRegulatedMotor;


/**
 * This class is used to drive the robot on the demo floor.
 */
public class Navigation {
  private static final int FORWARD_SPEED = 200;
  private static final int ROTATE_SPEED = 150;
  private static final double TILE_SIZE = 30.48;
  
  public static EV3LargeRegulatedMotor leftMotor;
  public static EV3LargeRegulatedMotor rightMotor;
  public static Odometer odo;
  public static double leftRadius;
  public static double rightRadius;
  public static double track;
  public static double current_T=0;
  
  
  public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      double leftRadius, double rightRadius, double track) {
	  Navigation.leftMotor=leftMotor;
	  Navigation.rightMotor=rightMotor;
	  Navigation.leftRadius= leftRadius;
	  Navigation.rightRadius= rightRadius;
	  Navigation.track= track;
  }
  
  /** This method is meant to drive the robot to the predefined waypoints.
   * 
   * @param waypoints
   */
  public void driveTo(double x, double y) {
	 double minimalT=0,distance=0;
	 double currentX=0.0;
	 double currentY=0.0;
	 double odometer[] = { 0, 0, 0 };
	 
	 navigating = true;
	 // Get odometer readings
     try {
         odometer = Odometer.getOdometer().getXYT();
     } catch (OdometerExceptions e) {
         // Do nothing lol
         e.printStackTrace();
     }
     
     // Set odometer reading angle as prev angle as well
     current_T = odometer[2];

     // Get displacement to travel on X and Y axis
     currentX= odometer[0];
	currentY = odometer[1];
	

	//Getting the distances with respect to the tile size
	double deltaX = x*TILE_SIZE-currentX;
	double deltaY =	y*TILE_SIZE-currentY;
	distance = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));
	
	//Calculating the minimal angle to get to destination
		minimalT = Math.toDegrees(Math.atan2(deltaX,deltaY));
		
	
	
	//If the angle is negative, we want its positive equivalent
	if(minimalT < 0) {
		minimalT = 360 - Math.abs(minimalT);
	}
	
	//Calling the rotate
	rotate(minimalT);
	
	leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);

    leftMotor.rotate(convertDistance(leftRadius, distance), true);
    rightMotor.rotate(convertDistance(rightRadius, distance), false);

	  
  }

  /**
   * This method is meant to drive the robot in a square of size 2x2 Tiles. It is to run in parallel
   * with the odometer and Odometer correcton classes allow testing their functionality.
   * 
   * @param leftMotor
   * @param rightMotor
   * @param leftRadius
   * @param rightRadius
   * @param width
   */
  public void rotate(double minimalT) {
	 //Calculating by how much we have to rotate with respect to our current angle
	 double deltaT = 0;
	 deltaT= minimalT - current_T;
	 boolean turnLeft = false;
	 
	 //Getting the positive equivalent
	 if(deltaT < 0) {
		deltaT = 360 - Math.abs(deltaT); 
	 }
	 
	 if(deltaT > 180) {
		 //Turn left
		 turnLeft = true;
		 //Rotate by
		 deltaT = 360 - Math.abs(deltaT); 
	 }
	 else {
		 turnLeft= false;
	 }
	 
	 leftMotor.setSpeed(ROTATE_SPEED);
	 rightMotor.setSpeed(ROTATE_SPEED);
	   
	 if(turnLeft) {
		 leftMotor.rotate(-convertAngle(leftRadius, track, deltaT), true);
		 rightMotor.rotate(convertAngle(rightRadius, track, deltaT), false);
	 }
	 else {
		 leftMotor.rotate(convertAngle(leftRadius, track, deltaT), true);
		 rightMotor.rotate(-convertAngle(rightRadius, track, deltaT), false);
	 }

  }
  static boolean navigating = false;
  
  public static boolean isNavigating() {
      return navigating;
  }

  /**
   * This method allows the conversion of a distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius
   * @param distance
   * @return
   */
  private static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }
}
