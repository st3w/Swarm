package swarm.pso.test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import swarm.pso.model.PSOFunction;
import swarm.pso.service.SequentialOptimization;
import swarm.pso.structures.config.FunctionConfiguration;
import swarm.pso.structures.config.SwarmConfiguration;

public class SwarmDriver {
	public static final int DIMENSIONS = 3;
	private static final double[] LOWER_BOUNDS = {-5.0, -4.77, -3.0};
	private static final double[] UPPER_BOUNDS = {3.55, 6.22, 5.0};
	
	private static final double[] MAX_VELOCITY = {0.5, 0.5, 0.5};
	
	public static final double INITIAL_INERTIA = 0.9;
	public static final double FINAL_INERTIA = 0.4;
	public static final double SELF_WEIGHT = 1.0;
	public static final double BEST_WEIGHT = 1.0;
	public static final double FDR_WEIGHT = 0;
	
	public static final int NUMBER_PARTICLES = 100;
	public static final int NUMBER_ITERATIONS = 1000;
	
	public static final long SEED = 234568798;
	public static final boolean USE_SEED = false;
	
	public static void main(String[] args) {
		// Make a function to optimize
		PSOFunction<Double> function = new Functions.Rosenbrock(DIMENSIONS);
		
		// Function bounds are a list of parameters
		List<Double> lowerBounds = Arrays.asList(new Double[function.getDimensions()]);
		List<Double> upperBounds = Arrays.asList(new Double[function.getDimensions()]);
		
		List<Double> maximumVelocity = Arrays.asList(new Double[function.getDimensions()]);
		
		// optimize arguments within bounds
		for (int i = 0; i < lowerBounds.size(); i++) {
			lowerBounds.set(i, LOWER_BOUNDS[i]);
			upperBounds.set(i, UPPER_BOUNDS[i]);
			maximumVelocity.set(i, MAX_VELOCITY[i]);
		}
		
		FunctionConfiguration funcConf = new FunctionConfiguration(function.getDimensions(), function, 
				lowerBounds, upperBounds);
		
		SwarmConfiguration swarmConf = new SwarmConfiguration(INITIAL_INERTIA, FINAL_INERTIA, SELF_WEIGHT, BEST_WEIGHT,
				FDR_WEIGHT, NUMBER_PARTICLES, NUMBER_ITERATIONS, maximumVelocity, funcConf);
		
		Random rand;
		if (USE_SEED) {
			rand = new Random(SEED);
		}
		else {
			rand = new Random();
		}
		
		SequentialOptimization pso = new SequentialOptimization(swarmConf, rand);
		List<Double> solution = pso.optimize();
		
		System.out.println(solution);
		System.out.println(function.function(solution));
	}
}
