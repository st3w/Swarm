package swarm.pso.model;

import java.util.List;

// Use different PSOFunction implementations to optimize different functions.
public abstract class PSOFunction<V> {
	//Performs the actual function given a list of arguments
	public abstract V function(List<V> arguments);
	
	public abstract int getDimensions();
	
	public abstract V getSolution();
	
	public abstract List<V> getLowerBounds();
	
	public abstract List<V> getUpperBounds();
}
