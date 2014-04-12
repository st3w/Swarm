package swarm.pso;

import java.util.ArrayList;
import java.util.Collections;
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
		this.lowerBounds = Collections.unmodifiableList(new ArrayList<Double>(lowerBounds));
		this.upperBounds = Collections.unmodifiableList(new ArrayList<Double>(upperBounds));
	}
	
	public FunctionConfiguration(FunctionConfiguration f) {
		this(f.dimensions, f.function, f.lowerBounds, f.upperBounds);
	}

	public final int getDimensions() {
		return dimensions;
	}

	public final PSOFunction<Double> getFunction() {
		return function;
	}

	public final List<Double> getLowerBounds() {
		return lowerBounds;
	}

	public final List<Double> getUpperBounds() {
		return upperBounds;
	}
	
}
