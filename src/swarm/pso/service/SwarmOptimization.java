package swarm.pso.service;

import java.util.List;

public interface SwarmOptimization {
	public List<Double> optimize();
	public List<Double> optimize(int delay);
}
