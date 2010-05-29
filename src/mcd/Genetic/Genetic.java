/**
 * 
 */
package mcd.Genetic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;
import robocode.RobotStatus;

/**
 * @author Mike
 * 
 */
public class Genetic {

	private static final int POPULATION_SIZE = 8;
	private static final int MIN_ACTIONS_PER_NODE = 8;
	//private static final int MAX_ACTIONS_PER_NODE = 10;
	private static final double MAX_DISTANCE = 500;
	private static final double MAX_ANGLE = Math.toRadians(360);
	private static final String DATA_FILE_EXT = ".txt";
	private static final String DELIMITER = " ";

	private ArrayList<Node> lastGeneration;
	private ArrayList<Node> currentGeneration;
	private Node currentNode = null;
	private LinkedList<RobotAction> currentActionSequence;
	
	private int bulletHitCount = 0;
	private long nodeBeginTime;

	// DEBUG PROPERTIES
	private Integer epochCount = 0;
	private Integer nodeIndex = 0;
	private Integer actionIndex = 0;

	private AdvancedRobot robot;
	private Random rand;
	private Boolean initialized = false;

	/**
	 * Constructor
	 */
	public Genetic(Random rand){
		this.rand = rand;
	}
	
	public void SetRobot(AdvancedRobot robot) {
		this.robot = robot;
	}

	private void initPopulation() {
		currentGeneration = new ArrayList<Node>();
		if (!loadPopulation()) {
			for (int i = 0; i < POPULATION_SIZE; ++i) {
				currentGeneration.add(getRandomizedNode());
			}
			epochCount = 1;
		}
		nextNode(robot.getTime());
		initialized = true;
	}

