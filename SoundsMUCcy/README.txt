Copyright (c) 2014 Tom Hedges

------------------------------------------------------------

Birkbeck - Mobile & Ubiquotous Computing (MUC) Spring 2014

------------------------------------------------------------

Intended for (and tested solely upon) Android UK mobile phone
devices running Android version 4.3. System may function less
consistently on devices which do not meet this specification.

Please also see the attached LICENSE file.

Source code available from: https://github.com/TomHedges/muc2014.git

------------------------------------------------------------

"Sounds MUCcy" is a noise logging app, that takes an amplitude sample
from the microphone of your Android device (without recording), converts this
to Sound Pressure Level (dB), and uploads the value, along with your location
(latitude and longitude) and Android device UUID to a Xively feed (https://xively.com/whats_xively/).
The system incorporates the "Sensor Manager" and "Sensor Data Manager" libraries
(c) University of Cambridge (Neal Lathia).

The system has a single user interface, allowing entry of the sound (amplitude)
sampling length, location sampling length, and the frequency with which you
would like the device to log data to Xively. The system can either perform a
single on-demand reading, or can be set to take and upload regular readings.

The system should only attempt to take a reading if an internet connection is
available. The system will attempt to use the fine-grain GPS location, though
if this is not available, the Sensor Manager will report a coarse-grain location.
Where no location data is available, latitude and longitude are recorded as "unknown".

If the system is able to take location and sound readings, it will parse the
Sensor Manager JSON results data and upload these to Xively, and then check
that Xively has successfully recorded all data.

The system logs its actions to the "Results" panel on-screen, along with some
error messages. Text lines are added to the bottom of the screen, and new text
will begin to push older lines off the top of the screen. The screen is cleared
ahead of a single reading/logging action.

The system has some error-handling and reporting, and is relatively stable. However,
there are some intermittent instabilities.

The Xively feed has been set up to receive four seperate datastreams - DeviceID,
Sound Pressure Level, Latitude and Longitude. I have opted to log Lat and Long to
their own datastreams rather than use the native Xively recording ("waypoints"),
as I feel it provides greater control and accuracy. Waypoints apply their own timestamp,
which would be at least slightly different from the device generated timestamp of the
ID/Sound Pressure Level readings. As Waypoints and Datapoints are effectively not connected,
it could make data processing of the results more difficult. My approach allows every piece
of data to be tied together exactly by timestamp. This means that multiple devices could
safely log to the same Xively feed, without fear of confused results. However, this would
be a naive implementation of Xively, as each device should record to a separate feed.