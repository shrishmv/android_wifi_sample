	package com.example.wifitest;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String LOGTAG = "BJN_DEBUG";
	
	ConnectivityManager conMan;
	TextView connection;
	TextView strength;
	TextView wifiInfoText;
	TelephonyManager telephonyManager;
	
	WifiManager wifimanager;
	
	NetworkUpdate task;
	
	ActivityManager appManager; 
	
	LinuxUtils linuxUtils;
	
	private static final String BJN_PACKAGE = "com.bluejeansnet.Base.Qoe13";
	
	int bjn_pid;
	
	String mBssid;
	String mSsid;
//	static final int TIME_TEST = 10;
//	int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connection = (TextView)findViewById(R.id.connection_type);
		strength = (TextView)findViewById(R.id.signal_val);
		wifiInfoText = (TextView) findViewById(R.id.wifi_info);
		telephonyManager =     (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	
		wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		appManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	
		linuxUtils = new LinuxUtils();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		task = new NetworkUpdate();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		
//		List<RunningAppProcessInfo> processes = appManager.getRunningAppProcesses();
//		
//		for(RunningAppProcessInfo p : processes){
//			Log.d(LOGTAG," process > pid - "+p.pid);
//			Log.d(LOGTAG," process > processName - "+p.processName);
//			
//			if(p.processName.contains(BJN_PACKAGE)){
//				bjn_pid = p.pid;
//			}
//		}
		 
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		task.cancelTask();
		task.cancel(true);
		task = null;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * @param ctx
	 * @return True if device has internet
	 *
	 * Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean haveInternet(Context ctx) {

	    NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
	            .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    if (info.isRoaming()) {
	        // here is the roaming option you can change it if you want to
	        // disable internet while roaming, just return false
	        return true;
	    }
	    return true;
	}
	
	public class NetworkUpdate extends AsyncTask<Void, Void, Void>{

		String connectionType;
		int sigstrength;
		boolean network = false;
		String bssid;
		String ssid;
		
		
		@SuppressLint("NewApi")
		@Override
		protected Void doInBackground(Void... params) {
			
			//mobile
			State mobile = conMan.getNetworkInfo(0).getState();

			//wifi
			State wifi = conMan.getNetworkInfo(1).getState();
		
			if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
				network = true;
				connectionType = "mobile data";
			} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
			    network = true;
			    connectionType = "wifi";
			}
			
			while(network){
				
				if(connectionType.equals("mobile data")){
					
					int sigstrengthmob = 0 ;
					CellInfo cellinfo = (CellInfo)telephonyManager.getAllCellInfo().get(0);
					
					if(cellinfo instanceof CellInfoWcdma){
						CellInfoWcdma cellinfospecific = (CellInfoWcdma) cellinfo;
						CellSignalStrengthWcdma cellSignalStrengthGsm = cellinfospecific.getCellSignalStrength();
						sigstrengthmob = cellSignalStrengthGsm.getDbm();
					}else if(cellinfo instanceof CellInfoGsm){
						CellInfoGsm cellinfospecific = (CellInfoGsm) cellinfo;
						CellSignalStrengthGsm cellSignalStrengthGsm = cellinfospecific.getCellSignalStrength();
						sigstrengthmob = cellSignalStrengthGsm.getDbm();
					}else if(cellinfo instanceof CellInfoCdma){
						CellInfoCdma cellinfospecific = (CellInfoCdma) cellinfo;
						CellSignalStrengthCdma cellSignalStrengthGsm = cellinfospecific.getCellSignalStrength();
						sigstrengthmob = cellSignalStrengthGsm.getDbm();
					}else if(cellinfo instanceof CellInfoLte){
						CellInfoLte cellinfospecific = (CellInfoLte) cellinfo;
						CellSignalStrengthLte cellSignalStrengthGsm = cellinfospecific.getCellSignalStrength();
						sigstrengthmob = cellSignalStrengthGsm.getDbm();
						
					}

					sigstrength=WifiManager.calculateSignalLevel(sigstrengthmob, 100);

					
					
				}else if(connectionType.equals("wifi")){
					
					int numberOfLevels=100;
					WifiInfo wifiInfo = wifimanager.getConnectionInfo();
					 bssid = wifiInfo.getBSSID();
					 ssid = wifiInfo.getSSID();

					sigstrength=WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
					
				}
				
				publishProgress();
				
				//Log.d(LOGTAG, "TOTAL CPU USAGE - "+readCPUUsage());
				
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			
			//count++;
			connection.setText(connectionType);
			
			strength.setText(""+sigstrength);
			
//			if(0 == count%TIME_TEST){
//				bssid = "blah";
//			}
			
			if((null != mBssid)&&(!mBssid.equals(bssid))){
				// vibration for 500 milliseconds
				((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
				Random rnd = new Random(); 
				int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
				wifiInfoText.setTextColor(color);
			}
			
			mBssid = bssid;
			mSsid = ssid;
			
			wifiInfoText.setText("Bssid - "+mBssid+" Ssid - "+mSsid);
			
			if(connectionType.equals("wifi")){
				wifiInfoText.setVisibility(View.VISIBLE);
			}else{			
				wifiInfoText.setVisibility(View.GONE);
			}
			
			
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(), "NO INTERNET", Toast.LENGTH_SHORT).show();
			
			connection.setText("NO INTERNET");
			
			strength.setText("0");
		}
		
		public void cancelTask(){
			network = false;
		}
	}
	
	private float readCPUUsage() {
	    try {
	        RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        String load = reader.readLine();

	        String[] toks = load.split(" +");  // Split on one or more spaces

	        long idle1 = Long.parseLong(toks[4]);
	        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        try {
	            Thread.sleep(360);
	        } catch (Exception e) {}

	        reader.seek(0);
	        load = reader.readLine();
	        reader.close();

	        toks = load.split(" +");

	        long idle2 = Long.parseLong(toks[4]);
	        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
	            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }

	    return 0;
	} 
}
