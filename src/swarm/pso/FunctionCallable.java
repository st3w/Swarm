package swarm.pso;

import java.util.List;
import java.util.concurrent.Callable;

public class FunctionCallable<V> implements Callable<V> {
	private final List<V> arguments;
	private final PSOFunction<V> function;

	public FunctionCallable(PSOFunction<V> function, List<V> arguments) {
		this.arguments = arguments;
		this.function = function;
	}

	@Override
	public V call() throws Exception {
		return function.function(arguments);
	}

}
