/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;



import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private double lightSensorDis = 1.4;
	private double squareSideLen = 30.48;
	//
	public float[] data;
	public static float lastclr;
	private SampleProvider clr;
	//
	double initialY = 0;
	double initialX = 0;
	int countx = 0;
	int county = 0;
	int count = 0;
	//
	private static float color = 0;
	private static final Port portColor = LocalEV3.get().getPort("S1");
	private static SensorModes myColor = new EV3ColorSensor(portColor);
	//static SampleProvider myColorSample = lightSensor.getRedMode();
	//float [] sample = new float[myColorSample.sampleSize()];

	/**
	 * This is the default class constructor. An existing instance of the odometer is used. This is to
	 * ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection() throws OdometerExceptions {

		this.odometer = Odometer.getOdometer();


	}

	/**
	 * Here is where the odometer correction code should be run.
	 * 
	 * @throws OdometerExceptions
	 */
	// run method (required for Thread)
	//below is our method for odometry correction implemented by Group37
	//the main task is to use the side length of the square in the lab which is 30.48cm as a reference to measure where the car is (in a corrdinate form)
	//need to pay attention to the fact that every time the color sensor detect a black line does not mean that the center of the rotation of the wheel 
	//is on the black line
	//need to minus the offset of the light sensor to the x-axis and y-axis.
	//we first got the offset between the light sensor and the center of rotation then it is easy to calculate the offset between light sensor and x-axis (lightSensorDis*cos(theta))
	//and y-axis (lightSensorDis*sin(theta))
	public void run() {
	
		long correctionStart, correctionEnd;
		SampleProvider myColorSample = myColor.getMode("Red");
		float[] sampleColor = new float[myColor.sampleSize()];
		myColorSample.fetchSample(sampleColor, 0);
		color = sampleColor[0]*1000;
		while (true) {
			correctionStart = System.currentTimeMillis();
			//
			myColorSample.fetchSample(sampleColor, 0);
			color = sampleColor[0]*1000;
			//
			if ( color < 450 ) { //we find a black line
				count++; //count is to count how many black lines we have pass
				double theta = odometer.getXYT()[2]; // we need to get our theta to find offset to X axis and offset to Y axis
				theta = theta * Math.PI /180;//transfer our theta to the radius form
				Sound.beep();
				
				//if the theta is bigger than pi/4 and smaller than 3*pi/4 this means that we are on a track of increasing x
				//countx is to count how many black lines we have passed for increasing the value of x
				//for the sensorXoffset, i have mentioned before.
				if (theta >= Math.PI/4 && theta < 3*Math.PI/4) {
				
					double sensorXOffset = Math.cos(theta) * lightSensorDis;
					countx++;
					odometer.setX(30.48*countx - sensorXOffset);
				}
				//we are at the direction of increasing y
				else if(theta >=0 && theta < Math.PI/4)
				{
					
					double sensorYOffset = Math.sin(theta) * lightSensorDis;
					county++;
					odometer.setY(30.48*countx - sensorYOffset);
				}
				//we are at the direction of decreasing x
				else if(theta >= 5*Math.PI/4 && theta < 7*Math.PI/4)
				{
					
					double sensorXOffset = Math.cos(theta) * lightSensorDis;
					countx--;
					odometer.setX(30.48*countx - sensorXOffset);
				}
				//we are at the direction of decreasing y
				else if(theta > Math.PI && theta<5*Math.PI/4)
				{
					double sensorYOffset = Math.sin(theta) * lightSensorDis;
					county--;
					odometer.setY(30.48*county - sensorYOffset);
				}
				//double check by using the count if the count is equal to 9 this means that we are at the last cornor of turning right refresh Y
				//b/c ideally Y would not change
				else if(count == 9)
				{
					double sensorYOffset = Math.sin(theta) * lightSensorDis;
					odometer.setY(0-sensorYOffset);
				}
				//double check by using the count if the count == 12 means that we are going to be back soon.
				//refresh X
				//b/c ideally X would not change
				else if(count == 12)
				{
					double sensorXOffset = Math.cos(theta) * lightSensorDis;
					odometer.setX(0 - sensorXOffset);
				}
				

			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here
				}
			}
		}
	}
}
