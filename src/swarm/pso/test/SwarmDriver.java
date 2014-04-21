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
import swarm.pso.service.SequentialOptimization;
import swarm.pso.structures.config.ConcurrentSwarmConfiguration;
import swarm.pso.structures.config.FunctionConfiguration;
import swarm.pso.structures.config.SwarmConfiguration;
import swarm.pso.ui.LogPainter;

public class SwarmDriver {
	public static final int DIMENSIONS = 2;
	//private static final double[] LOWER_BOUNDS = {-10, -10};
	//private static final double[] UPPER_BOUNDS = {10, 10};
	
	//private static final double[] MAX_VELOCITY = {Math.abs(UPPER_BOUNDS[0]-LOWER_BOUNDS[0]), Math.abs(UPPER_BOUNDS[1]-LOWER_BOUNDS[1])};
	
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
		// Make a function to optimize
		PSOFunction<Double> function;
		int numParticles = 0;
		int numIterations = 0;
		int animationTimeout = 0;
		
		if (args.length < 3 && args.length > 0)
			throw new IllegalArgumentException("You must have either 3 or 4 arguments if any.");
		else if (args.length >= 3) {
			int functionNum = Integer.parseInt(args[0]);
		
			switch (functionNum) {
			case 0:
				function = new Functions.Sphere(DIMENSIONS);
				break;
			case 1:
				function = new Functions.Ackley(DIMENSIONS);
				break;
			case 2:
				function = new Functions.Rosenbrock(DIMENSIONS);
				break;
			case 3:
				function = new Functions.TableFunc(DIMENSIONS);
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
			
			if (args.length == 4)
				animationTimeout = Integer.parseInt(args[3]);
		} else {
			function = new Functions.Rosenbrock(DIMENSIONS);
			numParticles = NUMBER_PARTICLES;
			numIterations = NUMBER_ITERATIONS;
			animationTimeout = 0;
		}
		// Function bounds are a list of parameters
		
		List<Double> maximumVelocity = Arrays.asList(new Double[function.getDimensions()]);
		
		// optimize arguments within bounds
		for (int i = 0; i < function.getDimensions(); i++) {
			maximumVelocity.set(i, 
					Math.abs(function.getUpperBounds().get(i)-function.getLowerBounds().get(i)));
		}
		
		FunctionConfiguration funcConf = new FunctionConfiguration(function.getDimensions(), function, 
				function.getLowerBounds(), function.getUpperBounds());
		
		SwarmConfiguration swarmConf = new SwarmConfiguration(INITIAL_INERTIA, FINAL_INERTIA, SELF_WEIGHT, BEST_WEIGHT,
				FDR_WEIGHT,  numParticles, numIterations, maximumVelocity, funcConf);
		
		ConcurrentSwarmConfiguration concurrentConfig = new ConcurrentSwarmConfiguration(swarmConf, Runtime.getRuntime().availableProcessors()-1);
		
		Random rand1;
		Random rand2;
		long seed;
		if (USE_SEED) {
			seed = SEED;
		}
		else {
			seed = (new Random()).nextLong();
		}
		rand1 = new Random(seed);
		rand2 = new Random(seed);
		
		Logging log1 = new Logging(swarmConf);
		Logging log2 = new Logging(concurrentConfig);
		
		//setupLogPainter(log1, swarmConf);
		
		SequentialOptimization pso = new SequentialOptimization(swarmConf, rand1, log1);
		List<Double> solution;
		
		if (animationTimeout > 0) 
			solution = pso.optimize(animationTimeout);
		else 
			solution = pso.optimize();
		
		SequentialOptimization pso2 = new SequentialOptimization(swarmConf, rand2, log2);
		List<Double> solution2 = pso2.optimize();
		
		log1.writeToFile("SeqFDR");
		log2.writeToFile("ConFDR");
		System.out.println(seed);
		
		System.out.println(solution);
		System.out.println(function.function(solution));
		
		System.out.println(solution2);
		System.out.println(function.function(solution2));
	}

	private static void setupLogPainter(final Logging log, final SwarmConfiguration config) {
		final LogPainter lp = new LogPainter(log, config);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Swarm: FDR-PSO");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

				JPanel panel = new JPanel();
				
				lp.setBounds(new Rectangle(0,0,LogPainter.WIDTH,LogPainter.HEIGHT));
				panel.setBounds(new Rectangle(0,0,LogPainter.WIDTH,LogPainter.HEIGHT));
				panel.add(lp);
				frame.add(panel);
				
				frame.pack();
				frame.setVisible(true);
			}
		});
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
