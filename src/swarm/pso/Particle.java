package swarm.pso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Particle {
	private List<Double> position; // The current position of the particle
	private List<Double> velocity; // The amount by which the particle is currently moving in each dimension.
	private List<Double> bestPosition; // The position this particle has previously visited with the best value.
	
	private double value; // f(position) or the value of the current position
	private double bestValue; // f(bestPosition) or the value of the best known position. Should be better than or equal to value.
	
	public Particle(List<Double> position, List<Double> velocity, List<Double> bestPosition, double value, double bestValue) {
		if (position.size() == 0 || position.size() != velocity.size()) {
			throw new IllegalArgumentException("position and velocity of particles must have the same nonzero length");
		}
		if (position.size() != velocity.size()) {
			throw new IllegalArgumentException("position and best position of particles must have the same nonzero length");
		}
		//Copy all input values to make structure immutable
		this.position = new ArrayList<Double>(position);
		this.velocity = new ArrayList<Double>(velocity);
		
		this.bestPosition = new ArrayList<Double>(bestPosition);
		
		this.value = value;
		this.bestValue = bestValue;
	}
	
	public List<Double> getVelocity() {
		return Collections.unmodifiableList(velocity);
	}
	
	public List<Double> getPosition() {
		return Collections.unmodifiableList(position);
	}
	
	public List<Double> getBestPosition() {
		return Collections.unmodifiableList(bestPosition);
	}
	
	public double getValue() {
		return value;
	}
	
	public double getBestValue() {
		return bestValue;
	}
	
	public int getDimensions() {
		return position.size();
	}
}
