/**
 * 
 */
package mcd.NeuralNet;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Mike
 *
 */
public class Neuron {

	ArrayList<Double> weights;
	ArrayList<Neuron> outputs;
	
	public Neuron()
	{
		weights = new ArrayList<Double>();
		outputs = new ArrayList<Neuron>();
	}
	
	public void Run(ArrayList<Double> inputs) {
		double sum = 0;
		for(int i = 0; i < weights.size(); ++i) {
			sum += weights.get(i) * inputs.get(i);
		}
		/*
		 * foreach output, Run()
		 */
	}

}
