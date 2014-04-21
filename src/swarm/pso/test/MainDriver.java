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
import swarm.pso.service.WrapAllOptimization;
import swarm.pso.structures.config.ConcurrentSwarmConfiguration;
import swarm.pso.structures.config.FunctionConfiguration;
import swarm.pso.structures.config.SwarmConfiguration;
import swarm.pso.ui.LogPainter;

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
		// Make a function to optimize
		PSOFunction<Double> function;
		int numParticles = 0;
		int numIterations = 0;
		int animationTimeout = 0;
		int dimensions = DIMENSIONS;
		
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
		
		// Function bounds are a list of parameters
		List<Double> maximumVelocity = Arrays.asList(new Double[function.getDimensions()]);
		
		// Set up max velocity for function
		for (int i = 0; i < function.getDimensions(); i++) {
			maximumVelocity.set(i, 
					Math.abs(function.getUpperBounds().get(i)-function.getLowerBounds().get(i)));
		}
		
		FunctionConfiguration funcConf = new FunctionConfiguration(function.getDimensions(), function, 
				function.getLowerBounds(), function.getUpperBounds());
		
		SwarmConfiguration swarmConf = new SwarmConfiguration(INITIAL_INERTIA, FINAL_INERTIA, SELF_WEIGHT, BEST_WEIGHT,
				FDR_WEIGHT, numParticles, numIterations, maximumVelocity, funcConf);
		
		ConcurrentSwarmConfiguration concurrentConfig = new ConcurrentSwarmConfiguration(swarmConf, Runtime.getRuntime().availableProcessors());
		
		Random rand;
		
		if (USE_SEED) {
			rand = new Random(SEED);
		}
		else {
			rand = new Random();
		}
		
		Logging log = new Logging(concurrentConfig);
		
		if (animationTimeout > 0) {
			setupLogPainter(log, concurrentConfig);
		}
		
		WrapAllOptimization pso = new WrapAllOptimization(concurrentConfig, rand, log);
		List<Double> solution;
		
		if (animationTimeout > 0) 
			solution = pso.optimize(animationTimeout);
		else 
			solution = pso.optimize();
		
		System.out.println(solution);
		System.out.println(function.function(solution));
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
