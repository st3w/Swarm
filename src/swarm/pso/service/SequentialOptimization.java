package swarm.pso.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import swarm.pso.logging.Logging;
import swarm.pso.structures.Particle;
import swarm.pso.structures.config.SwarmConfiguration;

//This class represents our first, sequential, implementation of FDR PSO
public class SequentialOptimization implements SwarmOptimization {
	private final SwarmConfiguration config; // SwarmConfiguration has info about the function and particle behavior
	
	private final List<Particle> particles; // The structures containing each particle's state information
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private double inertia; // The current inertia, linearly decreases from config's inertia to minInertia
	
	private final Random rand; // random number generation
	
	private final Logging log; // used to record state information
	
	public SequentialOptimization(SwarmConfiguration config, Random rand, Logging log) {
		this.config = config;
		inertia = config.getInertia();
		this.rand = rand;
		this.log = log;
		
		log.setStartTime();
		
		particles = Arrays.asList(new Particle[config.getNumParticles()]);
		
		//Initialize particles
		for (int p = 0; p < config.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			double initialValue = config.function(initialPosition);
			List<Double> initialVelocity = initialVelocity();
			setParticle(p, new Particle(initialPosition, initialVelocity, 
					initialPosition, initialValue, initialValue));
			updateGlobalBest(getParticle(p));
		}
	}
	
	//Randomly select initial positions on domain
	private List<Double> initialPosition() {
		List<Double> position = Arrays.asList(new Double[config.getDimensions()]);
		for (int d = 0; d < config.getDimensions(); d++) {
			Double lowPos = config.getLowerBounds().get(d);
			Double highPos = config.getUpperBounds().get(d);
			position.set(d, lowPos+rand.nextDouble()*(highPos-lowPos));
		}
		return position;
	}

	//Randomly select initial velocities within max velocity bounds
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
	
	//If particle p has a better value than the current best, replace the current best
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
	
	//Perform the optimization algorithm by performing a list update for every iteration
	@Override
	public List<Double> optimize() {
		// Perform iterations
		for (int i = 0; i < config.getNumIterations(); i++) {
			updateParticleList(i);
		}
		return bestPosition;
	}
	
	//Same as optimize(), but delay between iterations to allow animation
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
	
	//iterate over all particles and update their current states
	private void updateParticleList(int iteration) {
		for (int p = 0; p < config.getNumParticles(); p++) {
			updateParticle(iteration, p, inertia);
		}
		updateInertia(iteration+1); // After iteration, decrease inertia
		log.addBestPosition(iteration, bestPosition); // Log overall best and current time
		log.addTime(iteration, System.nanoTime());
	}
	
	// Calculate linear decreasing inertia value for this iteration
	private void updateInertia(int iteration) {
		inertia = ((config.getInertia() - config.getMinInertia()) * (config.getNumIterations() - (iteration))) /
				config.getNumIterations() + config.getMinInertia();
	}
	
	private void updateParticle(int iteration, int particle, double inertia) {
		List<Double> velocity = calculateVelocity(particle, inertia); //calculate new velocity
		List<Double> position = calculatePosition(particle, velocity); //modify particle position
		List<Double> bestPosition = selectBestPosition(particle, position); //determine whether this position is a personal best
		
		//Place a new particle in this particle's spot
		setParticle(particle, new Particle(position, velocity, bestPosition, 
				config.function(position), config.function(bestPosition)));
		updateGlobalBest(getParticle(particle)); //Determine if this is a new record
		
		log.addParticlePosition(iteration, particle, position); //Log this particle's new state
	}
	
	//Pick the position with the best value
	private List<Double> selectBestPosition(int particle, List<Double> newPosition) {
		List<Double> currentBestPosition = getParticle(particle).getBestPosition();
		if (config.function(currentBestPosition) <= config.function(newPosition)) {
			return currentBestPosition;
		}
		else {
			return newPosition;
		}
	}
	
	//update position based on velocity
	private List<Double> calculatePosition(int particle, List<Double> velocity) {
		List<Double> position = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldPosition = getParticle(particle).getPosition();
		
		//for each dimension
		for (int d = 0; d < config.getDimensions(); d++) {
			double dPos = oldPosition.get(d) + velocity.get(d);
			position.set(d, Math.min(config.getUpperBounds().get(d), 
					Math.max(config.getLowerBounds().get(d), dPos)));
			//bounce if we hit a wall
			if (position.get(d) != dPos) {
				velocity.set(d, -velocity.get(d));
			}
		}
		
		return position;
	}

	//perform the infamous velocity update
	private List<Double> calculateVelocity(int particle, double inertia) {
		List<Double> velocity = Arrays.asList(new Double[config.getDimensions()]);
		List<Double> oldVelocity = getParticle(particle).getVelocity();
		List<Double> position = getParticle(particle).getPosition();
		List<Double> selfBestPosition = getParticle(particle).getBestPosition();
		for (int d = 0; d < config.getDimensions(); d++) {
			List<Double> fdrPosition;
			if (config.getFdrWeight() != 0.0) { 
				fdrPosition = bestFitnessDistance(particle, d); //Search for the best FDR particle
			}
			else {
				fdrPosition = selfBestPosition; //If Non-FDR PSO skip the expensive search
			}
			double dVelocity = inertia * oldVelocity.get(d) + 
					rand.nextDouble() * config.getSelfWeight() * (selfBestPosition.get(d) - position.get(d)) + 
					rand.nextDouble() * config.getBestWeight() * (bestPosition.get(d) - position.get(d)) + 
					rand.nextDouble() * config.getFdrWeight() * (fdrPosition.get(d) - position.get(d)); // Weighted sum of attractive points
			//Clamp velocity to max
			velocity.set(d, Math.signum(dVelocity) * Math.min(config.getMaximumVelocity().get(d), Math.abs(dVelocity)));
		}
		return velocity;
	}
	
	//Find the position of the particle with the best FDR
	private List<Double> bestFitnessDistance(int particle, int dimension) {
		
		List<Double> bestFDRPosition = getParticle((particle+1)%config.getNumParticles()).getBestPosition();
		double bestFDR = fdr(particle, (particle+1)%config.getNumParticles(), dimension);
		for (int q = particle+1; q < config.getNumParticles(); q++) {
			double fdr = fdr(particle, q, dimension); //Calculate fdr, compare with current max
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
	
	//Determine a single FDR
	private double fdr(int currentParticle, int foreignParticle, int dimension) {
		//Fitness is how far a particle surpasses this particle
		double fitness = -(getParticle(foreignParticle).getBestValue() - getParticle(currentParticle).getValue());
		double distance = Math.abs(
				getParticle(foreignParticle).getBestPosition().get(dimension) - 
				getParticle(currentParticle).getPosition().get(dimension));
		if (distance != 0) {
			return fitness/distance; //Perform the ratio
		}
		else {
			return Double.MAX_VALUE; //Handle divides by zero
		}
	}
}