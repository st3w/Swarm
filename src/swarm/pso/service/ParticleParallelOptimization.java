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

public class ParticleParallelOptimization implements SwarmOptimization {
	private final ConcurrentSwarmConfiguration config;
	
	private final List<ParticleWrapper> particles;
	
	private List<Double> bestPosition = null; // the parameters that give the best known value
	private double bestValue; // the best known value of the function
	
	private final CyclicBarrier barrier;
	
	private final Random rand;
	
	private final Logging log;
	
	public ParticleParallelOptimization(ConcurrentSwarmConfiguration config, Logging log) {
		this(config, new Random(), log);
	}
	
	public ParticleParallelOptimization(ConcurrentSwarmConfiguration config, Random rand, Logging log) {
		this.config = config;
		this.rand = rand;
		this.log = log;
		
		barrier = new CyclicBarrier(ParticleParallelOptimization.this.config.getNumThreads(), new Runnable() {
			private int iteration = 0;
			
			@Override
			public void run() {
	    		ParticleParallelOptimization.this.log.addBestPosition(iteration, bestPosition);
	    		ParticleParallelOptimization.this.log.addTime(iteration++, System.nanoTime());
			}
			
		});
		
		log.setStartTime();
		
		particles = Arrays.asList(new ParticleWrapper[config.getNumParticles()]);

		for (int p = 0; p < config.getNumParticles(); p++) {
			List<Double> initialPosition = initialPosition();
			
			double initialValue = config.function(initialPosition);
			
			List<Double> initialVelocity = initialVelocity();
			
			Particle particle = new Particle(initialPosition, initialVelocity, initialPosition, initialValue, initialValue);
						
			setParticle(p, particle);
		
			updateGlobalBest(getParticle(p));
		}
	}

    //synchronized public int getIteration() { return iteration; }
    //synchronized public int getParticleNumber() { return particleNumber; }
	//synchronized public void incrementParticleNumber() { particleNumber += 1 % config.getNumParticles(); }

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
	
	private void updateGlobalBest(Particle p) {
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
	
	@Override
	public List<Double> optimize() {
		// Perform iterations
		startParticleList();
		return bestPosition;
	}
	
	public List<Double> optimize(int timeout) {
		// Perform iterations
		
		startParticleList();
//			try {
//				Thread.sleep(timeout);
//			} catch (InterruptedException e) {
//				
//			}
		return bestPosition;
	}

	private void startParticleList() {
        ExecutorService es = Executors.newCachedThreadPool();
        final int particlesPerThread = config.getNumParticles() / config.getNumThreads();
        final int remainder = config.getNumParticles() % config.getNumThreads();

        for (int t = 0; t < config.getNumThreads(); t++) {
        	final int thread = t;
            es.execute(new Runnable() {
                public void run() {
                	double inertia = config.getInertia();
                	for (int iteration = 0; iteration < config.getNumIterations(); iteration++) {
				        for (int p = 0; p < particlesPerThread; p++) {
				            int particleNumber = thread * particlesPerThread + p;
		                    updateParticle(iteration, particleNumber, inertia);
		                }
				        if (thread < remainder) {
				        	updateParticle(iteration, particlesPerThread*config.getNumThreads() + thread, inertia);
				        }
				        inertia = updateInertia(iteration+1);
				        try {
							barrier.await();
						} catch (InterruptedException e) {

						} catch (BrokenBarrierException e) {

						}
                	}
				}
            });
        }
        es.shutdown();
        try {
			if(!es.awaitTermination(100*config.getNumParticles()^2,TimeUnit.MILLISECONDS)) {
				System.err.println("Optimization failed to complete in " + (10*config.getNumIterations()*config.getNumParticles()^2) + " ms.");
			}
		} catch (InterruptedException e) {
			
		}
	}
	
	private double updateInertia(int iteration) {
		return ((config.getInertia() - config.getMinInertia()) * (config.getNumIterations() - (iteration))) /
				config.getNumIterations() + config.getMinInertia();
	}
	
	private void updateParticle(int iteration, int particle, double inertia) {
		List<Double> velocity = calculateVelocity(particle, inertia);
		List<Double> position = calculatePosition(particle, velocity);
		List<Double> bestPosition = selectBestPosition(particle, position);
		
		//System.out.println("Particle: " + position + ", " + velocity + ", " + function.function(position));
		setParticle(particle, new Particle(position, velocity, bestPosition, config.function(position), config.function(bestPosition)));
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