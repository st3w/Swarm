package swarm.pso;

import java.util.List;

public class FunctionConfiguration {
	private final int dimensions;
	private final PSOFunction<Double> function;
	private final List<Double> lowerBounds;
	private final List<Double> upperBounds;
	
	public FunctionConfiguration(int dimensions, PSOFunction<Double> function,
			List<Double> lowerBounds, List<Double> upperBounds) {
		this.dimensions = dimensions;
		this.function = function;
		this.lowerBounds = lowerBounds;
		this.upperBounds = upperBounds;
	}

	public int getDimensions() {
		return dimensions;
	}

	public PSOFunction<Double> getFunction() {
		return function;
	}

	public List<Double> getLowerBounds() {
		return lowerBounds;
	}

	public List<Double> getUpperBounds() {
		return upperBounds;
	}
	
}
