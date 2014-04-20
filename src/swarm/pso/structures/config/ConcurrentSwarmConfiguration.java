package swarm.pso.structures.config;

public class ConcurrentSwarmConfiguration extends SwarmConfiguration {
    private final int numThreads;

	public ConcurrentSwarmConfiguration(SwarmConfiguration swarmConfig, int numThreads) {
		super(swarmConfig);
        this.numThreads = numThreads;
	}

    public int getNumThreads() { return numThreads; }
}
