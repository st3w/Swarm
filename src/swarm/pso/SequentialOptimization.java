package swarm.pso;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SequentialOptimization implements SwarmOptimization {
	private final SwarmConfiguration swarmConf; // SwarmConfiguration has info about the function and particle behavior
	
	private final List<Particle> particles;
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private final Random rand; // random number generation
	
	public SequentialOptimization(SwarmConfiguration swarmConfiguration) {
		this.swarmConf = swarmConfiguration;
		particles = Arrays.asList(new Particle[swarmConfiguration.getNumParticles()]);
		rand = new Random();
		
		for (int p = 0; p < swarmConfiguration.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			double initialValue = swarmConf.function(initialPosition);
			List<Double> initialVelocity = initialVelocity();
			particles.set(p, new Particle(initialPosition, initialVelocity, 
					initialPosition, initialValue, initialValue));
			updateGlobalBest(particles.get(p));
		}
	}
	
	public List<Double> optimize() {
		// Perform iterations
		for (int p = 0; p < swarmConf.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		for (int i = 0; i < 1000; i++) {
			updateParticleList();
		}
		System.out.println("-----------------------------------------------------");
		for (int p = 0; p < swarmConf.getNumParticles(); p++) {
			System.out.println("Particle " + p + ": " + particles.get(p).getValue());
		}
		return bestPosition;
	}
	
	private void updateParticleList() {
		for (int p = 0; p < swarmConf.getNumParticles(); p++) {
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
				swarmConf.function(position), swarmConf.function(bestPosition)));
		updateGlobalBest(particles.get(particle));	
	}
	
	private List<Double> selectBestPosition(int particle, List<Double> newPosition) {
		List<Double> currentBestPosition = particles.get(particle).getBestPosition();
		if (swarmConf.function(currentBestPosition) <= swarmConf.function(newPosition)) {
			return currentBestPosition;
		}
		else {
			return newPosition;
		}
	}
	
	private List<Double> calculatePosition(int particle, List<Double> velocity) {
		List<Double> position = Arrays.asList(new Double[swarmConf.getDimensions()]);
		List<Double> oldPosition = particles.get(particle).getPosition();
		
		for (int d = 0; d < swarmConf.getDimensions(); d++) {
			position.set(d, Math.min(swarmConf.getUpperBounds().get(d), 
					Math.max(swarmConf.getLowerBounds().get(d), oldPosition.get(d) + velocity.get(d))));
		}
		
		return position;
	}

	private List<Double> calculateVelocity(int particle) {
		List<Double> velocity = Arrays.asList(new Double[swarmConf.getDimensions()]);
		List<Double> oldVelocity = particles.get(particle).getVelocity();
		List<Double> position = particles.get(particle).getPosition();
		List<Double> selfBestPosition = particles.get(particle).getBestPosition();
		for (int d = 0; d < swarmConf.getDimensions(); d++) {
			List<Double> fdrPosition = bestFitnessDistance(particle, d);
			double dVelocity = swarmConf.getInertia() * oldVelocity.get(d) + 
					swarmConf.getSelfWeight() * (selfBestPosition.get(d) - position.get(d)) + 
					swarmConf.getBestWeight() * (bestPosition.get(d) - position.get(d)) + 
					swarmConf.getFdrWeight() * (fdrPosition.get(d) - position.get(d));
			velocity.set(d, Math.min(swarmConf.getMaximumVelocity().get(d),
					Math.max(-swarmConf.getMaximumVelocity().get(d), dVelocity)));
		}
		return velocity;
	}
	
	private List<Double> bestFitnessDistance(int particle, int dimension) {
		List<Double> bestFDRPosition = null;
		double bestFDR = 0;
		for (int q = 0; q < swarmConf.getNumParticles(); q++) {
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
		List<Double> position = Arrays.asList(new Double[swarmConf.getDimensions()]);
		for (int d = 0; d < swarmConf.getDimensions(); d++) {
			Double lowPos = swarmConf.getLowerBounds().get(d);
			Double highPos = swarmConf.getUpperBounds().get(d);
			position.set(d, lowPos+rand.nextDouble()*(highPos-lowPos));
		}
		return position;
	}
	
	private List<Double> initialVelocity() {
		List<Double> velocity = Arrays.asList(new Double[swarmConf.getDimensions()]);
		for (int d = 0; d < swarmConf.getDimensions(); d++) {
			//Double lowPos = lowerBounds.get(d);
			//Double highPos = upperBounds.get(d);
			//Double lowVel = lowPos - highPos;
			//Double highVel = highPos - lowPos;
			Double lowVel = -swarmConf.getMaximumVelocity().get(d);
			Double highVel = swarmConf.getMaximumVelocity().get(d);
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