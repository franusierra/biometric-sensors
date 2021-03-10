package com.example.medicalnotifications;

import java.net.Inet4Address;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.SharedElementCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.medicalnotifications.database.Alarm;
import com.example.medicalnotifications.database.DatabaseClient;
import com.example.medicalnotifications.utils.AlarmConverter;
import com.example.medicalnotifications.utils.Notifications;

public class MQTTService extends Service {
	public static final String BROADCAST_ACTION="com.example.medicalnotifications.RESET_SERVICE_BROADCAST";
	private static final String PREFERENCE_CHANGE_ACTION="com.example.medicalnotifications.CONNECTION_PREFERENCE_CHANGED";
	private SharedPreferences.OnSharedPreferenceChangeListener sharedListener=null;
	private static final String TAG = "MQTTService";
	private static boolean hasWifi = false;
	private static boolean hasMmobile = false;
	private BroadcastReceiver mBroadcast;

	private ConnectivityManager mConnMan;
	private volatile IMqttAsyncClient mqttClient;
	private String deviceId;
	private NsdManager.ResolveListener resolveListener;
	private NsdManager.DiscoveryListener discoveryListener;
	private NsdManager nsdManager;


	private static final String alarmsTopic="clinic/alarms/+";
	private static final String serviceType="_mqtt._tcp.";

