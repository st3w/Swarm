package swarm.pso.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import swarm.pso.structures.Particle;
import swarm.pso.structures.config.SwarmConfiguration;

public class SequentialOptimization implements SwarmOptimization {
	private final SwarmConfiguration config; // SwarmConfiguration has info about the function and particle behavior
	
	private final List<Particle> particles;
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private double inertia;
	
	private final Random rand; // random number generation
	
	public SequentialOptimization(SwarmConfiguration config, Random rand) {
		this.config = config;
		inertia = config.getInertia();
		this.rand = rand;
		
		particles = Arrays.asList(new Particle[config.getNumParticles()]);
		
		for (int p = 0; p < config.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			double initialValue = config.function(initialPosition);
			List<Double> initialVelocity = initialVelocity();
			particles.set(p, new Particle(initialPosition, initialVelocity, 
					initialPosition, initialValue, initialValue));
			updateGlobalBest(particles.get(p));
		}
	}
	
	public List<Double> optimize() {
		// Perform iterations
		for (int p = 0; p < config.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		System.out.println("Inertia: " + inertia);
		for (int i = 0; i < config.getNumIterations()/2; i++) {
			updateParticleList();
			updateInertia(i+1);
		}
		System.out.println("-----------------------------------------------------");
		for (int p = 0; p < config.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		System.out.println("Inertia: " + inertia);
		for (int i = config.getNumIterations()/2; i < config.getNumIterations(); i++) {
			updateParticleList();
			updateInertia(i+1);
		}
		System.out.println("-----------------------------------------------------");
		for (int p = 0; p < config.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		System.out.println("Inertia: " + inertia);
		return bestPosition;
	}
	
	private void updateInertia(int iteration) {
		inertia = ((config.getInertia() - config.getMinInertia()) * (config.getNumIterations() - (iteration))) /
				config.getNumIterations() + config.getMinInertia();
	}

	private void updateParticleList() {
		for (int p = 0; p < config.getNumParticles(); p++) {
			updateParticle(p);
		}
	}
	
	private void updateParticle(int particle) {
		//System.out.println("Particle " + particle + ", initialPosition: " + particles.get(particle).getPosition());
		
		List<Double> velocity = calculateVelocity(particle);
		List<Double> position = calculatePosition(particle, velocity);
		List<Double> bestPosition = selectBestPosition(particle, position);
		
		//System.out.println("Particle: " + position + ", " + velocity + ", " + function.function(position));
		particles.set(particle, new Particle(position, velocity, bestPosition, 
				config.function(position), config.function(bestPosition)));
		updateGlobalBest(particles.get(particle));	
	}
	
	private List<Double> selectBestPosition(int particle, List<Double> newPosition) {
		List<Double> currentBestPosition = particles.get(particle).getBestPosition();
		if (config.function(currentBestPosition) <= config.function(newPosition)) {
			return currentBestPosition;
		}
		else {
			return newPosition;
		}
	}
	
	private List<Double> calculatePosition(int particle, List<Double> velocity) {
		List<Double> position = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldPosition = particles.get(particle).getPosition();
		
		for (int d = 0; d < config.getDimensions(); d++) {
			position.set(d, Math.min(config.getUpperBounds().get(d), 
					Math.max(config.getLowerBounds().get(d), oldPosition.get(d) + velocity.get(d))));
		}
		
		return position;
	}

	private List<Double> calculateVelocity(int particle) {
		List<Double> velocity = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldVelocity = particles.get(particle).getVelocity();
		List<Double> position = particles.get(particle).getPosition();
		List<Double> selfBestPosition = particles.get(particle).getBestPosition();
		for (int d = 0; d < config.getDimensions(); d++) {
			List<Double> fdrPosition = bestFitnessDistance(particle, d);
			double dVelocity = inertia * oldVelocity.get(d) + 
					config.getSelfWeight() * (selfBestPosition.get(d) - position.get(d)) + 
					config.getBestWeight() * (bestPosition.get(d) - position.get(d)) + 
					config.getFdrWeight() * (fdrPosition.get(d) - position.get(d));
			velocity.set(d, Math.min(config.getMaximumVelocity().get(d),
					Math.max(-config.getMaximumVelocity().get(d), dVelocity)));
		}
		return velocity;
	}
	
	private List<Double> bestFitnessDistance(int particle, int dimension) {
		List<Double> bestFDRPosition = null;
		double bestFDR = 0;
		//double bestFDR = (particle!=0)?0:1;
		for (int q = 0; q < config.getNumParticles(); q++) {
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
			//Double lowPos = lowerBounds.get(d);
			//Double highPos = upperBounds.get(d);
			//Double lowVel = lowPos - highPos;
			//Double highVel = highPos - lowPos;
			Double lowVel = -config.getMaximumVelocity().get(d);
			Double highVel = config.getMaximumVelocity().get(d);
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