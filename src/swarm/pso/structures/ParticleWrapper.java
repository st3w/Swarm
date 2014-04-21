package swarm.pso.structures;

public class ParticleWrapper {
	private volatile Particle particle = null;
	
	public Particle getParticle() {
		return particle;
	}
	
	public void setParticle(Particle particle) {
		this.particle = particle;
	}
}
