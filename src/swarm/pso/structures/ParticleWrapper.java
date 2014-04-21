package swarm.pso.structures;

public class ParticleWrapper {
	private volatile Particle particle = null; //Simply contains a volatile reference to a particle, to stop data races
	
	public Particle getParticle() {
		return particle;
	}
	
	public void setParticle(Particle particle) {
		this.particle = particle;
	}
}
