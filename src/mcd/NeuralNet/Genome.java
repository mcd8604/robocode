package mcd.NeuralNet;

import java.util.ArrayList;

public class Genome {

	ArrayList<Double> vecWeights;
	double dFitness;

	Genome() {
		vecWeights = new ArrayList<Double>();
		dFitness = 0;
	}

	Genome(ArrayList<Double> w, double f) {
		vecWeights = w;
		dFitness = f;
	}

	// overload '<' used for sorting
	/*
	 * friend bool operator<(const SGenome& lhs, const SGenome& rhs) { return
	 * (lhs.dFitness < rhs.dFitness); }
	 */
}
