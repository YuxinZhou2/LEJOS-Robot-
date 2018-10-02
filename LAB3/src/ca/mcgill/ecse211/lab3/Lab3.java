// Lab3.java
package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

public class Lab3 {

  // Motor Objects, and Robot related parameters
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  private static final TextLCD lcd = LocalEV3.get().getTextLCD();
  private EV3ColorSensor lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
  public static final double WHEEL_RAD = 2.2;
  public static final double TRACK = 11.5;

  public static void main(String[] args) throws OdometerExceptions {

    int buttonChoice;

    // Odometer related objects
    Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); // TODO Complete implementation
    OdometryCorrection odometryCorrection = new OdometryCorrection(); // TODO Complete
                                                                      // implementation
    Display odometryDisplay = new Display(lcd); // No need to change

      // Start odometer and display threads
      Thread odoThread = new Thread(odometer);
      odoThread.start();
      Thread odoDisplayThread = new Thread(odometryDisplay);
      odoDisplayThread.start();

      // spawn a new Thread to avoid Navigation.drive() from blocking
      (new Thread() {
        public void run() {
        	//Defining trajectories
        		int test_map[][]= new int[][]{{2,1},{1,1},{1,2},{2,0}}; //map for lab report
        		int map1[][]= new int[][] {{0,2},{1,1},{2,2},{2,1},{1,0}};
        		int map2[][] = new int[][] {{1,1},{0,2},{2,2},{2,1},{1,0}};
        		int map3[][] = new int[][] {{1,0},{2,1},{2,2},{0,2},{1,1}};
        		int map4[][] = new int[][] {{0,1},{1,2},{1,0},{2,1},{2,2}};
        		Navigation navig = new Navigation(leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
        		
        	//Looping through all the waypoints of a map
        		for(int i=0;i < 5;i++) {
        			navig.travelTo(test_map[i][0], test_map[i][1]);
        		}
        		
          
          
        }
      }).start();
 

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
