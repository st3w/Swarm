package swarm.pso.test;

import java.util.Arrays;
import java.util.List;

import swarm.pso.model.PSOFunction;

public class Functions {
	// Sphere is a sample PSOFunction for testing purposes. It represents a sum-of-squares function with arbitrary dimensions.
	// For example, a new Sphere(3) represents the function f = x^2 + y^2 + z^2.
	public static class Sphere extends PSOFunction<Double> {
		// Dimensions is the number of arguments in the function
		public final int dimensions;
		public final double MINIMUM = 0;
		private final double lowerBound = -10.0;
		private final double upperBound = 10.0;
		
		public Sphere(int dimensions) {
			this.dimensions = dimensions;
		}
		
		public List<Double> getLowerBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, lowerBound);
			
			return bounds;
		}
		
		public List<Double> getUpperBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, upperBound);
			
			return bounds;
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

		@Override
		public Double getSolution() {
			return MINIMUM;
		}
		
	}
	
	public static class TableFunc extends PSOFunction<Double> {
		public final int dimensions;
		public static final double MINIMUM = -19.2085;
		private final double lowerBound = -10.0;
		private final double upperBound = 10.0;
		
		public TableFunc(int dimensions) {
			this.dimensions = dimensions;
		}
		
		public Double function(List<Double> arguments) {
			return - (Math.abs(
					Math.sin(arguments.get(0)) * Math.cos(arguments.get(1))
					* Math.exp(Math.abs(
							1 - (Math.sqrt(Math.pow(arguments.get(0), 2)
							+ Math.pow(arguments.get(1), 2)))/Math.PI))));
		}
		
		@Override
		public int getDimensions() {
			return dimensions;
		}

		@Override
		public Double getSolution() {
			return MINIMUM;
		}
		
		public List<Double> getLowerBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, lowerBound);
			
			return bounds;
		}
		
		public List<Double> getUpperBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, upperBound);
			
			return bounds;
		}
	}
	
	public static class Ackley extends PSOFunction<Double> {
		// Dimensions is the number of arguments in the function
		public final int dimensions;
		public static final double MINIMUM = 0;
		private final double lowerBound = -4.0;
		private final double upperBound = 4.0;
		
		public Ackley(int dimensions) {
			this.dimensions = dimensions;
		}
		
		// function is an Ackley function adapted from Wikipedia article en.wikipedia.org/wiki/Test_functions_for_optimization
		@Override
		public Double function(List<Double> arguments) {
			double sum1=0;
			double sum2=0;

			for (int i = 0; i < dimensions; i++) {
			    sum1 += Math.pow(arguments.get(i), 2);
			    sum2 += Math.cos(2*Math.PI*arguments.get(i));
			}
			return 20 + Math.E - 20 * Math.exp(-0.2 * Math.sqrt(sum1/dimensions)) - Math.exp(sum2/dimensions);
		}

		@Override
		public int getDimensions() {
			return dimensions;
		}

		@Override
		public Double getSolution() {
			return MINIMUM;
		}
		
		public List<Double> getLowerBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, lowerBound);
			
			return bounds;
		}
		
		public List<Double> getUpperBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, upperBound);
			
			return bounds;
		}
	}
	
	public static class Rosenbrock extends PSOFunction<Double> {
		// Dimensions is the number of arguments in the function
		public final int dimensions;
		public static final double MINIMUM = 0;
		private final double lowerBound = -10.0;
		private final double upperBound = 10.0;
		
		public Rosenbrock(int dimensions) {
			this.dimensions = dimensions;
		}
		
		// function is a Rosenbrock function adapted from Wikipedia article en.wikipedia.org/wiki/Test_functions_for_optimization
		@Override
		public Double function(List<Double> arguments) {
			double sum=0;

			for (int i = 0; i < dimensions - 1; i++) {
			    sum += 100 * Math.pow((arguments.get(i+1)-Math.pow(arguments.get(i), 2)),2) + Math.pow(arguments.get(i)-1, 2);
			}
			return sum;
		}

		@Override
		public int getDimensions() {
			return dimensions;
		}

		@Override
		public Double getSolution() {
			return MINIMUM;
		}
		
		public List<Double> getLowerBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, lowerBound);
			
			return bounds;
		}
		
		public List<Double> getUpperBounds() {
			List<Double> bounds = Arrays.asList(new Double[dimensions]);
			
			for (int i = 0; i < dimensions; i++)
				bounds.set(i, upperBound);
			
			return bounds;
		}
	}
}
