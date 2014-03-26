package swarm.pso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SequentialOptimization {
	//Optimize function on interval from lower to upper bounds
	private final int dimensions;
	private final PSOFunction<Double> function;
	private final List<Double> lowerBounds;
	private final List<Double> upperBounds;

	//Use numberOfParticles 
	private final int numberOfParticles;
	
	private final List<Particle> particles;
	
	//private final int iterations;
	private final Double[] mv = {0.2, 0.2, 0.2};
	private final List<Double> maximumVelocity = Arrays.asList(mv);
	private final double inertia = 0.2;
	private final double selfWeight = 0.2;
	private final double bestWeight = 0.2;
	private final double fdrWeight = 0.4;
	
	private List<Double> bestPosition = null;
	private double bestValue;
	
	private final Random rand;
	
	public SequentialOptimization(int dimensions, PSOFunction<Double> function, List<Double> lowerBounds, List<Double> upperBounds, int numberOfParticles) {
		if (dimensions != lowerBounds.size() || dimensions != upperBounds.size()) {
			throw new IllegalArgumentException("Bounds must have size() = dimensions");
		}
		if (dimensions != function.getDimensions()) {
			throw new IllegalArgumentException("Function must have getDimensions() = dimensions");
		}
		
		this.dimensions = dimensions;
		this.function = function;
		this.lowerBounds = Collections.unmodifiableList(new ArrayList<Double>(lowerBounds));
		this.upperBounds = Collections.unmodifiableList(new ArrayList<Double>(upperBounds));
		
		this.numberOfParticles = numberOfParticles;

		particles = Arrays.asList(new Particle[numberOfParticles]);

		rand = new Random();
		
		for (int p = 0; p < numberOfParticles; p++) {
			List<Double> initialPosition = initialPosition();
			
			double initialValue = function.function(initialPosition);
			
			List<Double> initialVelocity = initialVelocity();
			
			particles.set(p, new Particle(initialPosition, initialVelocity, initialPosition, initialValue, initialValue));
		
			updateGlobalBest(particles.get(p));
		}
	}
	
	public List<Double> optimize() {
		// Perform iterations
		for (int p = 0; p < numberOfParticles; p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		updateParticleList();
		System.out.println("-----------------------------------------------------");
		for (int p = 0; p < numberOfParticles; p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		return bestPosition;
	}

	private void updateParticleList() {
		for (int p = 0; p < numberOfParticles; p++) {
			updateParticle(p);
		}
	}
	
	private void updateParticle(int particle) {
		//System.out.println("Particle " + particle + ", initialPosition: " + particles.get(particle).getPosition());
		
		List<Double> velocity = calculateVelocity(particle);
		List<Double> position = calculatePosition(particle, velocity);
		List<Double> bestPosition = selectBestPosition(particle, position);
		
		//System.out.println("Particle: " + position + ", " + velocity + ", " + function.function(position));
		particles.set(particle, new Particle(position, velocity, bestPosition, function.function(position), function.function(bestPosition)));
		updateGlobalBest(particles.get(particle));
		
	}

	private List<Double> selectBestPosition(int particle, List<Double> newPosition) {
		List<Double> currentBestPosition = particles.get(particle).getBestPosition();
		if (function.function(currentBestPosition) <= function.function(newPosition)) {
			return currentBestPosition;
		}
		else {
			return newPosition;
		}
	}

	private List<Double> calculatePosition(int particle, List<Double> velocity) {
		List<Double> position = Arrays.asList(new Double[dimensions]);
		List<Double> oldPosition = particles.get(particle).getPosition();
		
		for (int d = 0; d < dimensions; d++) {
			position.set(d, Math.min(upperBounds.get(d), Math.max(lowerBounds.get(d), oldPosition.get(d) + velocity.get(d))));
		}
		
		return position;
	}

	private List<Double> calculateVelocity(int particle) {
		List<Double> velocity = Arrays.asList(new Double[dimensions]);
		List<Double> oldVelocity = particles.get(particle).getVelocity();
		List<Double> position = particles.get(particle).getPosition();
		List<Double> selfBestPosition = particles.get(particle).getBestPosition();
		for (int d = 0; d < dimensions; d++) {
			List<Double> fdrPosition = bestFitnessDistance(particle, d);
			double dVelocity = inertia * oldVelocity.get(d) + 
					selfWeight * (selfBestPosition.get(d) - position.get(d)) + 
					bestWeight * (bestPosition.get(d) - position.get(d)) + 
					fdrWeight * (fdrPosition.get(d) - position.get(d));
			velocity.set(d, Math.min(maximumVelocity.get(d), Math.max(-maximumVelocity.get(d), dVelocity)));
			velocity.set(d, dVelocity);
		}
		return velocity;
	}

	private List<Double> bestFitnessDistance(int particle, int dimension) {
		List<Double> bestFDRPosition = null;
		double bestFDR = 0;
		for (int q = 0; q < numberOfParticles; q++) {
			//if (q != particle) {
				double fdr = fdr(particle, q, dimension);
				if (bestFDRPosition == null || fdr > bestFDR) {
					bestFDRPosition = particles.get(q).getBestPosition();
					bestFDR = fdr;
				}
			//}
		}
		return bestFDRPosition;
	}

	private double fdr(int currentParticle, int foreignParticle, int dimension) {
		double fitness = particles.get(currentParticle).getValue() - particles.get(foreignParticle).getBestValue();
		double distance = Math.abs(
				particles.get(foreignParticle).getBestPosition().get(dimension) - 
				particles.get(currentParticle).getPosition().get(dimension));
		if (distance != 0) {
			return fitness/distance;
		}
		else {
			return Double.MAX_VALUE;
		}
	}

	private List<Double> initialPosition() {
		List<Double> position = Arrays.asList(new Double[dimensions]);
		for (int d = 0; d < dimensions; d++) {
			Double lowPos = lowerBounds.get(d);
			Double highPos = upperBounds.get(d);
			position.set(d, lowPos+rand.nextDouble()*(highPos-lowPos));
		}
		return position;
	}
	
	private List<Double> initialVelocity() {
		List<Double> velocity = Arrays.asList(new Double[dimensions]);
		for (int d = 0; d < dimensions; d++) {
			//Double lowPos = lowerBounds.get(d);
			//Double highPos = upperBounds.get(d);
			//Double lowVel = lowPos - highPos;
			//Double highVel = highPos - lowPos;
			Double lowVel = -maximumVelocity.get(d);
			Double highVel = maximumVelocity.get(d);
			velocity.set(d, lowVel+rand.nextDouble()*(highVel-lowVel));
		}
		return velocity;
	}

	private void updateGlobalBest(Particle p) {
		if (bestPosition == null || p.getValue() < bestValue) {
			bestPosition = p.getPosition();
			bestValue = p.getValue();
		}
	}
}
