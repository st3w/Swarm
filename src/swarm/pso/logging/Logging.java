package swarm.pso.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import swarm.pso.structures.config.FunctionConfiguration;
import swarm.pso.structures.config.SwarmConfiguration;

public class Logging {
    List<List<Double>> particlePositions;
        // 2D array represented in 1D, iteration x particle. ex. to get particle 4 for 2nd iteration, 2*particle count + 4
    List<List<Double>> bestPositions;
    List<Long> times;
    long startTime;

    int particleCount;
    int iterationCount;
    
    private final FunctionConfiguration config;
    
    Object iterationGuard = new Object();
    int latestIteration;
    public Logging(SwarmConfiguration config) {
    	this.config = config;
        this.particleCount = config.getNumParticles();
        this.iterationCount = config.getNumIterations();
    	
        particlePositions = new ArrayList<List<Double>>(particleCount * iterationCount);
        for (int i = 0; i < particleCount * iterationCount; i++) {
        	particlePositions.add(null);
        }
        bestPositions = new ArrayList<List<Double>>(iterationCount);
        for (int i = 0; i < iterationCount; i++) {
        	bestPositions.add(null);
        }
        times = new ArrayList<Long>(iterationCount);
        for (int i = 0; i < iterationCount; i++) {
        	times.add(null);
        }
        startTime = System.nanoTime();

        latestIteration = -1;
    }

    public void addParticlePosition(int iteration, int particleNumber, List<Double> bestPosition) {
        particlePositions.set(iteration * particleCount + particleNumber, bestPosition);
    }
    public void addBestPosition(int iteration, List<Double> bestPosition) {
        bestPositions.set(iteration, bestPosition);
    }
    public void addTime(int iteration, long time) {
        times.set(iteration, time - startTime);
    	synchronized(iterationGuard) {
    		if(iteration > latestIteration) latestIteration = iteration;
    	}
    }
    public void setStartTime() {
        startTime = System.nanoTime();
    }

    public List<Double> getParticlePosition(int iteration, int particleNumber) {
        return particlePositions.get(iteration * particleCount + particleNumber);
    }

    public List<Double> getBestPosition(int iteration) {
        return bestPositions.get(iteration);
    }

    public long getTime(int iteration) {
        return times.get(iteration);
    }

    public int getLatestIteration() {
    	synchronized(iterationGuard) {
    		return latestIteration;
    	}
    }

    public void writeToFile() {
        writeBestPositionsToFile();
        writeTimesToFile();
    }

    private void writeTimesToFile() {
        File file = new File("times.txt");
        try {
            if (!file.exists()) file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 0; i < times.size(); i++) {
                bw.write(String.format("%d\n", times.get(i)));
            }
            bw.close();
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void writeBestPositionsToFile() {
        File file = new File("bestposition.txt");
        File file2 = new File("errorvals.txt");
        try {
            if (!file.exists()) file.createNewFile();
            if (!file2.exists()) file2.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
            FileWriter fw2 = new FileWriter(file2.getAbsoluteFile());
            BufferedWriter bw2 = new BufferedWriter(fw2);
            
            for(int i = 0; i < bestPositions.size(); i++) {
                //bw.write(String.format("%d,", i));
                bw2.write(Double.toString(config.function(bestPositions.get(i))-config.getSolution()));
                for(int currentDimension = 0; currentDimension < bestPositions.get(i).size(); currentDimension++) {
                    bw.write(Double.toString(bestPositions.get(i).get(currentDimension)));
                    if(currentDimension < bestPositions.get(i).size() - 1) {
                        bw.write(",");
                    } 
                }
                
                if (i < bestPositions.size() - 1) {
                    bw.write("\n");
                    bw2.write("\n");
                }
            }
            bw.close();
            bw2.close();
        } catch(IOException e) { e.printStackTrace(); }
    }

    /*private void writeParticlePositionsToFile() {
        File file = new File("particles.txt");
        try {
            if (!file.exists()) file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 1; i <= particlePositions.size(); i++) {
                bw.write(String.format("%d\t%s\n", i, particlePositions.get(i).toString()));
            }
            bw.close();
        } catch(IOException e) { e.printStackTrace(); }
    }*/
}
