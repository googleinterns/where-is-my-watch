package com.google.sharedlibrary.model;

import java.util.Objects;
import java.util.PriorityQueue;

/**
 * This class create value object of SatelliteSignalData containing the top 4 strongest satellites'
 * signal
 */
public class SatelliteSignalData {
  private final String TAG = "SatelliteSignalData";
  private final float firstSignal;
  private final float secondSignal;
  private final float thirdSignal;
  private final float forthSignal;

  /** Constructor without parameter, create a single SatelliteSignalData with default value */
  public SatelliteSignalData() {
    this.forthSignal = 0.0f;
    this.thirdSignal = 0.0f;
    this.secondSignal = 0.0f;
    this.firstSignal = 0.0f;
  }

  /**
   * Constructor with a PriorityQueue parameter, create a single SatelliteSignalData with passing
   * values
   *
   * @param pq
   */
  public SatelliteSignalData(PriorityQueue<Float> pq) {
    this.forthSignal = pq.poll();
    this.thirdSignal = pq.poll();
    this.secondSignal = pq.poll();
    this.firstSignal = pq.poll();
  }

  /**
   * Get the first strongest signal
   *
   * @return the first strongest signal
   */
  public float getFirstSignal() {
    return this.firstSignal;
  }

  /**
   * Get the second strongest signal
   *
   * @return the second strongest signal
   */
  public float getSecondSignal() {
    return this.secondSignal;
  }

  /**
   * Get the third strongest signal
   *
   * @return the third strongest signal
   */
  public float getThirdSignal() {
    return this.thirdSignal;
  }

  /**
   * Get the forth strongest signal
   *
   * @return the forth strongest signal
   */
  public float getForthSignal() {
    return this.forthSignal;
  }

  /**
   * Get the average of the top 4 strongest signal
   *
   * @return the average of the top 4 strongest signal or top 3 strongest signal if no forth signal
   */
  public float getAverageSignal() {
    return (this.firstSignal + this.secondSignal + this.thirdSignal + this.forthSignal) / 4;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    SatelliteSignalData satelliteSignalData = (SatelliteSignalData) o;

    return Float.compare(this.firstSignal, satelliteSignalData.firstSignal) == 0
        && Float.compare(this.secondSignal, satelliteSignalData.secondSignal) == 0
        && Float.compare(this.thirdSignal, satelliteSignalData.thirdSignal) == 0
        && Float.compare(this.forthSignal, satelliteSignalData.forthSignal) == 0;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
