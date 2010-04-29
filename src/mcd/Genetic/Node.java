package mcd.Genetic;

import java.util.LinkedList;


public class Node {
	private double fitness;
	private LinkedList<RobotAction> actionSequence;

	public Node() {
		fitness = 0;
		actionSequence = new LinkedList<RobotAction>();
	}

	public void addAction(RobotAction action) {
		actionSequence.add(action);
	}
	
	public LinkedList<RobotAction> getActionSequence() {
		return actionSequence;
	}
	
	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
}