	@Override
	public void onCreate() {
		IntentFilter intentf = new IntentFilter();
		setClientID();
		intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mBroadcast=new ConnectionChangeBroadcastReceiver();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(PREFERENCE_CHANGE_ACTION);
		registerReceiver(mBroadcast, intentFilter);
		mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		sharedListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if(!key.equals("alarm_notification_id")) {
					if(mqttClient!=null && mqttClient.isConnected()) {
						try {
							IMqttToken token;
							token = mqttClient.disconnect();
							token.waitForCompletion(1000);
						} catch (MqttException e) {
							e.printStackTrace();
						}

					}
					sendBroadcast(new Intent(PREFERENCE_CHANGE_ACTION));
				}
			}
		};
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sharedListener);
	}
	private void setClientID(){
		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo();
		deviceId = wInfo.getMacAddress();
		if(deviceId == null){
			deviceId = MqttAsyncClient.generateClientId();
		}

	}
	private void doConnect(String mqttUri){
		Log.d(TAG, "doConnect("+mqttUri+")");
		IMqttToken token;
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(true);
		try {
			mqttClient = new MqttAsyncClient(mqttUri, deviceId, new MemoryPersistence());
			token = mqttClient.connect();
			token.waitForCompletion(3500);
			mqttClient.setCallback(new MqttEventCallback(this));
			token = mqttClient.subscribe(alarmsTopic, 0);
			token.waitForCompletion(5000);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			switch (e.getReasonCode()) {
				case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
				case MqttException.REASON_CODE_CLIENT_TIMEOUT:
				case MqttException.REASON_CODE_CONNECTION_LOST:
				case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
					Log.v(TAG, "c" +e.getMessage());
					e.printStackTrace();
					break;
				case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
					Log.e(TAG, "b"+ e.getMessage());
					break;
				default:
					Log.e(TAG, "a" + e.getMessage());
			}
		}
	}

	class ConnectionChangeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			IMqttToken token;
			boolean hasConnectivity = false;
			boolean hasChanged = false;
			NetworkInfo infos[] = mConnMan.getAllNetworkInfo();


			// Check if N/W connectivity is available

			for (int i = 0; i < infos.length; i++){
				if (infos[i].getTypeName().equalsIgnoreCase("MOBILE")){
					if((infos[i].isConnected() != hasMmobile)){
						hasChanged = true;
						hasMmobile = infos[i].isConnected();
					}
					Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());

				} else if ( infos[i].getTypeName().equalsIgnoreCase("WIFI") ){
					if((infos[i].isConnected() != hasWifi)){
						hasChanged = true;
						hasWifi = infos[i].isConnected();
					}
					Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
				}
			}

			hasConnectivity = hasMmobile || hasWifi;
			Log.v(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - "+(mqttClient == null || !mqttClient.isConnected()));

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean localConnectivity=preferences.getBoolean("local_connection",true);
			if (hasConnectivity && (mqttClient == null || !mqttClient.isConnected())) {
				if(localConnectivity) {
					Log.d(TAG,"Connecting to local mqtt...");
					if (hasWifi) {
						findMQTTService();
					} else {
						Notifications.noWifiNotification(context);
					}
				}else{
					String mqttURL=preferences.getString("remote_mqtt_server",null);
					Log.d(TAG,"Connecting to: "+mqttURL);
					if(mqttURL!=null){
						doConnect("tcp://"+mqttURL);
					}
				}
			} else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
				Log.d(TAG, "doDisconnect()");
				try {
					token = mqttClient.disconnect();
					token.waitForCompletion(1000);
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		}

	};

	public class MQTTBinder extends Binder {
		public MQTTService getService(){
			return MQTTService.this;
		}
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcast);
		sendBroadcast(new Intent(BROADCAST_ACTION));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged()");
		android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}



	private void findMQTTService(){
		resolveListener = new NsdManager.ResolveListener() {
			@Override
			public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

			}

			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				Log.d(TAG,"Service resolved "+serviceInfo.toString());
				int port = serviceInfo.getPort();
				Inet4Address host = (Inet4Address) serviceInfo.getHost();
				doConnect("tcp:/"+host.toString()+":"+port );
			}
		};
		discoveryListener=new NsdManager.DiscoveryListener() {
			@Override
			public void onStartDiscoveryFailed(String serviceType, int errorCode) {

			}

			@Override
			public void onStopDiscoveryFailed(String serviceType, int errorCode) {

			}

			@Override
			public void onDiscoveryStarted(String serviceType) {

			}

			@Override
			public void onDiscoveryStopped(String serviceType) {

			}

			@Override
			public void onServiceFound(NsdServiceInfo serviceInfo) {
				Log.d(TAG,"Service found "+serviceInfo);
				nsdManager.resolveService(serviceInfo,resolveListener);
			}

			@Override
			public void onServiceLost(NsdServiceInfo serviceInfo) {

			}
		};
		nsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
		nsdManager.discoverServices(serviceType,NsdManager.PROTOCOL_DNS_SD,discoveryListener);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand()");
		return START_STICKY;
	}
	class SaveAlarm extends AsyncTask<Alarm, Void, Void> {

		@Override
		protected Void doInBackground(Alarm... alarms) {

			//adding to database
			DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
					.alarmDao()
					.insert(alarms[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
		}
	}

	private class MqttEventCallback implements MqttCallback {
		Context mContext;
		MqttEventCallback(Context context){
			mContext=context;
		}

		@Override
		public void connectionLost(Throwable arg0) {
			findMQTTService();
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {

		}

		@Override
		public void messageArrived(String topic, final MqttMessage msg) throws Exception {
			Log.i(TAG, "Message arrived from topic " + topic);

			Handler h = new Handler(getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					try {
						JSONObject msgJson=new JSONObject(msg.toString());
						String alarmTopic=topic.substring(topic.lastIndexOf("/")+1);
						Alarm alarm=AlarmConverter.jsonObjectToAlarm(msgJson,alarmTopic);
						SaveAlarm sa=new SaveAlarm();
						sa.execute(alarm);
						Notifications.alarmNotification(MQTTService.this.getApplicationContext(), alarm, new Intent(getApplicationContext(), MainActivity.class), AlarmActivity.getAlarmImageResource(alarm.getAlarm_typeEnum()));
						if(!MainActivity.active) {
							Intent launchA = AlarmActivity.getIntent(mContext,alarm);
							launchA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
							startActivity(launchA);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}


				}
			});
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind called");
		return null;
	}

}