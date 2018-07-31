package com.ajie.bluetoothcard.pre;


import android.content.Context;
import android.content.SharedPreferences;


public class Preference {
	private Context mContext;
	SharedPreferences mSharedPreferences;
	
	public Preference(Context context){
		mContext = context;
		mSharedPreferences = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
	}
	
	private static Preference instance;
	
	public static Preference getInstance(Context context){
		if(instance == null){
			instance = new Preference(context);
		}
		return instance;
	}

	

	
	// 是否是第一次插入------------------------
	
	public	final static String APP_FIRST_TIME = "APP_FIRST_TIME";
	public  long  FIRST_TIME = 0;
	
	public long getFirstTime() {
		return mSharedPreferences.getLong(APP_FIRST_TIME, FIRST_TIME);
	}
	
	public void setFirstTime(long firstTime) {
		mSharedPreferences.edit().putLong(APP_FIRST_TIME, firstTime).commit();
	}
	
	// 是否是第一次插入------------------------
	
		public	final static String FIRST_INSERT = "FIRST_INSERT";
		public  boolean  isFirst_insert = true;
		
		public boolean getIsFirst_insert() {
			return mSharedPreferences.getBoolean(FIRST_INSERT, isFirst_insert);
		}
		
		public void setFIRST_INSERT(boolean isFirst_insert) {
			mSharedPreferences.edit().putBoolean(FIRST_INSERT, isFirst_insert).commit();
		}
		
		// 是否是第一次插入------------------------
		
		public	final static String FIRST_INSERT_BOX = "FIRST_INSERT_BOX";
		public  boolean  isFirst_insert_box = true;
		
		public boolean getIsFirst_insert_box() {
			return mSharedPreferences.getBoolean(FIRST_INSERT_BOX, isFirst_insert_box);
		}
		
		public void setFirst_insert_box(boolean isFirst_insert_box) {
			mSharedPreferences.edit().putBoolean(FIRST_INSERT_BOX, isFirst_insert_box).commit();
		}
		
		// 是否是第一次插入------------------------
		
		public	final static String DEVICE_NAME = "DEVICE_NAME";
		public  String  device_name_key = "";
		
		public String getDeviceNameKey() {
			return mSharedPreferences.getString(DEVICE_NAME, device_name_key);
		}

		public void setDeviceNameKey(String device_name_key) {
			mSharedPreferences.edit().putString(DEVICE_NAME, device_name_key).commit();
		}

}
