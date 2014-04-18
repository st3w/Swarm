package swarm.pso.structures.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwarmConfiguration extends FunctionConfiguration {
	private final double inertia;
	private final double minInertia;
	private final double selfWeight;
	private final double bestWeight;
	private final double fdrWeight;
	
	private final int numParticles;
	private final int numIterations;

	private final List<Double> maximumVelocity;
	
	public SwarmConfiguration(double inertia, double minInertia, double selfWeight, double bestWeight, 
			double fdrWeight, int numParticles, int numIterations, List<Double> maximumVelocity,
			FunctionConfiguration functionConf) {
		super(functionConf);
		
		this.inertia = inertia;
		this.minInertia = minInertia;
		this.selfWeight = selfWeight;
		this.bestWeight = bestWeight;
		this.fdrWeight = fdrWeight;
		
		this.numParticles = numParticles;
		this.numIterations = numIterations;
		
		this.maximumVelocity = Collections.unmodifiableList(new ArrayList<Double>(maximumVelocity));
	}
	
	public SwarmConfiguration(SwarmConfiguration other) {
		this(other.inertia, other.minInertia, other.selfWeight, other.bestWeight, other.fdrWeight, 
				other.numParticles, other.numIterations, other.maximumVelocity, other);
	}

	public final double getInertia() {
		return inertia;
	}
	
	public final double getMinInertia() {
		return minInertia;
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

	public final int getNumParticles() {
		return numParticles;
	}

	public final int getNumIterations() {
		return numIterations;
	}

	public final List<Double> getMaximumVelocity() {
		return maximumVelocity;
	}
}
