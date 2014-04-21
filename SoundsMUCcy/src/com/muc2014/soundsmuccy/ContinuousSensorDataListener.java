/* **************************************************
 
 Copyright (c) Tom Hedges 2014
 
 This class file developed from an original, copyrighted by the University of Cambridge.
 This class file contains content (indicated within code) sourced from 'stackoverflow' and
 licensed under "cc by-sa 3.0" (http://creativecommons.org/licenses/by-sa/3.0/)
 Your use of this source code is limited by the University of Cambridge license below, as well as
 as 'stackoverflow's "cc by-sa 3.0" (where applicable), as well as this application's LICENSE file.
 
 ----------------------------------
 
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk

This demo application was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.muc2014.soundsmuccy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.ESSensorManagerInterface;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.config.GlobalConfig;
import com.ubhave.sensormanager.config.sensors.pull.LocationConfig;
import com.ubhave.sensormanager.config.sensors.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pullsensor.LocationData;
import com.ubhave.sensormanager.data.pullsensor.MicrophoneData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class ContinuousSensorDataListener extends AsyncTask<Void, Void, SensorData> implements SensorDataListener
{
	private final static String LOG_TAG = "SensorListener";

	private int sensorType;

	private ESSensorManagerInterface sensorManager;
	private JSONFormatter formatter;

	private int sensorSubscriptionId;
	private boolean isSubscribed;

	private Activity context;
	private long sleepPeriod;
	private Boolean started = false;
	private String sensorTypeVerbose;
	private String errorMessage;

	private String strSoundPressureLevel = null;
	private String strLatitude = null;
	private String strLongitude = null;
	
	// Constructor - establish core tools
	public ContinuousSensorDataListener(Activity context)//, Activity context) //, SensorDataUI userInterface)
	{
		isSubscribed = false;

		this.context = context;

		try
		{
			sensorManager = ESSensorManager.getSensorManager(context);
			sensorManager.setGlobalConfig(GlobalConfig.LOW_BATTERY_THRESHOLD, 25);

			if (sensorType == SensorUtils.SENSOR_TYPE_LOCATION)
			{
				sensorManager.setSensorConfig(SensorUtils.SENSOR_TYPE_LOCATION, LocationConfig.ACCURACY_TYPE, LocationConfig.LOCATION_ACCURACY_FINE);
			}
		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}

	// Configure sensors for sampling length (in milliseconds) and length of time between samples for continuous logging
	public void ConfigureSensor(int sensorType, long sampleLength, long sleepPeriod) {
		this.sensorType = sensorType;
		this.sleepPeriod = sleepPeriod;
		try {
			sensorManager.setSensorConfig(sensorType, PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, sampleLength);
			sensorManager.setSensorConfig(sensorType, PullSensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS, sleepPeriod);
		} catch (ESException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		formatter = DataFormatter.getJSONFormatter(context, sensorType);

		switch (sensorType) {

		case SensorUtils.SENSOR_TYPE_LOCATION:
			sensorTypeVerbose = "location";
			break;

		case SensorUtils.SENSOR_TYPE_MICROPHONE:
			sensorTypeVerbose = "sound";
			break;
		}

		updateInterface(false, "Sensor for " + sensorTypeVerbose + " is configured.");	
		updateInterface(false, "Sample length for " + sensorTypeVerbose + " is " + sampleLength/1000 + " seconds.");		
	}

	// Begin taing regular samples of data
	public void subscribeToSensorData()
	{
		try
		{
			sensorSubscriptionId = sensorManager.subscribeToSensorData(sensorType, this);
			isSubscribed = true;
			updateInterface(false, "You are subscribed for " + sensorTypeVerbose + " updates every " + sleepPeriod/60000 + " minute(s).");
		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}

	// Stop taking regular samples of data
	public void unsubscribeFromSensorData()
	{
		try
		{
			sensorManager.unsubscribeFromSensorData(sensorSubscriptionId);
			isSubscribed = false;
			started = false;
			updateInterface(false, "You are unsubscribed from " + sensorTypeVerbose + " updates.");
		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}

	// Send on data sampled periodically in "continuous" mode
	@Override
	public void onDataSensed(SensorData data)
	{
		started = true;
		updateInterface(false, processSensorData(data));
		checkWhetherAllDataReturned();
	}

	public String getSensorName()
	{
		try
		{
			return SensorUtils.getSensorName(sensorType);
		}
		catch (ESException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	// Getter method for subscription status
	public boolean isSubscribed()
	{
		return isSubscribed;
	}

	// Pause and unpause continuous data sampling as device crosses low-battery threshold
	@Override
	public void onCrossingLowBatteryThreshold(boolean isBelowThreshold)
	{
		Log.d(LOG_TAG, "crossingLowBatteryThreshold: "+isBelowThreshold);
		try
		{
			if (isBelowThreshold)
			{
				sensorManager.pauseSubscription(sensorSubscriptionId);
				updateInterface(true, "Sensing stopped: low battery");
			}
			else
			{
				if (started) {
					sensorManager.unPauseSubscription(sensorSubscriptionId);
					updateInterface(true, "Sensing started: battery healthy");
				}
			}
		}
		catch (ESException e)
		{
			e.printStackTrace();
		}
	}

	// Carry out action in main UI thread/Activity which requires updates to UI. This
	// is more closely coupled than it should be, but avoids unpleasant errors caused by
	// attempts to access UI from "incorrect thread".
	// The methods "checkWhetherAllDataReturned()" and "updateInterface()" derived from (but 
	// source code provided by user "Sujal Mandal" (http://stackoverflow.com/users/2835764/sujal-mandal)
	// at 'stackoverflow' (http://stackoverflow.com/a/19388540). This content licensed under Creative Commons
	// "cc by-sa 3.0" (http://creativecommons.org/licenses/by-sa/3.0/)
	private void checkWhetherAllDataReturned() {
		((SoundLevelLogging) context).runOnUiThread(new Runnable()
		{
			public void run()
			{
				((SoundLevelLogging) context).checkDataAndSendForUpload();
			}
		});
	}

	// Carry out action in main UI thread/Activity which requires updates to UI. This
	// is more closely coupled than it should be, but avoids unpleasant errors caused by
	// attempts to access UI from "incorrect thread".
	public void updateInterface(final boolean clearResults, final String data) {
		((SoundLevelLogging) context).runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (clearResults) {
					((SoundLevelLogging) context).clearDetailText();
				}
				((SoundLevelLogging) context).setDetailText(data);
			}
		});
	}

	// Carry out sampling Asynchronously, without locking out UI or other processes
	@Override
	protected SensorData doInBackground(Void... params) {
		try
		{
			Log.d("Sensor Task", "Sampling from Sensor");
			return sensorManager.getDataFromSensor(sensorType);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	// Update user that sampling is about to happen
	@Override
	public void onPreExecute()
	{
		updateInterface(false, "Starting " + sensorTypeVerbose + " check...");
	}

	// Update user that sampling has happened and send data for processing
	@Override
	public void onPostExecute(SensorData data)
	{
		if (data != null) {
			updateInterface(false, "Completed " + sensorTypeVerbose + " check... analyzing...");
			updateInterface(false, processSensorData(data));
			checkWhetherAllDataReturned();
		} else {
			updateInterface(false, "No data available - resetting for next attempt.\nError message:\n" + errorMessage);
			strSoundPressureLevel = null;
			strLatitude = null;
			strLongitude = null;
			((SoundLevelLogging) context).buttonResetCheck();
		}
	}
	
	// Getter method for detected Sound Pressure Level
	public String getSoundPressureLevel() {
		return strSoundPressureLevel;
	}

	// Getter method for detected latitude
	public String getLatitude() {
		return strLatitude;
	}

	// Getter method for detected longitude
	public String getLongitude() {
		return strLongitude;
	}

	// Setter method for detected Sound Pressure Level
	public void setSoundPressureLevel(String strSoundPressureLevel) {
		this.strSoundPressureLevel = strSoundPressureLevel;
	}

	// Setter method for detected latitude
	public void setLatitude(String strLatitude) {
		this.strLatitude = strLatitude;
	}

	// Setter method for detected longitude
	public void setLongitude(String strLongitude) {
		this.strLongitude = strLongitude;
	}

	// Parse JSON data returned by Sensor Manager, and then send for further processing if necessary.
	// Reports result to user and stores locally for transmission to Xively.
	private String processSensorData(SensorData data) {
		// Default message;
		String returnMessage = "Data not parsed correctly.";

		switch (sensorType) {
		case SensorUtils.SENSOR_TYPE_MICROPHONE:
			try {
				MicrophoneData micData = (MicrophoneData) data; // sensing result
				JSONObject json = formatter.toJSON(micData);
				String dataFull = json.toString();

				JSONObject jObject = new JSONObject(dataFull);

				String strAmplitude = jObject.getString("amplitude");
				JSONArray jsonArray = new JSONArray(strAmplitude);

				int iLoopCounter = jsonArray.length();
				int[] finalAmpArray = new int[iLoopCounter-1];
				// array always has leading 0 entry - so don't include this value in the average
				for (int i = 1; i < iLoopCounter; i++) {
					finalAmpArray[i-1] = jsonArray.getInt(i);
				}

				SoundPressureLevelCalculator splCalc = new SoundPressureLevelCalculator(finalAmpArray);

				strSoundPressureLevel = Integer.toString(splCalc.getSPL());
				
				returnMessage = "Sound Pressure Level = " + strSoundPressureLevel + "dB.";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case SensorUtils.SENSOR_TYPE_LOCATION:
			try {
				LocationData locData = (LocationData) data; // sensing result
				JSONObject json = formatter.toJSON(locData);

				String dataFull = json.toString();
				JSONObject jObject = new JSONObject(dataFull);

				strLatitude = jObject.getString("latitude");
				strLongitude = jObject.getString("longitude");

				if (strLatitude.equals("0")) {
					strLatitude = "Unknown";
				}

				if (strLongitude.equals("0")) {
					strLongitude = "Unknown";
				}

				returnMessage = "Latitude = " + strLatitude + "\nLongitude = " + strLongitude;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		}

		return returnMessage;
	}
}
