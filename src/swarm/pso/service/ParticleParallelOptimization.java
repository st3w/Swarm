package swarm.pso.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import swarm.pso.logging.Logging;
import swarm.pso.structures.Particle;
import swarm.pso.structures.ParticleWrapper;
import swarm.pso.structures.config.ConcurrentSwarmConfiguration;

//This class represents our latest and greatest implementation of concurrent FDR PSO
public class ParticleParallelOptimization implements SwarmOptimization {
	private final ConcurrentSwarmConfiguration config; // config has info about the function and particle behavior
	
	private final List<ParticleWrapper> particles; // The structures containing each particle's state information
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private final CyclicBarrier barrier; // Threads wait on barrier before continuing with next iteration.
											// This prevents one thread from getting too far ahead
	
	private final Random rand; // RNG
	
	private final Logging log; // Stores some state information
	
	public ParticleParallelOptimization(ConcurrentSwarmConfiguration config, Logging log) {
		this(config, new Random(), log);
	}
	
	public ParticleParallelOptimization(ConcurrentSwarmConfiguration config, Random rand, Logging log) {
		this.config = config;
		this.rand = rand;
		this.log = log;
		
		barrier = new CyclicBarrier(ParticleParallelOptimization.this.config.getNumThreads(), new Runnable() {
			private int iteration = 0;
			
			// Last particle out will log state at time of completion
			@Override
			public void run() {
	    		ParticleParallelOptimization.this.log.addBestPosition(iteration, bestPosition);
	    		ParticleParallelOptimization.this.log.addTime(iteration++, System.nanoTime());
			}
			
		});
		
		log.setStartTime();
		
		particles = Arrays.asList(new ParticleWrapper[config.getNumParticles()]);

		//Initialize particles
		for (int p = 0; p < config.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			
			double initialValue = config.function(initialPosition);
			
			List<Double> initialVelocity = initialVelocity();
			
			Particle particle = new Particle(initialPosition, initialVelocity, initialPosition, initialValue, initialValue);
						
			setParticle(p, particle);
		
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
			Double lowVel = -config.getMaximumVelocity().get(d);
			Double highVel = config.getMaximumVelocity().get(d);
			velocity.set(d, lowVel+rand.nextDouble()*(highVel-lowVel));
		}
		return velocity;
	}
	
	private synchronized void updateGlobalBest(Particle p) {
		if (bestPosition == null || p.getValue() < bestValue) {
			bestPosition = p.getPosition();
			bestValue = p.getValue();
		}
	}

	private Particle getParticle(int index) {
		ParticleWrapper pw = particles.get(index);
		if (pw == null) {
			return null;
		}
		else {
			return pw.getParticle();
		}
	}
	
	private void setParticle(int index, Particle particle) {
		ParticleWrapper pw = particles.get(index);
		if (pw == null) {
			pw = new ParticleWrapper();
			particles.set(index, pw);
		}
		pw.setParticle(particle);
	}
	
	// performs the optimization algorithm
	@Override
	public List<Double> optimize() {
		// Perform iterations
		startParticleList(0);
		return bestPosition;
	}
	
	// performs optimization with a delay for animation
	public List<Double> optimize(int timeout) {
		// Perform iterations
		startParticleList(timeout);
		return bestPosition;
	}

	// Starts threads to handle all particle updates
	private void startParticleList(final int delay) {
		ExecutorService es = Executors.newCachedThreadPool();
        final int particlesPerThread = config.getNumParticles() / config.getNumThreads(); // Each thread handles at least this many particles
        final int remainder = config.getNumParticles() % config.getNumThreads(); // This many threads get 1 extra

        for (int t = 0; t < config.getNumThreads(); t++) {
        	final int thread = t;
            es.execute(new Runnable() {
                public void run() {
                	double inertia = config.getInertia();
                	for (int iteration = 0; iteration < config.getNumIterations(); iteration++) { //Thread performs all iterations
				        for (int p = 0; p < particlesPerThread; p++) { //For each particle in group
				            int particleNumber = thread * particlesPerThread + p;
		                    updateParticle(iteration, particleNumber, inertia);
		                }
				        if (thread < remainder) { //If this thread has a leftover particle
				        	updateParticle(iteration, particlesPerThread*config.getNumThreads() + thread, inertia);
				        }
				        inertia = updateInertia(iteration+1);
				        
				        if (delay > 0) { //if delay is positive, sleep to allow animation
					        try {
					        	Thread.sleep(delay);
							} catch (InterruptedException e) {
								
							}
				        }
				        
				        try {
							barrier.await(); // Synchronize after performing all updates in an iteration
						} catch (InterruptedException e) {

						} catch (BrokenBarrierException e) {

						}
                	}
				}
            });
        }
        es.shutdown(); // Terminate and join
        try {
			if(!es.awaitTermination(100*(1+delay)*config.getNumParticles()^2,TimeUnit.MILLISECONDS)) {
				System.err.println("Optimization failed to complete in " + (10*config.getNumIterations()*config.getNumParticles()^2) + " ms.");
			}
		} catch (InterruptedException e) {
			
		}
	}
	
	private double updateInertia(int iteration) { //Calculate new inertia
		return ((config.getInertia() - config.getMinInertia()) * (config.getNumIterations() - (iteration))) /
				config.getNumIterations() + config.getMinInertia();
	}
	
	//Remaining methods work similarly to SequentialOptimization
	private void updateParticle(int iteration, int particle, double inertia) {
		List<Double> velocity = calculateVelocity(particle, inertia);
		List<Double> position = calculatePosition(particle, velocity);
		List<Double> bestPosition = selectBestPosition(particle, position);
		
		//System.out.println("Particle: " + position + ", " + velocity + ", " + function.function(position));
		setParticle(particle, new Particle(position, velocity, bestPosition, config.function(position), config.function(bestPosition)));
		updateGlobalBest(getParticle(particle)); //Method is now synchronized

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
		List<Double> bestFDRPosition = getParticle((particle+1)%config.getNumParticles()).getBestPosition();
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