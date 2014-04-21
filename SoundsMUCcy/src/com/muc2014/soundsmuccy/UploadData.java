/* **************************************************
 
 Copyright (c) Tom Hedges 2014
 
 This class file developed from an original, copyrighted by LogMeIn Inc.
 Your use of this software and its source code is limited by the LogMeIn Inc. license below, as well as this application's LICENSE file.
 
 ----------------------------------
 
This library is Open Source, under the BSD 3-Clause license.

Copyright (c) 2013, LogMeIn Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    Neither the names of LogMeIn, Inc., nor Xively Ltd., nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL LOGMEIN, INC. OR XIVELY LTD. BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 

 ************************************************** */

package com.muc2014.soundsmuccy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.widget.Toast;

import com.xively.android.service.IHttpService;
import com.xively.android.service.Response;

public class UploadData extends AsyncTask<String, Void, Boolean> {
	private IHttpService service;
	private HttpServiceConnection connection;
	private Activity context;
	private String myApiKey;
	private int myFeedId;
	private String myDatastreamIdSPL;
	private String myDatastreamIdDevID;
	private String myDatastreamIdLat;
	private String myDatastreamIdLong;
	private String strResultsMessage = "";
	private Boolean bSuccessfulUpload;


	public UploadData(Activity context) {
		this.context = context;
		initService();
		myApiKey = context.getString(R.string.api_key);
		myFeedId = Integer.parseInt(context.getString(R.string.feed_id));
		myDatastreamIdSPL = context.getString(R.string.datastream_SPL);
		myDatastreamIdDevID = context.getString(R.string.datastream_DevID);
		myDatastreamIdLat = context.getString(R.string.datastream_Lat);
		myDatastreamIdLong = context.getString(R.string.datastream_Long);
	}

	// Format results for upload to Xively, send, and check receipt Asynchronously, without locking out UI or other processes
	@Override
	protected Boolean doInBackground(String... params) {
		String strSPL = params[0];
		String strLat = params[1];
		String strLong = params[2];

		Response reply;
		boolean allUploaded = false;

		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSS'Z'");
		ft.setTimeZone(TimeZone.getTimeZone("UTC"));

		try {
			service.setApiKey(myApiKey);

			String dtstamp = ft.format(new Date());
			String android_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);

			String uploadDataSPL = "{ \"datapoints\":[ {\"at\" : \""
				+ dtstamp + "\" , \"value\":\"" + strSPL + "\"} ] }";

			String uploadDataDevID = "{ \"datapoints\":[ {\"at\" : \""
				+ dtstamp + "\" , \"value\":\"" + android_id + "\"} ] }";

			String uploadDataLat = "{ \"datapoints\":[ {\"at\" : \""
				+ dtstamp + "\" , \"value\":\"" + strLat + "\"} ] }";

			String uploadDataLong = "{ \"datapoints\":[ {\"at\" : \""
				+ dtstamp + "\" , \"value\":\"" + strLong + "\"} ] }";

			service.createDatapoint(myFeedId, myDatastreamIdDevID, uploadDataDevID);
			service.createDatapoint(myFeedId, myDatastreamIdSPL, uploadDataSPL);
			service.createDatapoint(myFeedId, myDatastreamIdLat, uploadDataLat);
			service.createDatapoint(myFeedId, myDatastreamIdLong, uploadDataLong);

			// This could be refactored to check status of original upload rather than perform second request - though this is belt and braces.
			// Could also be performed differently to capture all error messages for user, rather than just the first encountered.
			if (service.getDatapoint(myFeedId, myDatastreamIdDevID, dtstamp).getStatusCode() == 200) {
				if (service.getDatapoint(myFeedId, myDatastreamIdSPL, dtstamp).getStatusCode() == 200) {
					if (service.getDatapoint(myFeedId, myDatastreamIdLat, dtstamp).getStatusCode() == 200) {
						if (service.getDatapoint(myFeedId, myDatastreamIdLong, dtstamp).getStatusCode() == 200) {
							allUploaded = true;
							setResultsMessage("Data uploaded with Device ID: " + android_id + "\nData uploaded with timestamp: " + dtstamp);
						} else {
							setResultsMessage("Longitude was not successfully uploaded!");
						}
					} else {
						setResultsMessage("Latitude was not successfully uploaded!");
					}
				} else {
					setResultsMessage("Sound Pressure Level was not successfully uploaded!");
				}
			} else {
				setResultsMessage("DeviceID was not successfully uploaded!");
			}

		} catch (RemoteException e) {
			e.printStackTrace();

		}

		return allUploaded;
	}

	// Record status after carrying out upload and receipt check
	@Override
	protected void onPostExecute(Boolean bSuccessful) {
		setSuccessfulUpload(bSuccessful);
	}

	// Initiate post-upload actions in main thread - should be de-coupled
	private void setSuccessfulUpload(Boolean bSuccessfulUpload) {
		this.bSuccessfulUpload = bSuccessfulUpload;
		((SoundLevelLogging) context).outputUploadResults();
		((SoundLevelLogging) context).buttonResetCheck();
	}

	// Getter method for upload result status
	public Boolean getSuccessfulUpload() {
		return bSuccessfulUpload;
	}

	/**
	 * Binds this activity to the service.
	 */
	private void initService() {
		connection = new HttpServiceConnection();
		Intent i = new Intent("com.xively.android.service.HttpService");
		boolean ret = context.bindService(i, connection, Context.BIND_AUTO_CREATE);
		//		Toast.makeText(context, "ret = " + ret, Toast.LENGTH_LONG).show();
	}

	/**
	 * Unbinds this activity from the service.
	 */
	public void releaseService() {
		if (connection != null) {
			context.unbindService(connection);
		}
		connection = null;
	}

	// Setter method for verbose upload results
	private void setResultsMessage(String strResultMessage) {
		if (this.strResultsMessage.length() == 0) {
			this.strResultsMessage = strResultMessage;
		} else {
			this.strResultsMessage = this.strResultsMessage + "\n" + strResultMessage;
		}
	}

	// Getter method for verbose upload results
	public String getResultMessage() {
		return strResultsMessage;
	}

	// Inner class to manage HTTP Connection for results upload
	class HttpServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder boundService) {
			service = IHttpService.Stub.asInterface((IBinder) boundService);
			//Toast.makeText(context, "Service connected", Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName name) {
			service = null;
			//Toast.makeText(context, "Service disconnected", Toast.LENGTH_SHORT).show();
		}
	}
}
