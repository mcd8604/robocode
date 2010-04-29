package mcd.NeuralNet;

import java.util.ArrayList;
import java.util.Random;

public class GeneticAlg {

	Random rand = new Random();

	private static final int iNumElite = 4;
	private static final int iNumCopiesElite = 1;

	private static final int dMaxPerturbation = 0;

	// this holds the entire population of chromosomes
	ArrayList<Genome> m_vecPop;

	// size of population
	int m_iPopSize;

	// amount of weights per chromo
	int m_iChromoLength;

	// total fitness of population
	double m_dTotalFitness;

	// best fitness this population
	double m_dBestFitness;

	// average fitness
	double m_dAverageFitness;

	// worst
	double m_dWorstFitness;

	// keeps track of the best genome
	int m_iFittestGenome;

	// probability that a chromosones bits will mutate.
	// Try figures around 0.05 to 0.3 ish
	double m_dMutationRate;

	// probability of chromosones crossing over bits
	// 0.7 is pretty good
	double m_dCrossoverRate;

	// generation counter
	int m_cGeneration;

	// -----------------------------------constructor-------------------------
	//
	// sets up the population with random floats
	//
	// -----------------------------------------------------------------------
	public GeneticAlg(int popsize, double MutRat, double CrossRat,
			int numweights) {
		m_iPopSize = popsize;
		m_dMutationRate = MutRat;
		m_dCrossoverRate = CrossRat;
		m_iChromoLength = numweights;
		m_dTotalFitness = 0;
		m_cGeneration = 0;
		m_iFittestGenome = 0;
		m_dBestFitness = 0;
		m_dWorstFitness = 99999999;
		m_dAverageFitness = 0;
		// initialise population with chromosomes consisting of random
		// weights and all fitnesses set to zero
		for (int i = 0; i < m_iPopSize; ++i) {
			m_vecPop.add(new Genome());

			for (int j = 0; j < m_iChromoLength; ++j) {
				m_vecPop.get(i).vecWeights.add(RandomClamped());
			}
		}
	}

	// ---------------------------------Mutate--------------------------------
	//
	// mutates a chromosome by perturbing its weights by an amount not
	// greater than CParams::dMaxPerturbation
	// -----------------------------------------------------------------------
	void Mutate(ArrayList<Double> chromo) {
		// traverse the chromosome and mutate each weight dependent
		// on the mutation rate
		for (int i = 0; i < chromo.size(); ++i) {
			// do we perturb this weight?
			if (rand.nextFloat() < m_dMutationRate) {
				// add or subtract a small value to the weight
				chromo.set(i, chromo.get(i) + RandomClamped()
						* dMaxPerturbation);
			}
		}
	}

	private Double RandomClamped() {
		return rand.nextDouble() - rand.nextDouble();
	}

	// ----------------------------------GetChromoRoulette()------------------
	//
	// returns a chromo based on roulette wheel sampling
	//
	// -----------------------------------------------------------------------
	Genome GetChromoRoulette() {
		// generate a random number between 0 & total fitness count
		double Slice = (double) (rand.nextFloat() * m_dTotalFitness);

		// this will be set to the chosen chromosome
		Genome TheChosenOne = null;

		// go through the chromosones adding up the fitness so far
		double FitnessSoFar = 0;

		for (int i = 0; i < m_iPopSize; ++i) {
			FitnessSoFar += m_vecPop.get(i).dFitness;

			// if the fitness so far > random number return the chromo at
			// this point
			if (FitnessSoFar >= Slice) {
				TheChosenOne = m_vecPop.get(i);

				break;
			}

		}

		return TheChosenOne;
	}

	// -------------------------------------Crossover()-----------------------
	//	
	// given parents and storage for the offspring this method performs
	// crossover according to the GAs crossover rate
	// -----------------------------------------------------------------------
	void Crossover(ArrayList<Double> mum, ArrayList<Double> dad,
			ArrayList<Double> baby1, ArrayList<Double> baby2) {
		// just return parents as offspring dependent on the rate
		// or if parents are the same
		if ((rand.nextFloat() > m_dCrossoverRate) || (mum == dad)) {
			baby1 = mum;
			baby2 = dad;

			return;
		}

		// determine a crossover point
		int cp = rand.nextInt(m_iChromoLength - 1);

		// create the offspring
		for (int i = 0; i < cp; ++i) {
			baby1.add(mum.get(i));
			baby2.add(dad.get(i));
		}

		for (int i = cp; i < mum.size(); ++i) {
			baby1.add(dad.get(i));
			baby2.add(mum.get(i));
		}

		return;
	}

