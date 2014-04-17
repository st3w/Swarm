package swarm.pso;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SwarmDriver {
	public static final int DIMENSIONS = 3;
	private static final double[] LOWER_BOUNDS = {-1.0, -2.0, -3.0};
	private static final double[] UPPER_BOUNDS = {3.55, 6.22, 1.0};
	
	private static final double[] MAX_VELOCITY = {0.5, 0.5, 0.5};
	
	public static final double INITIAL_INERTIA = 0.9;
	public static final double FINAL_INERTIA = 0.4;
	public static final double SELF_WEIGHT = 1.0;
	public static final double BEST_WEIGHT = 1.0;
	public static final double FDR_WEIGHT = 2.0;
	
	public static final int NUMBER_PARTICLES = 10;
	public static final int NUMBER_ITERATIONS = 1000;
	
	public static final long SEED = 234568798;
	public static final boolean USE_SEED = true;
	
	// SumOfSquares is a sample PSOFunction for testing purposes. It represents a sum-of-squares function with arbitrary dimensions.
	// For example, a new SumOfSquares(3) represents the function f = x^2 + y^2 + z^2.
	static class SumOfSquares extends PSOFunction<Double> {
		// Dimensions is the number of arguments in the function
		public final int dimensions;
		
		public SumOfSquares(int dimensions) {
			this.dimensions = dimensions;
		}
		
		// function is the function that accepts the list of numbers and returns the sum of their squares
		@Override
		public Double function(List<Double> arguments) {
			if (arguments.size() != getDimensions()) {
				throw new IllegalArgumentException("Expected " + dimensions + " doubles");
			}
			
			double sum = 0;
			for (int i = 0; i < dimensions; i++) {
				sum += Math.pow(arguments.get(i), 2);
			}
			return sum;
		}

		@Override
		public int getDimensions() {
			return dimensions;
		}
		
	}
	
	public static void main(String[] args) {
		// Make a 3 dimensional sum of squares function
		PSOFunction<Double> function = new SumOfSquares(DIMENSIONS);
		
		// Function bounds are a list of parameters
		List<Double> lowerBounds = Arrays.asList(new Double[function.getDimensions()]);
		List<Double> upperBounds = Arrays.asList(new Double[function.getDimensions()]);
		
		List<Double> maximumVelocity = Arrays.asList(new Double[function.getDimensions()]);
		
		// optimize arguments on (-1, 3)
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
