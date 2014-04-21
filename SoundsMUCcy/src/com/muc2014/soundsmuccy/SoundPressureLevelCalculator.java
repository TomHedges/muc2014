/* **************************************************
 
 Copyright (c) Tom Hedges 2014
 This class file contains content derived from Wikipedia.
 Your use of this source code is limited by the license details below, as well as this application's LICENSE file

 "SoundPressureLevelCalculator" class:
 Calculates sound pressure level in decibels and returns value

 ************************************************** */

package com.muc2014.soundsmuccy;

public class SoundPressureLevelCalculator {
	private int[] amplitudeArray;
	private int soundPressureLevel;

	public SoundPressureLevelCalculator(int[] amplitudeArray) {
		this.amplitudeArray = amplitudeArray;
		calculateSPL();
	}

	private void calculateSPL() {
		// Sound Pressure Level calculation supplied by Wikipedia under the Creative Commons Attribution-ShareAlike License (http://creativecommons.org/licenses/by-sa/3.0/)
		// Taken from: http://en.wikipedia.org/wiki/Sound_pressure
		double measurementPressure;
		final int referencePressure = 20; //  commonly used reference sound pressure in air is 20 µPa (rms)
		long totalSquared = 0;
		long averageSquared = 0;
		for (int amplitude : amplitudeArray) {
			totalSquared = totalSquared + (amplitude * amplitude);
		}
		averageSquared = totalSquared/amplitudeArray.length;
		measurementPressure = Math.sqrt(averageSquared);
		soundPressureLevel = (int) (20 * Math.log10(measurementPressure/referencePressure));
	}

	public int getSPL() {
		return soundPressureLevel;
	}
}