	private boolean loadPopulation() {
		File f = robot.getDataFile(robot.getName() + DATA_FILE_EXT);

		if (!f.exists() || !f.canRead())
			return false;
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));

			String line = r.readLine();
			if(line == null)
				return false;
			String[] values = line.split(DELIMITER);
			epochCount = Integer.parseInt(values[1]);
			
			line = r.readLine();
			if(line == null)
				return false;
			values = line.split(DELIMITER);
			nodeIndex = Integer.parseInt(values[1]);
			
			line = r.readLine();
			if(line == null)
				return false;
			while (line != null) {
				values = line.split(DELIMITER);
				Node g = new Node();
				g.setFitness(Double.parseDouble(values[0]));
				for (int i = 2; i < values.length; i += 2) {
					g.addAction(new RobotAction(Double
							.parseDouble(values[i - 1]), Double
							.parseDouble(values[i])));
				}				
				currentGeneration.add(g);
				line = r.readLine();
			}
			if(nodeIndex >= currentGeneration.size())
				nodeIndex = 0;
			
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private void savePopulation() {
		if(lastGeneration == null || lastGeneration.size() == 0) {
			robot.out.println("Attempted to save data, but no nodes exist!");
			return;
		}
		
		File f = robot.getDataFile(robot.getName() + DATA_FILE_EXT);

		if (f.exists())
			f.delete();

		PrintStream s = null;

		try {
			s = new PrintStream(new RobocodeFileOutputStream(f));
			s.println("EpochCount: " + epochCount);
			s.println("NodeCount: " + nodeIndex);
			for (Iterator<Node> nodeIter = lastGeneration.iterator(); nodeIter.hasNext();) {
				Node node = nodeIter.next();
				LinkedList<RobotAction> actionSequence = node.getActionSequence();
				StringBuilder sb = new StringBuilder();
				sb.append(node.getFitness()+ DELIMITER);
				for (Iterator<RobotAction> aIter = actionSequence.iterator(); aIter
						.hasNext();) {
					RobotAction a = aIter.next();
					sb.append(a.getDistance() + DELIMITER + a.getAngle()
							+ DELIMITER);
				}
				s.println(sb.toString());
			}
			if (s.checkError()) {
				throw new IOException("PrintStream error while saving genetic data!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	private Node getRandomizedNode() {
		Node g = new Node();

		//int numActions = rand.nextInt(MAX_ACTIONS_PER_NODE + 1)
		//		+ MIN_ACTIONS_PER_NODE;

		for (int i = 0; i < MIN_ACTIONS_PER_NODE; ++i) {
			double d = (rand.nextDouble() - rand.nextDouble()) * MAX_DISTANCE;
			double a = (rand.nextDouble() - rand.nextDouble()) * MAX_ANGLE;
			RobotAction action = new RobotAction(d, a);
			g.addAction(action);
		}

		return g;
	}
	
	public void Stop() {
		savePopulation();		
	}

	private void nextEpoch(long time) {		
		// First, back up the current completed generation to save later
		lastGeneration = cloneGeneration(currentGeneration);
		
		// 1 - selection
		ArrayList<Node> nextGeneration = doSelection();
		
		// 2 - crossover & mutation
		// ArrayList<Node> nextGeneration = new ArrayList<Node>();

		// for (int i = 1; i < nodes.size(); ++i)
		// nextGeneration.add(crossover(nodes.subList(i - 1, i)));

		// nodes.clear();
		// nodes.addAll(nextGeneration);
		doCrossover(2);
		
		// start running first node
		++epochCount;
		nodeIndex = 0;
		nextNode(time);
	}

	private ArrayList<Node> cloneGeneration(ArrayList<Node> generation) {
		ArrayList<Node> clonedGeneration = new ArrayList<Node>();
		Iterator<Node> i = generation.iterator();
		while(i.hasNext()) {
			try {
				clonedGeneration.add((Node)i.next().clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return clonedGeneration;
	}
	
	/*
	 * Performs fitness proportionate selection
	 * 1 - Normalize fitness values 		
	 * 2 - sort by descending fitness values.
	 * 3 - Compute accumulated normalized fitness values.
	 * 4 - Get cutoff value.
	 * 5 - Select individuals whose accumulated normalized value 
	 *		is greater than the cutoff value.
	 */
	private ArrayList<Node> doSelection() {
		normalizeFitnessValues();
		Collections.sort(currentGeneration);
		computeAccumulatedFitnessValues();
		//TODO remove hardcoded cutoff
		double cutoff = 0.5;		
		ArrayList<Node> selectedNodes = new ArrayList<Node>();
		Iterator<Node> iter = currentGeneration.iterator();
		while(iter.hasNext()) {
			Node n = iter.next();
			if(n.getFitness() > cutoff)
				selectedNodes.add(n);
		}
		return selectedNodes;
	}
	
	private void computeAccumulatedFitnessValues() {
		double sum = 0;
		Iterator<Node> iter = currentGeneration.iterator();
		while(iter.hasNext()) {
			Node n = iter.next();
			sum += n.getFitness();
			n.setFitness(sum);
		}
	}
	
	private void normalizeFitnessValues() {
		double sum = 0;
		
		Iterator<Node> iter = currentGeneration.iterator();
		while(iter.hasNext())
			sum += iter.next().getFitness();
		
		iter = currentGeneration.iterator();
		while(iter.hasNext()) {
			Node n = iter.next();
			double f = n.getFitness() / sum;
			n.setFitness(f);
		}
	}

	/*
	 * Performs a crossover of Nodes using "Cut and splice" technique
	 */
	private void doCrossover(int numNodes) {
		if(numNodes <= 1)
			return;

		// TODO account for remainder of parents		
		// TODO clone actions instead of simply swapping sequences,
		// TODO create enough offspring to maintain population 	
		
		for(int i = 0; i < currentGeneration.size(); i+=numNodes) { 
			 List<Node> parents = currentGeneration.subList(i, i + numNodes);
			 LinkedList<RobotAction> currentSubSequence = null;

			 // each cut is spliced onto the next Node in list order
			 
			 for(int k = 0; k < numNodes; ++k) {
				 LinkedList<RobotAction> actionSequence = parents.get(k).getActionSequence();
				 	 
				 // cut the subsequence from the current Node
				 LinkedList<RobotAction> nextSubSequence = new LinkedList<RobotAction>();
				 for(int a = actionSequence.size() / 2; a > 0; --a)
					 nextSubSequence.addFirst(actionSequence.removeLast());
				 
				 if(currentSubSequence != null)
					 actionSequence.addAll(currentSubSequence);
				 
				 // TODO if the new sequence length is less than minimum, add new actions				 
				 
				 currentSubSequence = nextSubSequence;
			 }
			 
			 // splice the final Node cut onto first Node			 
			 parents.get(0).getActionSequence().addAll(currentSubSequence);			 
		 }
		 
	}

	/*
	 * private Node crossover(List<Node> parents) { Node offspring = new
	 * Node(); int maxActions = 0; ArrayList<ArrayList<RobotAction>>
	 * actionLists = new ArrayList<ArrayList<RobotAction>>();
	 * 
	 * for (Iterator<Node> i = parents.iterator(); i.hasNext();) {
	 * ArrayList<RobotAction> actionSequence = i.next() .getActionSequence();
	 * int numActions = actionSequence.size(); if (numActions > maxActions)
	 * maxActions = numActions; actionLists.add(actionSequence); }
	 * 
	 * for (int a = 0; a < maxActions; ++a) { double sumDistance = 0; double
	 * sumAngle = 0; int n = 0; for (Iterator<ArrayList<RobotAction>> i =
	 * actionLists.iterator(); i .hasNext();) { ArrayList<RobotAction>
	 * actionSequence = i.next(); if (actionSequence.size() > a) { RobotAction
	 * action = actionSequence.get(a); sumDistance += action.getDistance();
	 * sumAngle += action.getAngle(); ++n; } } double distance = sumDistance /
	 * n; double angle = sumAngle / n; RobotAction action = new
	 * RobotAction(robot, distance, angle); offspring.addAction(action); }
	 * 
	 * return offspring; }
	 */

	private void nextNode(long time) {
		if (nodeIndex < currentGeneration.size()) {
			currentNode = currentGeneration.get(nodeIndex++);
			currentActionSequence = currentNode.getActionSequence();
			ResetNode();
			nodeBeginTime = time;
			nextAction(time);
		} else {
			nextEpoch(time);
		}
	}
	
	public void ResetNode() {
		actionIndex = 0;
		bulletHitCount = 0;
		nodeBeginTime = 0;
	}

	private void nextAction(long time) {
		if (actionIndex < currentActionSequence.size()) {
			currentActionSequence.get(actionIndex++).Run(robot);
		} else {
			// Evaluate fitness (in bullet hits per time)
			double f = (double)bulletHitCount / (time - nodeBeginTime);
			robot.out.println("fitness = " + f);
			robot.out.println("bulletHitCount = " + bulletHitCount);
			robot.out.println("nodeBeginTime = " + nodeBeginTime);
			robot.out.println("time = " + time);
			currentNode.setFitness(f);			
			nextNode(time);
		}
	}

	public void UpdateStatus(RobotStatus s) {
		if (!initialized)
			initPopulation();
		
		long t = s.getTime();
		
		robot.setDebugProperty("EPOCH", epochCount.toString());
		robot.setDebugProperty("NODE", nodeIndex.toString());
		robot.setDebugProperty("ACTION", actionIndex.toString());
		if (s.getDistanceRemaining() == 0
				&& s.getTurnRemainingRadians() == 0)
			nextAction(t);
	}

	public void HitByBullet() {
		++bulletHitCount;
	}

}
