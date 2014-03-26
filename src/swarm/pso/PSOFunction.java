package swarm.pso;

import java.util.List;

// Use different PSOFunction implementations to optimize different functions.
public abstract class PSOFunction<V> {
	public abstract V function(List<V> arguments);
	
	public abstract int getDimensions();
}
