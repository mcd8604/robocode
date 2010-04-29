package mcd.Genetic;

import robocode.AdvancedRobot;

public class RobotAction {
		
	private double distance;
	private double angle;
	
	private AdvancedRobot robot;
	
	public RobotAction(AdvancedRobot robot, double distance, double angle) {
		this.robot = robot;
		this.distance = distance;
		this.angle = angle;
	}

	public void Run() {
		robot.setAhead(distance);
		robot.setTurnRightRadians(angle);
	}

	public double getDistance() {
		return distance;
	}

	public double getAngle() {
		return angle;
	}
	
}
