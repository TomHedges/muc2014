/* **************************************************
 
 Copyright (c) Tom Hedges 2014
 
 This class file contains content (indicated within code) sourced from the Android Open Source Project
 under a Creative Commons 2.5 Attribution License (http://creativecommons.org/licenses/by/2.5/),
 and from Chintan Khetiya (without license).
 Your use of this software and its source code is limited by the Android Open Source Project license below, as well as this application's LICENSE file.
 
 ----------------------------------
 
 Full Android Open Source Project  Creative Commons 2.5 Attribution License can be found at http://creativecommons.org/licenses/by/2.5/legalcode
 
 ************************************************** */

 
package com.muc2014.soundsmuccy;

import com.ubhave.sensormanager.sensors.SensorUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SoundLevelLogging extends Activity implements OnClickListener {

	private static final int sensorTypeMic = SensorUtils.SENSOR_TYPE_MICROPHONE;
	private static final int sensorTypeLoc = SensorUtils.SENSOR_TYPE_LOCATION;
	private static final int iMinSamplingTime = 1; //secs
	private static final int iMaxSamplingTime = 120; //secs
	private static final int iMinSleepTime = 1; //mins
	private static final int iMaxSleepTime = 60; //mins
	private long lMicMeasurementLength;
	private long lLocMeasurementLength;
	private long lPauseLength;
	private EditText txtLocTime;
	private EditText txtSPLTime;
	private EditText txtFreqTime;
	private TextView txtResults;
	private Button btnLogOnce;
	private Button btnContStart;
	private Button btnContStop;
	private UploadData communicationDAO;
	private ContinuousSensorDataListener csdlMic;
	private ContinuousSensorDataListener csdlLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			Log.d(this.getLocalClassName(), "onCreated called");
		}
		setContentView(R.layout.sound_level_logging);
		txtLocTime = (EditText) findViewById(R.id.sll_loc_value);
		txtSPLTime = (EditText) findViewById(R.id.sll_spl_value);
		txtFreqTime = (EditText) findViewById(R.id.sll_freq_value);
		txtResults = (TextView) findViewById(R.id.sll_results_value);
		btnLogOnce = (Button) findViewById(R.id.soundlevel_button_logonce);
		btnLogOnce.setOnClickListener(this);
		btnContStart = (Button) findViewById(R.id.soundlevel_button_start_cont);
		btnContStart.setOnClickListener(this);
		btnContStop = (Button) findViewById(R.id.soundlevel_button_stop_cont);
		btnContStop.setOnClickListener(this);
	}
	
	// Handler for clicking of three buttons in UI
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.soundlevel_button_logonce:
			txtResults.setText("");
			if (checkInternetConnection()) {
				if (Is_Valid_Number_Validation(iMinSamplingTime, iMaxSamplingTime, txtLocTime) && Is_Valid_Number_Validation(iMinSamplingTime, iMaxSamplingTime, txtSPLTime)) {
					btnLogOnce.setEnabled(false);
					btnContStart.setEnabled(false);
					btnContStop.setEnabled(false);
					updateSettings();
					refreshAsyncElements();
					csdlMic.ConfigureSensor(sensorTypeMic, lMicMeasurementLength, lPauseLength);
					csdlLoc.ConfigureSensor(sensorTypeLoc, lLocMeasurementLength, lPauseLength);
					csdlMic.execute();
					csdlLoc.execute();
				}	
			} else {
				setDetailText("No internet connection available - cannot start logging!");
			}
			break;

		case R.id.soundlevel_button_start_cont:
			txtResults.setText("");
			if (checkInternetConnection()) {
				if (Is_Valid_Number_Validation(iMinSleepTime, iMaxSleepTime, txtFreqTime) && Is_Valid_Number_Validation(iMinSamplingTime, iMaxSamplingTime, txtLocTime) && Is_Valid_Number_Validation(iMinSamplingTime, iMaxSamplingTime, txtSPLTime)) {
					txtResults.setText("");				
					btnContStart.setEnabled(false);
					btnLogOnce.setEnabled(false);
					btnContStop.setEnabled(true);
					updateSettings();
					refreshAsyncElements();
					csdlMic.ConfigureSensor(sensorTypeMic, lMicMeasurementLength, lPauseLength);
					csdlLoc.ConfigureSensor(sensorTypeLoc, lLocMeasurementLength, lPauseLength);
					csdlMic.subscribeToSensorData();
					csdlLoc.subscribeToSensorData();
					btnContStop.setEnabled(true);
				}
			} else {
				setDetailText("No internet connection available - cannot start logging!");
			}
			break;

		case R.id.soundlevel_button_stop_cont:
			switchOffAllTools();
			btnLogOnce.setEnabled(true);
			btnContStop.setEnabled(false);
			btnContStart.setEnabled(true);
			break;
		}
	}

	// Check internet connection is available for results upload
	// Method "checkInternetConnection()" is a modification based on work created and shared by the
	// Android Open Source Project and used according to terms described in the Creative Commons 2.5 Attribution License. 
	// Code taken from: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
	public boolean checkInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}

	// Control singleton factories (of sorts) to generate new Async tasks for each logging session
	public void refreshAsyncElements() {
		csdlMic = getMicDataManager();
		csdlLoc = getLocDataManager();
		communicationDAO = getCommunicationDAO();
	}

	// Singleton factory (of sorts) for the Sound data capture class
	private ContinuousSensorDataListener getMicDataManager() {
		if (csdlMic == null || csdlMic.isSubscribed() == false) {
			return new ContinuousSensorDataListener(this);
		} else {
			return csdlMic;
		}
	}

	// Singleton factory (of sorts) for the Location data capture class
	private ContinuousSensorDataListener getLocDataManager() {
		if (csdlLoc == null || csdlLoc.isSubscribed() == false) {
			return new ContinuousSensorDataListener(this);
		} else {
			return csdlLoc;
		}
	}

	// Singleton factory (of sorts) for the Xively data upload  class
	private UploadData getCommunicationDAO() {
		if (communicationDAO != null) {
			communicationDAO.releaseService();
		}
		return new UploadData(this);
	}

	// Update local variables which hold the values of UI editable text areas (converting to milliseconds or seconds as necessary)
	public void updateSettings() {
		lPauseLength = Integer.parseInt(txtFreqTime.getText().toString())*1000*60;
		lMicMeasurementLength = Integer.parseInt(txtSPLTime.getText().toString())*1000;
		lLocMeasurementLength = Integer.parseInt(txtLocTime.getText().toString())*1000;
	}

	// Validation of data entry into UI editable text areas
	// Method "Is_Valid_Number_Validation()" derived form an example provided by
	// Chintan Khetiya (https://sites.google.com/site/khetiyachintan/home-1/home)
	public boolean Is_Valid_Number_Validation(int MinLen, int MaxLen, EditText edt) throws NumberFormatException {
		boolean Is_Valid_Number = true;
		if (edt.getText().toString().length() == 0 || edt.getText().toString().equals("") || edt.getText().toString().equals(null)) {
			edt.setError("Please enter numeric value");
			Is_Valid_Number = false;
		} else if (Integer.valueOf(edt.getText().toString()) < MinLen || Integer.valueOf(edt.getText().toString()) > MaxLen) {
			edt.setError("Out of Range: Please stay between " + MinLen + " and " + MaxLen);
			Is_Valid_Number = false;
		} else {
			edt.setError(null);
			//	edt.getText().toString();
			//	Toast.makeText(this, edt.getError(), Toast.LENGTH_LONG).show();
		}

		return Is_Valid_Number;
	}

	// Check that all data has been captured, and if so send for uploadS
	public void checkDataAndSendForUpload() {
		if (csdlMic.getSoundPressureLevel() != null && csdlLoc.getLatitude() != null && csdlLoc.getLongitude() != null) {
			try {
				setDetailText("Full dataset collected.\nUploading data...");
				communicationDAO.execute(csdlMic.getSoundPressureLevel(), csdlLoc.getLatitude(), csdlLoc.getLongitude());

				// Reset the variables to ensure a new set is attained before upload
				csdlMic.setSoundPressureLevel(null);
				csdlLoc.setLatitude(null);
				csdlLoc.setLongitude(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			setDetailText("Awaiting complete dataset before upload...");
		}
	}

	// Reset all UI button enabled statuses when there are no subscriptions for regular data updates
	public void buttonResetCheck() {
		if (!csdlMic.isSubscribed() && !csdlLoc.isSubscribed()) {
			btnLogOnce.setEnabled(true);
			btnContStop.setEnabled(false);
			btnContStart.setEnabled(true);
		}
	}

	// Retrieve results of the data upload attempt and display to user
	public void outputUploadResults() {
		if (communicationDAO.getSuccessfulUpload()) {
			//All uploaded ok!
			setDetailText("All uploaded OK!");
		} else {
			setDetailText("Upload failed - Error details:");
		}
		setDetailText(communicationDAO.getResultMessage());

		refreshAsyncElements();
	}

	public void onResume() {
		super.onResume();
	}

	// Release all Async tasks to reduce memory leakage and battery strain
	public void switchOffAllTools() {
		if (communicationDAO != null) {
			communicationDAO.releaseService();
		}

		if (csdlMic != null) {
			if (csdlMic.isSubscribed()) {
				csdlMic.unsubscribeFromSensorData();
			}
		}

		if (csdlLoc != null) {
			if (csdlLoc.isSubscribed()) {
				csdlLoc.unsubscribeFromSensorData();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		switchOffAllTools();
	}


	public void onPause() {
		super.onPause();
		if (communicationDAO != null) {
			communicationDAO.releaseService();
		}
	}

	@Override 
	public void onStart() {
		super.onStart();
	}

	// Getter method for UI results data
	public String getDetailText() {
		return (String) txtResults.getText();
	}

	// Clear UI results data
	public void clearDetailText() {
		txtResults.setText("");
	}

	// Setter method for UI results data
	public void setDetailText(String details) {
		if (getDetailText().length() == 0) {
			txtResults.setText(getDetailText() + details);
		} else {
			txtResults.setText(getDetailText() + "\n" + details);
		}
	}
}
