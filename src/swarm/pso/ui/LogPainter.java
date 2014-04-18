package swarm.pso.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import swarm.pso.logging.Logging;
import swarm.pso.structures.config.SwarmConfiguration;

@SuppressWarnings("serial")
public class LogPainter extends Component {
	public static final Color COLOR = Color.RED;
	public static final int RADIUS = 2;
	public static final int WIDTH = 500;
	public static final int HEIGHT = 500;

	private final Logging log;
	private final SwarmConfiguration config;
	private final Image img;
	
	public LogPainter(Logging log, SwarmConfiguration config) {
		this.log = log;
		this.config = config;
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_USHORT_GRAY);
		Graphics g = img.getGraphics();
		double[][] values = new double[WIDTH][HEIGHT];
		for (int x = 0; x < WIDTH; x++) {
			double xpos = x * (config.getUpperBounds().get(0)-config.getLowerBounds().get(0)) / WIDTH + config.getLowerBounds().get(0);
			for (int y = 0; y < HEIGHT; y++) {
				double ypos = y * (config.getUpperBounds().get(1)-config.getLowerBounds().get(1)) / WIDTH + config.getLowerBounds().get(1);
				List<Double> position = Arrays.asList(xpos,ypos);
				values[x][y] = config.function(position);
			}
		}
		
		for (int x = 0; x < WIDTH; x++) {
			double xpos = x * (config.getUpperBounds().get(0)-config.getLowerBounds().get(0)) / WIDTH + config.getLowerBounds().get(0);
			for (int y = 0; y < HEIGHT; y++) {
				//g.setColor(Color.)
				//g.drawLine(x, y, x, y);
			}
		}
	}
	
	protected void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
		
		g.setColor(COLOR);
		if (log.getLatestIteration() >= 0) {
			for (int i = 0; i < config.getNumParticles(); i++) {
				List<Double> position = log.getParticlePosition(log.getLatestIteration(), i);
				paintPoint(g, position);
			}
		}
	}
	
	private void paintPoint(Graphics g, List<Double> point) {
		int xPos = (int)(WIDTH * (point.get(0)-config.getLowerBounds().get(0))/(config.getUpperBounds().get(0)-config.getLowerBounds().get(0)));
		int yPos = (int)(HEIGHT * (1-(point.get(1)-config.getLowerBounds().get(1))/(config.getUpperBounds().get(1)-config.getLowerBounds().get(1))));
		g.fillOval(xPos-RADIUS, yPos-RADIUS, 2*RADIUS, 2*RADIUS);
	}
}