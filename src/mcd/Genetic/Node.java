package mcd.Genetic;

import java.util.Iterator;
import java.util.LinkedList;


public class Node implements Comparable, Cloneable {
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

	@Override
	public int compareTo(Object o) {
		Node other = (Node)o;
		return Double.compare(fitness, other.fitness);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Node n = (Node)super.clone();
		n.actionSequence = new LinkedList<RobotAction>();
		Iterator<RobotAction> i = this.actionSequence.iterator();
		while(i.hasNext())
			n.actionSequence.add((RobotAction)i.next().clone());
		return n;
	}
}
