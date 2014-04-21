package swarm.pso.test;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import swarm.pso.logging.Logging;
import swarm.pso.model.PSOFunction;
import swarm.pso.service.ParticleParallelOptimization;
import swarm.pso.structures.config.ConcurrentSwarmConfiguration;
import swarm.pso.structures.config.FunctionConfiguration;
import swarm.pso.structures.config.SwarmConfiguration;
import swarm.pso.ui.LogPainter;

//MainDriver allows the user to specify various parameters for running concurrent optimization, with or without animation
public class MainDriver {
	public static final int DIMENSIONS = 2;
	
	public static final double INITIAL_INERTIA = 0.9;
	public static final double FINAL_INERTIA = 0.4;
	public static final double SELF_WEIGHT = 1;
	public static final double BEST_WEIGHT = 1;
	public static final double FDR_WEIGHT = 2;
	
	public static final int NUMBER_PARTICLES = 10;
	public static final int NUMBER_ITERATIONS = 1000;
	
	public static final long SEED = 7100555322108534535L;
	public static final boolean USE_SEED = false;
	
	public static void main(String[] args) throws IllegalArgumentException {
		PSOFunction<Double> function; //Holds the target optimization function
		int numParticles = 0;
		int numIterations = 0;
		int animationTimeout = 0; //Specifies the number of milliseconds in between PSO updates, for animation. 0 for no animation.
		int dimensions = DIMENSIONS; //Modifies the number of dimensions for multidimensional functions
		
		if (args.length < 3 && args.length > 0)
			throw new IllegalArgumentException("You must have 3 to 5 arguments if any.");
		else if (args.length >= 3) {
			if (args.length >= 5)
				dimensions = Integer.parseInt(args[4]);
			if (dimensions <= 1) 
				throw new IllegalArgumentException("Number of dimensions must be > 1");
			
			
			int functionNum = Integer.parseInt(args[0]);
		
			switch (functionNum) {
			case 0:
				function = new Functions.Sphere(dimensions);
				break;
			case 1:
				function = new Functions.Ackley(dimensions);
				break;
			case 2:
				function = new Functions.Rosenbrock(dimensions);
				break;
			case 3:
				function = new Functions.TableFunc();
				break;
			default:
				throw new IllegalArgumentException("Function number must be between 0 and 2");
			}
			
			numParticles = Integer.parseInt(args[1]);
			if (numParticles <= 0) 
				throw new IllegalArgumentException("Number of particles must be > 0");
			
			numIterations = Integer.parseInt(args[2]);
			if (numIterations <= 0)
				throw new IllegalArgumentException("Number of iterations must be > 0");
			
			if (args.length >= 4)
				animationTimeout = Integer.parseInt(args[3]);
		} else {
			function = new Functions.Rosenbrock(dimensions);
			numParticles = NUMBER_PARTICLES;
			numIterations = NUMBER_ITERATIONS;
			animationTimeout = 0;
		}
		
		List<Double> maximumVelocity = Arrays.asList(new Double[function.getDimensions()]);
		
		// Set up max velocity for function as the max distance across the domain
		for (int i = 0; i < function.getDimensions(); i++) {
			maximumVelocity.set(i, 
					Math.abs(function.getUpperBounds().get(i)-function.getLowerBounds().get(i)));
		}
		
		// Set up configurations related to the Optimization problem
		FunctionConfiguration funcConf = new FunctionConfiguration(function.getDimensions(), function, 
				function.getLowerBounds(), function.getUpperBounds());
		
		// Set up configurations related to the PSO algorithm
		SwarmConfiguration swarmConf = new SwarmConfiguration(INITIAL_INERTIA, FINAL_INERTIA, SELF_WEIGHT, BEST_WEIGHT,
				FDR_WEIGHT, numParticles, numIterations, maximumVelocity, funcConf);
		
		// Set up the configurations for concurrent PSO
		ConcurrentSwarmConfiguration concurrentConfig = new ConcurrentSwarmConfiguration(swarmConf, Runtime.getRuntime().availableProcessors());
		
		Random rand;
		
		if (USE_SEED) {
			// allow pre-specified seeds (useful for debugging)
			rand = new Random(SEED);
		}
		else {
			rand = new Random();
		}
		
		// set up the logging for writing to file and for animation
		Logging log = new Logging(concurrentConfig);
		
		// if update delay is larger than 0, bring up the animation window
		if (animationTimeout > 0) {
			setupLogPainter(log, concurrentConfig);
		}
		
		//initialize the optimization class with our configurations
		ParticleParallelOptimization pso = new ParticleParallelOptimization(concurrentConfig, rand, log);
		List<Double> solution;
		
		if (animationTimeout > 0) 
			//find the solution, using a delay so it can be animated
			solution = pso.optimize(animationTimeout);
		else 
			//find the solution as quickly as possible
			solution = pso.optimize();

		//write best positions, errors, and times to mainResults_bestposition.txt, mainResults_errorvals.txt,
		//and mainResults_times.txt
		log.writeToFile("mainResults_");
		
		//print solution array to screen, followed by the minimum value
		System.out.println(solution);
		System.out.println(function.function(solution));
	}
	
	private static void setupLogPainter(final Logging log, final SwarmConfiguration config) {
		//initialize a component to paint the function and the current particle positions
		final LogPainter lp = new LogPainter(log, config);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Swarm: FDR-PSO");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
				JPanel panel = new JPanel();
				
				lp.setBounds(new Rectangle(0,0,LogPainter.WIDTH,LogPainter.HEIGHT));
				panel.setBounds(new Rectangle(0,0,LogPainter.WIDTH,LogPainter.HEIGHT));
				panel.add(lp);
				frame.add(panel);
				
				frame.pack();
				frame.setVisible(true);
			}
		});
		
		//Create a timer to refresh the panel every 30 ms
		final Timer timer = new Timer(30, null);
		timer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lp.repaint();
				if (!lp.getParent().isVisible()) {
					timer.stop();
				}
			}
		});
		timer.start();
	}
}
