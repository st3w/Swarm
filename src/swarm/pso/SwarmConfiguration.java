package swarm.pso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwarmConfiguration {
	private final double inertia;
	private final double selfWeight;
	private final double bestWeight;
	private final double fdrWeight;

	private final List<Double> maximumVelocity;
	
	private final FunctionConfiguration functionConf;
	
	public SwarmConfiguration(double inertia, double selfWeight, double bestWeight, 
			double fdrWeight, List<Double> maximumVelocity, FunctionConfiguration functionConf) {
		this.inertia = inertia;
		this.selfWeight = selfWeight;
		this.bestWeight = bestWeight;
		this.fdrWeight = fdrWeight;
		this.maximumVelocity = Collections.unmodifiableList(new ArrayList<Double>(maximumVelocity));
		this.functionConf = functionConf;
	}

	public final double getInertia() {
		return inertia;
	}

	public final double getSelfWeight() {
		return selfWeight;
	}

	public final double getBestWeight() {
		return bestWeight;
	}

	public final double getFdrWeight() {
		return fdrWeight;
	}

	public final FunctionConfiguration getFunctionConf() {
		return functionConf;
	}

	public final List<Double> getMaximumVelocity() {
		return maximumVelocity;
	}
}
