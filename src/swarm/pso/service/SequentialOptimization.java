package swarm.pso.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import swarm.pso.logging.Logging;
import swarm.pso.structures.Particle;
import swarm.pso.structures.config.SwarmConfiguration;

public class SequentialOptimization implements SwarmOptimization {
	private final SwarmConfiguration config; // SwarmConfiguration has info about the function and particle behavior
	
	private final List<Particle> particles;
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private double inertia;
	
	private final Random rand; // random number generation
	
	private final Logging log;
	
	public SequentialOptimization(SwarmConfiguration config, Random rand, Logging log) {
		this.config = config;
		inertia = config.getInertia();
		this.rand = rand;
		this.log = log;
		
		log.setStartTime();
		
		particles = Arrays.asList(new Particle[config.getNumParticles()]);
		
		for (int p = 0; p < config.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			double initialValue = config.function(initialPosition);
			List<Double> initialVelocity = initialVelocity();
			setParticle(p, new Particle(initialPosition, initialVelocity, 
					initialPosition, initialValue, initialValue));
			updateGlobalBest(getParticle(p));
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

	private Particle getParticle(int index) {
		return particles.get(index);
	}
	
	private void setParticle(int index, Particle particle) {
		particles.set(index, particle);
	}
	
	@Override
	public List<Double> optimize() {
		// Perform iterations
		for (int i = 0; i < config.getNumIterations(); i++) {
			updateParticleList(i);
		}
		return bestPosition;
	}
	
	public List<Double> optimize(int timeout) {
		// Perform iterations
		for (int i = 0; i < config.getNumIterations(); i++) {
			updateParticleList(i);
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				
			}
		}
		return bestPosition;
	}
	

	private void updateParticleList(int iteration) {
		for (int p = 0; p < config.getNumParticles(); p++) {
			updateParticle(iteration, p, inertia);
		}
		updateInertia(iteration+1);
		log.addBestPosition(iteration, bestPosition);
		log.addTime(iteration, System.nanoTime());
	}
	
	private void updateInertia(int iteration) {
		inertia = ((config.getInertia() - config.getMinInertia()) * (config.getNumIterations() - (iteration))) /
				config.getNumIterations() + config.getMinInertia();
//		inertia = ((inertia - config.getMinInertia()) * (config.getNumIterations() - (iteration))) /
//				(config.getNumIterations() + config.getMinInertia());
	}
	
	private void updateParticle(int iteration, int particle, double inertia) {
		List<Double> velocity = calculateVelocity(particle, inertia);
		List<Double> position = calculatePosition(particle, velocity);
		List<Double> bestPosition = selectBestPosition(particle, position);
		
		//System.out.println("Particle: " + position + ", " + velocity + ", " + function.function(position));
		setParticle(particle, new Particle(position, velocity, bestPosition, 
				config.function(position), config.function(bestPosition)));
		updateGlobalBest(getParticle(particle));
		
		log.addParticlePosition(iteration, particle, position);
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
			double dPos = oldPosition.get(d) + velocity.get(d);
			position.set(d, Math.min(config.getUpperBounds().get(d), 
					Math.max(config.getLowerBounds().get(d), dPos)));
			if (position.get(d) != dPos) {
				velocity.set(d, -velocity.get(d));
			}
		}
		
		return position;
	}

	private List<Double> calculateVelocity(int particle, double inertia) {
		List<Double> velocity = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldVelocity = getParticle(particle).getVelocity();
		List<Double> position = getParticle(particle).getPosition();
		List<Double> selfBestPosition = getParticle(particle).getBestPosition();
		for (int d = 0; d < config.getDimensions(); d++) {
			List<Double> fdrPosition;
			if (config.getFdrWeight() != 0.0) {
				fdrPosition = bestFitnessDistance(particle, d);
			}
			else {
				fdrPosition = selfBestPosition;
			}
			double dVelocity = inertia * oldVelocity.get(d) + 
					rand.nextDouble() * config.getSelfWeight() * (selfBestPosition.get(d) - position.get(d)) + 
					rand.nextDouble() * config.getBestWeight() * (bestPosition.get(d) - position.get(d)) + 
					rand.nextDouble() * config.getFdrWeight() * (fdrPosition.get(d) - position.get(d));
			velocity.set(d, Math.signum(dVelocity) * Math.min(config.getMaximumVelocity().get(d), Math.abs(dVelocity)));
		}
		return velocity;
	}
	
	private List<Double> bestFitnessDistance(int particle, int dimension) {
		//List<Double> bestFDRPosition = particles.get(0).getBestPosition();
		List<Double> bestFDRPosition = getParticle((particle+1)%config.getNumParticles()).getBestPosition();
		//double bestFDR = fdr(particle, 0, dimension);
		double bestFDR = fdr(particle, (particle+1)%config.getNumParticles(), dimension);
		for (int q = particle+1; q < config.getNumParticles(); q++) {
			double fdr = fdr(particle, q, dimension);
			if (fdr > bestFDR) {
				bestFDRPosition = getParticle(q).getBestPosition();
				bestFDR = fdr;
			}
		}
		for (int q = 0; q < particle; q++) {
			double fdr = fdr(particle, q, dimension);
			if (fdr > bestFDR) {
				bestFDRPosition = getParticle(q).getBestPosition();
				bestFDR = fdr;
			}
		}
		return bestFDRPosition;
	}
	
	private double fdr(int currentParticle, int foreignParticle, int dimension) {
		double fitness = -(getParticle(foreignParticle).getBestValue() - getParticle(currentParticle).getValue());
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