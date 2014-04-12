package swarm.pso;

import java.util.Arrays;
import java.util.List;

public class SwarmDriver {
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
		// function represents f = x^2 + y^2 + z^2
		PSOFunction<Double> function = new SumOfSquares(3);
		List<Double> lowerBounds = Arrays.asList(new Double[function.getDimensions()]);
		List<Double> upperBounds = Arrays.asList(new Double[function.getDimensions()]);
		// optimize arguments on (-1, 3)
		for (int i = 0; i < lowerBounds.size(); i++) {
			lowerBounds.set(i, (double) -1);
			upperBounds.set(i, (double) 3);
		}
		
		// call the constructor to initialize the optimization class and set up particles
		SequentialOptimization pso = new SequentialOptimization(function.getDimensions(), function, lowerBounds, upperBounds, 10);
		
		// call the optimize method and get the results
		List<Double> solution = pso.optimize();
		
		System.out.println(solution);
		System.out.println(function.function(solution));
	}
}
