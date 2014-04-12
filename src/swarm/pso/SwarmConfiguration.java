package swarm.pso;

import java.util.List;

public class SwarmConfiguration {
	private final double inertia;
	private final double selfWeight;
	private final double bestWeight;
	private final double fdrWeight;
	
	private final FunctionConfiguration functionConf;
	private final List<Double> maximumVelocity;
	
	public SwarmConfiguration(double inertia, double selfWeight, double bestWeight, 
			double fdrWeight, FunctionConfiguration functionConf, List<Double> maximumVelocity) {
		this.inertia = inertia;
		this.selfWeight = selfWeight;
		this.bestWeight = bestWeight;
		this.fdrWeight = fdrWeight;
		this.functionConf = functionConf;
		this.maximumVelocity = maximumVelocity;
	}

	public double getInertia() {
		return inertia;
	}

	public double getSelfWeight() {
		return selfWeight;
	}

	public double getBestWeight() {
		return bestWeight;
	}

	public double getFdrWeight() {
		return fdrWeight;
	}

	public FunctionConfiguration getFunctionConf() {
		return functionConf;
	}

	public List<Double> getMaximumVelocity() {
		return maximumVelocity;
	}
}
