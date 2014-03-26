package swarm.pso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Particle {
	private List<Double> position;
	private List<Double> velocity;
	private List<Double> bestPosition;
	
	private double value;
	private double bestValue;
	
	public Particle(List<Double> initialPosition, List<Double> initialVelocity, List<Double> bestPosition, double value, double bestValue) {
		if (initialPosition.size() != initialVelocity.size()) {
			throw new IllegalArgumentException("position and velocity of particles must have the same nonzero length");
		}
		this.position = new ArrayList<Double>(initialPosition);
		this.velocity = new ArrayList<Double>(initialVelocity);
		
		this.bestPosition = new ArrayList<Double>(initialPosition);
		
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
