package app_interface;

import java.util.Arrays;

/**
 * The {@code TimeMeasurement} class provides functionality to measure, store,
 * and analyze time durations. It tracks the last {@code N} measurements and 
 * computes statistics such as the mean, standard deviation, maximum, and minimum 
 * of the recorded measurements.
 */
public class TimeMeasurement {
    private int N;
    private long startTime;
    private double[] measurements;
    private double lastMeasurement;
    private int currentIndex;
    private int count; // To track how many measurements have been added

    /**
     * Constructs a {@code TimeMeasurement} object that tracks the last {@code N} measurements.
     *
     * @param N the number of most recent measurements to store and analyze
     */
    public TimeMeasurement(int N) {
        this.N = N;
        this.measurements = new double[N];
        this.currentIndex = 0;
        this.count = 0;
    }

    /**
     * Returns the number of measurements that can be stored.
     *
     * @return the capacity for storing measurements
     */
    public int getN() {
        return N;
    }

    /**
     * Starts a new time measurement by recording the current time.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * Stops the current time measurement and stores the elapsed time 
     * in milliseconds.
     */
    public void stop() {
        lastMeasurement = (System.nanoTime() - startTime) / 1_000_000.0;
        measurements[currentIndex] = lastMeasurement;
        currentIndex = (currentIndex + 1) % N; // Move in a circular manner
        count = Math.min(count + 1, N); // Ensure count doesn't exceed N
    }

    /**
     * Returns the time elapsed in milliseconds since the last {@code start()} call.
     *
     * @return the time elapsed in milliseconds since the start
     */
    public double getTimeFromStart() {
        return (System.nanoTime() - startTime) / 1_000_000.0;
    }

    /**
     * Returns the last recorded time measurement in milliseconds.
     *
     * @return the last recorded time measurement
     */
    public double getLastMeasurement() {
        return lastMeasurement;
    }

    /**
     * Calculates and returns the mean (average) of the last {@code N} time measurements.
     *
     * @return the mean of the last {@code N} time measurements
     */
    public double getMeanOfLastN() {
        return calculateMean();
    }

    /**
     * Calculates and returns the standard deviation of the last {@code N} time measurements.
     *
     * @return the standard deviation of the last {@code N} time measurements
     */
    public double getStdOfLastN() {
        return calculateStd(getMeanOfLastN());
    }

    /**
     * Returns the maximum value among the last {@code N} time measurements.
     *
     * @return the maximum of the last {@code N} time measurements, or 0 if there are no measurements
     */
    public double getMaxOfLastN() {
        return count > 0 ? Arrays.stream(getLastNMeasurements()).max().orElse(0) : 0;
    }

    /**
     * Returns the minimum value among the last {@code N} time measurements.
     *
     * @return the minimum of the last {@code N} time measurements, or 0 if there are no measurements
     */
    public double getMinOfLastN() {
        return count > 0 ? Arrays.stream(getLastNMeasurements()).min().orElse(0) : 0;
    }

    /**
     * Returns a string representation of the time measurements, including 
     * the mean, standard deviation, maximum, and minimum of the last {@code N} measurements.
     *
     * @return a formatted string showing statistical data for the last {@code N} measurements
     */
    @Override
    public String toString() {
        return String.format(
            "Last %d Measurements:\n" +
            "Mean: %.3f ms\n" +
            "Std: %.3f ms\n" +
            "Max: %.3f ms\n" +
            "Min: %.3f ms\n",
            count, getMeanOfLastN(), getStdOfLastN(), getMaxOfLastN(), getMinOfLastN()
        );
    }

    private double[] getLastNMeasurements() {
        double[] lastNMeasurements = new double[count];
        for (int i = 0; i < count; i++) {
            int index = (currentIndex - count + i + N) % N; // Circular indexing
            lastNMeasurements[i] = measurements[index];
        }
        return lastNMeasurements;
    }

    private double calculateMean() {
        if (count == 0) return 0;
        double sum = 0;
        for (double value : getLastNMeasurements()) {
            sum += value;
        }
        return sum / count;
    }

    private double calculateStd(double mean) {
        if (count == 0) return 0;
        double sum = 0;
        for (double value : getLastNMeasurements()) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / count);
    }

    public static void main(String[] args) {
        TimeMeasurement tm = new TimeMeasurement(5);
        tm.start();
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        tm.stop();

        tm.start();
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
        tm.stop();

        tm.start();
        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
        tm.stop();

        tm.start();
        try { Thread.sleep(400); } catch (InterruptedException e) { e.printStackTrace(); }
        tm.stop();

        tm.start();
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        tm.stop();

        System.out.println(tm);
    }
}
