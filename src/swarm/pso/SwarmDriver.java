package swarm.pso;

import java.util.Arrays;
import java.util.List;

public class SwarmDriver {
	static class SumOfSquares extends PSOFunction<Double> {
		public final int dimensions;
		
		public SumOfSquares(int dimensions) {
			this.dimensions = dimensions;
		}
		
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
		PSOFunction<Double> function = new SumOfSquares(3);
		List<Double> lowerBounds = Arrays.asList(new Double[function.getDimensions()]);
		List<Double> upperBounds = Arrays.asList(new Double[function.getDimensions()]);
		for (int i = 0; i < lowerBounds.size(); i++) {
			lowerBounds.set(i, (double) -1);
			upperBounds.set(i, (double) 3);
		}
		SequentialOptimization pso = new SequentialOptimization(function.getDimensions(), function, lowerBounds, upperBounds, 10);
		
		List<Double> solution = pso.optimize();
		
		System.out.println(solution);
		System.out.println(function.function(solution));
	}
}
