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
public class NeuralNet {

	protected ArrayList<Neuron> inputNeurons, hiddenNeurons, outputNeurons;
	
	public NeuralNet() {
		inputNeurons = new ArrayList<Neuron>();
		hiddenNeurons = new ArrayList<Neuron>();
		outputNeurons = new ArrayList<Neuron>();
	}
	
	public void Run() {
		ArrayList<Double> inputs = new ArrayList<Double>();
		for(Iterator<Neuron> i = inputNeurons.iterator(); i.hasNext();) {
			i.next().Run(inputs);			
		}
	}
}
