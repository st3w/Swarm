package swarm.pso.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import swarm.pso.structures.Particle;
import swarm.pso.structures.config.ConcurrentSwarmConfiguration;

public class ConcurrentOptimization implements SwarmOptimization {
	private final ConcurrentSwarmConfiguration config;
	
	private final List<Particle> particles;
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private AtomicInteger nextUpdateParticle = new AtomicInteger(0);
	private AtomicInteger currentIteration = new AtomicInteger(0);
	
	private final Random rand;
	
	public ConcurrentOptimization(ConcurrentSwarmConfiguration config) {
		this(config, new Random());
	}
	
	public ConcurrentOptimization(ConcurrentSwarmConfiguration config, Random rand) {
		this.config = config;
		this.rand = rand;
		
		particles = Arrays.asList(new Particle[config.getNumParticles()]);

		for (int p = 0; p < config.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			
			double initialValue = config.function(initialPosition);
			
			List<Double> initialVelocity = initialVelocity();
			
			Particle particle = new Particle(initialPosition, initialVelocity, initialPosition, initialValue, initialValue);
						
			setParticle(p, particle);
		
			updateGlobalBest(getParticle(p));
		}
	}
	
	private void updateGlobalBest(Particle p) {
		if (bestPosition == null || p.getValue() < bestValue) {
			bestPosition = p.getPosition();
			bestValue = p.getValue();
		}
	}

	private List<Double> initialPosition() {
		List<Double> position = Arrays.asList(new Double[config.getDimensions()]);
		for (int d = 0; d < config.getDimensions(); d++) {
			Double lowPos = config.getLowerBounds().get(d);
			Double highPos = config.getUpperBounds().get(d);
			position.set(d, lowPos+rand.nextDouble()*(highPos-lowPos));
		}
		return position;
	}
	
	private List<Double> initialVelocity() {
		List<Double> velocity = Arrays.asList(new Double[config.getDimensions()]);
		for (int d = 0; d < config.getDimensions(); d++) {
			//Double lowPos = lowerBounds.get(d); //Possible 
			//Double highPos = upperBounds.get(d);
			//Double lowVel = lowPos - highPos;
			//Double highVel = highPos - lowPos;
			Double lowVel = -config.getMaximumVelocity().get(d);
			Double highVel = config.getMaximumVelocity().get(d);
			velocity.set(d, lowVel+rand.nextDouble()*(highVel-lowVel));
		}
		return velocity;
	}

	private Particle getParticle(int index) {
		return particles.get(index);
	}
	
	private void setParticle(int index, Particle particle) {
		particles.set(index, particle);
	}
	
	@Override
	public List<Double> optimize() {
		// Perform iterations
		for (int p = 0; p < config.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		for (int i = 0; i < 1000; i++) {
			updateParticleList();
		}
		System.out.println("-----------------------------------------------------");
		for (int p = 0; p < config.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		return bestPosition;
	}

	private void updateParticleList() {
		for (int p = 0; p < config.getNumParticles(); p++) {
			updateParticle(p);
		}
	}
	
	private void updateParticle(int particle) {
		List<Double> velocity = calculateVelocity(particle);
		List<Double> position = calculatePosition(particle, velocity);
		List<Double> bestPosition = selectBestPosition(particle, position);
		
		//System.out.println("Particle: " + position + ", " + velocity + ", " + function.function(position));
		setParticle(particle, new Particle(position, velocity, bestPosition, config.function(position), config.function(bestPosition)));
		updateGlobalBest(getParticle(particle));
		
	}

	private List<Double> selectBestPosition(int particle, List<Double> newPosition) {
		List<Double> currentBestPosition = getParticle(particle).getBestPosition();
		if (config.function(currentBestPosition) <= config.function(newPosition)) {
			return currentBestPosition;
		}
		else {
			return newPosition;
		}
	}

	private List<Double> calculatePosition(int particle, List<Double> velocity) {
		List<Double> position = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldPosition = getParticle(particle).getPosition();
		
		for (int d = 0; d < config.getDimensions(); d++) {
			position.set(d, Math.min(config.getUpperBounds().get(d), Math.max(config.getLowerBounds().get(d), oldPosition.get(d) + velocity.get(d))));
		}
		
		return position;
	}

	private List<Double> calculateVelocity(int particle) {
		List<Double> velocity = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldVelocity = getParticle(particle).getVelocity();
		List<Double> position = getParticle(particle).getPosition();
		List<Double> selfBestPosition = getParticle(particle).getBestPosition();
		for (int d = 0; d < config.getDimensions(); d++) {
			List<Double> fdrPosition = bestFitnessDistance(particle, d);
			double dVelocity = config.getInertia() * oldVelocity.get(d) + 
					config.getSelfWeight() * (selfBestPosition.get(d) - position.get(d)) + 
					config.getBestWeight() * (bestPosition.get(d) - position.get(d)) + 
					config.getFdrWeight() * (fdrPosition.get(d) - position.get(d));
			velocity.set(d, Math.min(config.getMaximumVelocity().get(d), Math.max(-config.getMaximumVelocity().get(d), dVelocity)));
		}
		return velocity;
	}

	private List<Double> bestFitnessDistance(int particle, int dimension) {
		List<Double> bestFDRPosition = null;
		double bestFDR = 0;
		for (int q = 0; q < config.getNumParticles(); q++) {
			//if (q != particle) {
			double fdr = fdr(particle, q, dimension);
			if (bestFDRPosition == null || fdr > bestFDR) {
				bestFDRPosition = getParticle(q).getBestPosition();
				bestFDR = fdr;
			}
			//}
		}
		return bestFDRPosition;
	}

	private double fdr(int currentParticle, int foreignParticle, int dimension) {
		double fitness = getParticle(currentParticle).getValue() - getParticle(foreignParticle).getBestValue();
		double distance = Math.abs(
				getParticle(foreignParticle).getBestPosition().get(dimension) - 
				getParticle(currentParticle).getPosition().get(dimension));
		if (distance != 0) {
			return fitness/distance;
		}
		else {
			return Double.MAX_VALUE;
		}
	}

}