	// -----------------------------------Epoch()-----------------------------
	//
	// takes a population of chromosones and runs the algorithm through one
	// cycle.
	// Returns a new population of chromosones.
	//
	// -----------------------------------------------------------------------
	ArrayList<Genome> Epoch(ArrayList<Genome> old_pop) {
		// assign the given population to the classes population
		m_vecPop.clear();
		m_vecPop.addAll(old_pop);

		// reset the appropriate variables
		Reset();

		// sort the population (for scaling and elitism)
		// sort(m_vecPop.begin(), m_vecPop.end());

		// calculate best, worst, average and total fitness
		CalculateBestWorstAvTot();

		// create a temporary vector to store new chromosones
		ArrayList<Genome> vecNewPop = new ArrayList<Genome>();

		// Now to add a little elitism we shall add in some copies of the
		// fittest genomes. Make sure we add an EVEN number or the roulette
		// wheel sampling will crash
		if (iNumCopiesElite * iNumElite % 2 != 0) {
			GrabNBest(iNumElite, iNumCopiesElite, vecNewPop);
		}

		// now we enter the GA loop

		// repeat until a new population is generated
		while (vecNewPop.size() < m_iPopSize) {
			// grab two chromosones
			Genome mum = GetChromoRoulette();
			Genome dad = GetChromoRoulette();

			// create some offspring via crossover
			ArrayList<Double> baby1 = new ArrayList<Double>();
			ArrayList<Double> baby2 = new ArrayList<Double>();

			Crossover(mum.vecWeights, dad.vecWeights, baby1, baby2);

			// now we mutate
			Mutate(baby1);
			Mutate(baby2);

			// now copy into vecNewPop population
			vecNewPop.add(new Genome(baby1, 0));
			vecNewPop.add(new Genome(baby2, 0));
		}

		// finished so assign new pop back into m_vecPop
		m_vecPop = vecNewPop;

		return m_vecPop;
	}

	// -------------------------GrabNBest----------------------------------
	//
	// This works like an advanced form of elitism by inserting NumCopies
	// copies of the NBest most fittest genomes into a population vector
	// --------------------------------------------------------------------
	void GrabNBest(int NBest, int NumCopies, ArrayList<Genome> Pop) {
		// add the required amount of copies of the n most fittest
		// to the supplied vector
		while (NBest-- > 0) {
			for (int i = 0; i < NumCopies; ++i) {
				Pop.add(m_vecPop.get(m_iPopSize - 1 - NBest));
			}
		}
	}

	// -----------------------CalculateBestWorstAvTot-----------------------
	//
	// calculates the fittest and weakest genome and the average/total
	// fitness scores
	// ---------------------------------------------------------------------
	void CalculateBestWorstAvTot() {
		m_dTotalFitness = 0;

		double HighestSoFar = 0;
		double LowestSoFar = 9999999;

		for (int i = 0; i < m_iPopSize; ++i) {
			// update fittest if necessary
			if (m_vecPop.get(i).dFitness > HighestSoFar) {
				HighestSoFar = m_vecPop.get(i).dFitness;

				m_iFittestGenome = i;

				m_dBestFitness = HighestSoFar;
			}

			// update worst if necessary
			if (m_vecPop.get(i).dFitness < LowestSoFar) {
				LowestSoFar = m_vecPop.get(i).dFitness;

				m_dWorstFitness = LowestSoFar;
			}

			m_dTotalFitness += m_vecPop.get(i).dFitness;

		}// next chromo

		m_dAverageFitness = m_dTotalFitness / m_iPopSize;
	}

	// -------------------------Reset()------------------------------
	//
	// resets all the relevant variables ready for a new generation
	// --------------------------------------------------------------
	void Reset() {
		m_dTotalFitness = 0;
		m_dBestFitness = 0;
		m_dWorstFitness = 9999999;
		m_dAverageFitness = 0;
	}

}
