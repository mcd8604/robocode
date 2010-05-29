package mcd.Genetic;

import robocode.AdvancedRobot;

public class RobotAction implements Cloneable {
		
	private double distance;
	private double angle;
	
	public RobotAction(double distance, double angle) {
		this.distance = distance;
		this.angle = angle;
	}

	public void Run(AdvancedRobot robot) {
		robot.setAhead(distance);
		robot.setTurnRightRadians(angle);
		robot.execute();
	}

	public double getDistance() {
		return distance;
	}

	public double getAngle() {
		return angle;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
