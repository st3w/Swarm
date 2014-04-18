package swarm.pso.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logging {
    List<List<Double>> particlePositions;
        // 2D array represented in 1D, iteration x particle. ex. to get particle 4 for 2nd iteration, 2*particle count + 4
    List<List<Double>> bestPositions;
    List<Long> times;
    long startTime;

    int particleCount;
    int iterationCount;
    int latestIteration;
    public Logging(int particleCount, int iterationCount) {
        particlePositions = new ArrayList<List<Double>>(particleCount * iterationCount);
        bestPositions = new ArrayList<List<Double>>(iterationCount);
        times = new ArrayList<Long>(iterationCount);
        startTime = System.currentTimeMillis();

        this.particleCount = particleCount;
        this.iterationCount = iterationCount;
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
        if(iteration > latestIteration) latestIteration = iteration;
    }
    public void setStartTime() {
        startTime = System.currentTimeMillis();
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
        return latestIteration;
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
            for(int i = 1; i <= times.size(); i++) {
                bw.write(String.format("%d\n", times.get(i)));
            }
            bw.close();
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void writeBestPositionsToFile() {
        File file = new File("bestposition.txt");
        try {
            if (!file.exists()) file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 1; i <= bestPositions.size(); i++) {
                //bw.write(String.format("%d,", i));
                for(int currentDimension = 0; currentDimension < bestPositions.get(i).size(); currentDimension++) {
                    bw.write(String.format("%f", bestPositions.get(i).get(currentDimension)));
                    if(i < bestPositions.size()) {
                        bw.write(",");
                    }
                    else {
                        bw.write("\n");
                    }
                }
            }
            bw.close();
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void writeParticlePositionsToFile() {
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
    }
}
