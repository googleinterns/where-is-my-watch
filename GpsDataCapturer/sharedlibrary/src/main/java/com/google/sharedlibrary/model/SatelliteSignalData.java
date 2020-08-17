package com.google.sharedlibrary.model;

import java.util.PriorityQueue;

/**
 * The data class wrapping the top 4 strongest satellites signal.
 */
public class SatelliteSignalData {
    private float firstSignal;
    private float secondSignal;
    private float thirdSignal;
    private float forthSignal;

    /**
     * Set the four signal data
     */
    public void setSignalData(PriorityQueue<Float> pq){
        if(pq.size() == 4) {
            this.forthSignal = pq.poll();
            this.thirdSignal = pq.poll();
            this.secondSignal = pq.poll();
            this.firstSignal = pq.poll();
        }
    }

    /**
     * Get the first strongest signal
     * @return the first strongest signal
     */
    public float getFirstSignal(){
        return this.firstSignal;
    }

    /**
     * Get the second strongest signal
     * @return the second strongest signal
     */
    public float getSecondSignal(){
        return this.secondSignal;
    }

    /**
     * Get the third strongest signal
     * @return the third strongest signal
     */
    public float getThirdSignal(){
        return  this.thirdSignal;
    }

    /**
     * Get the forth strongest signal
     * @return the forth strongest signal
     */
    public float getForthSignal(){
        return this.forthSignal;
    }

    /**
     * Get the average of the top 4 strongest signal
     * @return the average of the top 4 strongest signal or top 3 strongest signal if no forth signal
     */
    public float getAverageSignal(){
        return (this.firstSignal + this.secondSignal + this.thirdSignal + this.forthSignal) / 4;
    }

    /**
     * Reset the signalData
     */
    public void resetSignalData(){
        this.firstSignal = 0.0f;
        this.secondSignal = 0.0f;
        this.thirdSignal = 0.0f;
        this.forthSignal = 0.0f;
    }

}
