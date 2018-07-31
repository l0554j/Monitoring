package com.ajie.bluetoothcard.ui;


import java.io.IOException;

import com.ajie.bluetoothcard.pre.Preference;
import com.ajie.bluetoothcard.util.CommUtil;
import com.ajie.monitoring.bluetooth.BluetoothService;
import com.ajie.monitoring.bluetooth.CollectorFactory;
import com.ajie.monitoring.bluetooth.Command;
import com.ajie.monitoring.bluetooth.WifiService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/** 主界面 */
public class MainActivity extends BaseActivity  {
	protected static final String TAG = MainActivity.class.getSimpleName();
	private Button main_pause, main_electricize, main_order_set,main_order_electricize;
	/** 电池电压*/
	private TextView main_battery_voltage,main_battery_voltage_value;
	/** 充电电流*/
	private TextView main_charge_current,main_charge_current_value;
	/** 已经完成*/
	private TextView main_already_over,main_already_over_value;
	/** 已经充电时间*/
	private TextView main_already_charge_time,main_already_charge_time_value;
	/** 预计完成时间*/
	private TextView main_estimated_time_of_completion,main_estimated_time_of_completion_value;
	/** 产品生产日期*/
	private TextView main_date_of_manufacture,main_date_of_manufacture_value;
	/** 蓝牙连接方式*/
	private TextView bluetooth_collect;
	/** Wifi连接方式*/
	private TextView wifi_collect;
	/** 连接蓝牙*/
	private TextView collect_blutooth;
	/** 选择网络 */
	private TextView collect_wifi;
	/** 连接设备*/
	private TextView collect_equipment;
	private View collect_layout,wifi_layout,bluetooth_layout;
	
	 private BluetoothAdapter mBluetoothAdapter = null;
	 
	 private BluetoothService bluetoothService;
	 private String orderTime = null;
	 private Preference preference;
	 ProgressDialog bluetoothDia;
//	private SocketStateListener mBluetoothStateListener;
	 /** 后续要删除 */
//	 private boolean isAutomation = true;
	 /** 是否查看过时间日期 */
	 private boolean isLook = false;
	 /** 是否连接上蓝牙 */
	 private boolean isConnect = false;
	 /** 是否是第一次连接 */
	 private boolean isFirst = true;
	 /** 是否是第一次连接 */
	 private boolean isFirstConnect = true;
	WifiService wifiService = null;
	/** 连接类型  1:wifi 2:蓝牙*/
	private int connectType = 1;
	 
	// 定时重新登录
	private Runnable getValueTask = new Runnable() {
		public void run() {
			myHandler.postDelayed(this, 1 * 1000);
			// 需要执行的代码
			if(isConnect){
				sendMessageBuffer(Command.makeReadBatteryVoltage());
			}
		}
	};
	
	// 定时去连接
	private Runnable connectDevice = new Runnable() {
		public void run() {
			myHandler.postDelayed(this, 5 * 1000);
			// 需要执行的代码
			if(!isConnect&&!isFirst){
				if(connectType == 2){
					if(device!=null){
						bluetoothService.connect(device);
					}
				}else{
					if(wifiService!=null){
						wifiService.startConn("10.10.100.254", 8899);
					}
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
//		myHandler.postDelayed(getValueTask, 5 * 1000);//隔一段时间去获取有没新的回复
		if(preference.getIsFirst_insert()){
			preference.setFirstTime(System.currentTimeMillis());
			preference.setFIRST_INSERT(false);
		}else{
			long fisrtTime = preference.getFirstTime();
			if((System.currentTimeMillis() - fisrtTime)>7*24*60*60*1000){
				showToast("软件使用期限以过，请联系开发人员。");
				myHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						MainActivity.this.finish();
					}
				}, 1000*5);
			}
		}
	}

	@Override
	protected void initView() {
		super.initView();
		preference = new Preference(this);
		
		main_pause = (Button) findViewById(R.id.main_pause);
		main_pause.setOnClickListener(this);
		main_electricize = (Button) findViewById(R.id.main_electricize);
		main_electricize.setOnClickListener(this);
		main_order_set = (Button) findViewById(R.id.main_order_set);
		main_order_set.setOnClickListener(this);
		main_order_electricize = (Button) findViewById(R.id.main_order_electricize);
		main_order_electricize.setOnClickListener(this);
		
		
		main_battery_voltage =  (TextView) findViewById(R.id.main_battery_voltage);
		main_battery_voltage_value =  (TextView) findViewById(R.id.main_battery_voltage_value);
		main_charge_current =  (TextView) findViewById(R.id.main_charge_current);
		main_charge_current_value =  (TextView) findViewById(R.id.main_charge_current_value);
		main_already_over =  (TextView) findViewById(R.id.main_already_over);
		main_already_over_value =  (TextView) findViewById(R.id.main_already_over_value);
		main_already_charge_time =  (TextView) findViewById(R.id.main_already_charge_time);
		main_already_charge_time_value =  (TextView) findViewById(R.id.main_already_charge_time_value);
		main_estimated_time_of_completion =  (TextView) findViewById(R.id.main_estimated_time_of_completion);
		main_estimated_time_of_completion_value =  (TextView) findViewById(R.id.main_estimated_time_of_completion_value);
		main_date_of_manufacture =  (TextView) findViewById(R.id.main_date_of_manufacture);
		main_date_of_manufacture_value =  (TextView) findViewById(R.id.main_date_of_manufacture_value);
		
		collect_blutooth =  (TextView) findViewById(R.id.collect_blutooth);
		collect_blutooth.setOnClickListener(this);
		collect_wifi =  (TextView) findViewById(R.id.collect_wifi);
		collect_wifi.setOnClickListener(this);
	
		
		bluetooth_collect =  (TextView) findViewById(R.id.bluetooth_collect);
		bluetooth_collect.setOnClickListener(this);
		wifi_collect =  (TextView) findViewById(R.id.wifi_collect);
		wifi_collect.setOnClickListener(this);
		collect_equipment =  (TextView) findViewById(R.id.collect_equipment);
		collect_equipment.setOnClickListener(this);
		
		collect_layout =   findViewById(R.id.collect_layout);
		wifi_layout =   findViewById(R.id.wifi_layout);
		bluetooth_layout =   findViewById(R.id.blutooth_layout);
		
//		main_battery_voltage.setOnClickListener(this);
//		main_charge_current.setOnClickListener(this);
//		main_already_over.setOnClickListener(this);
//		main_already_charge_time.setOnClickListener(this);
//		main_estimated_time_of_completion.setOnClickListener(this);
//		main_date_of_manufacture.setOnClickListener(this);
		

//		bluetoothService = new BluetoothService(this, myHandler);
//      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//      if (mBluetoothAdapter == null) {
//          Toast.makeText(this, "蓝牙功能不可用", Toast.LENGTH_LONG).show();
//      }
//      if (!mBluetoothAdapter.isEnabled()) {
//          Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//          startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//      }else{
//      	if(!"".equals(preference.getDeviceNameKey())){
//      		connectBluetooth(preference.getDeviceNameKey());
//      	}
//      }
	}

	
//	private void getValue() {
//		// TODO Auto-generated method stub
////		sendThrad(Command.makeReadBatteryVoltage());
////		sendThrad(Command.makeReadChargingCurrent());
////		sendThrad(Command.makeReadChargingPerentage());
////		sendThrad(Command.makeReadChargingTime());
////		sendThrad(Command.makeReadChargingOver());
////		sendThrad(Command.makeReadProductionDate());
//		try{
//			Thread.currentThread().sleep(1000);
////			showToast("电池电压");
//			sendMessage(Command.makeReadBatteryVoltage());
//			Thread.currentThread().sleep(1000);
////			showToast("充电电流");
//			sendMessage(Command.makeReadChargingCurrent());
//			Thread.currentThread().sleep(1000);
////			showToast("已经完成");
//			sendMessage(Command.makeReadChargingPerentage());
//			Thread.currentThread().sleep(1000);
////			showToast("已经充电时间");
//			sendMessage(Command.makeReadChargingTime());
//			Thread.currentThread().sleep(1000);
////			showToast("预计完成时间");
//			sendMessage(Command.makeReadChargingOver());
//			Thread.currentThread().sleep(1000);
////			showToast("产品生产日期");
//			sendMessage(Command.makeReadProductionDate());
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	private void sendThrad(final byte[] data) {
//		// TODO Auto-generated method stub
//		new Thread() {										// 线程对象
//			public void run() {								// 线程主体方法
//				try {
//					Thread.sleep(1000);						// 运行3秒后关闭对话框
//					sendMessage(data);
//					System.out
//							.println("MainActivity.sendThrad(...).new Thread() {...}.run()");
//				} catch (Exception e) {
//				} 
//			}}.start();	
//	}
	@Override
	public void onClick(View v) {
//		isAutomation = false;
		switch (v.getId()) {
		case R.id.main_pause:
//			showToast("暂停");
			sendMessageBuffer(Command.makeWriteOperationPause());
//			Message msg = new Message();
//			msg.what = WifiService.RELAYOPT;
//			hMsg.sendMessage(msg);
			break;
		case R.id.main_electricize:
//			showToast("充电");
			sendMessageBuffer(Command.makeWriteOperationStart());
			break;
		case R.id.main_order_set:
//			showToast("预约设置");
			startActivityForResult(new Intent(this,OrderSettingActivity.class).putExtra("orderTime", orderTime), 10001);
			break;
		case R.id.main_order_electricize:
//			showToast("预约充电");
			if(orderTime==null){
				showToast("请先设置好预约充电时间");
				return;
			}
			String hour = Integer.toHexString(Integer.parseInt(orderTime.substring(0, 2)));
			String minute = Integer.toHexString(Integer.parseInt(orderTime.substring(2, 4)));
			if( hour.length() == 1){
				hour = "0"+hour;
			}
			if( minute.length() == 1){
				minute = "0"+minute;
			}
//			sendMessage(Command.makeWriteOrderSet(HexString2Bytes(orderTime)[0], HexString2Bytes(orderTime)[1]));
			sendMessageBuffer(Command.makeWriteOrderSet(CollectorFactory.HexString2Bytes(hour+minute)[0], CollectorFactory.HexString2Bytes(hour+minute)[1]));
			break;
		case R.id.main_battery_voltage:
//			showToast("电池电压");
			sendMessageBuffer(Command.makeReadBatteryVoltage());
			break;
		case R.id.main_charge_current:
//			showToast("充电电流");
			sendMessageBuffer(Command.makeReadChargingCurrent());
			break;
		case R.id.main_already_over:
//			showToast("已经完成");
			sendMessageBuffer(Command.makeReadChargingPerentage());
			break;
		case R.id.main_already_charge_time:
//			showToast("已经充电时间");
			sendMessageBuffer(Command.makeReadChargingTime());
			break;
		case R.id.main_estimated_time_of_completion:
//			showToast("预计完成时间");
			sendMessageBuffer(Command.makeReadChargingOver());
			break;
		case R.id.main_date_of_manufacture:
//			showToast("产品生产日期");
			sendMessageBuffer(Command.makeReadProductionDate());
			break;
		case R.id.bluetooth_collect:// 蓝牙连接方式
			connectType = 2;
			collect_layout.setVisibility(View.GONE);
			wifi_layout.setVisibility(View.GONE);
			bluetooth_layout.setVisibility(View.VISIBLE);
			bluetoothService = new BluetoothService(this, myHandler);
	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (mBluetoothAdapter == null) {
	            Toast.makeText(this, "蓝牙功能不可用", Toast.LENGTH_LONG).show();
	        }
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        }else{
	        	if(!"".equals(preference.getDeviceNameKey())){
	        		connectBluetooth(preference.getDeviceNameKey());
	        	}
	        }
			break;
		case R.id.wifi_collect: // Wifi连接方式
			connectType = 1;
			collect_layout.setVisibility(View.GONE);
			wifi_layout.setVisibility(View.VISIBLE);
			bluetooth_layout.setVisibility(View.GONE);
			wifiService = new WifiService(hMsg, this);
			break;
		case R.id.collect_equipment: //连接设备
			if (wifiService.startConn("10.10.100.254", 8899)) {
    			isConnect = true;
    			isFirst = false;
				showToast("连接成功");
//    			sendMessageBuffer(Command.makeReadBatteryVoltage());
			} else {
				isConnect = false;
				showToast("连接失败,请连接好设备");
			}
			break;
		case R.id.collect_wifi: //连接网络
			startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			break;
		case R.id.collect_blutooth: //连接蓝牙
			startActivityForResult(new Intent(this,DeviceListActivity.class),REQUEST_CONNECT_DEVICE);
			break;
		default:
			break;
		}
	}
	
	Handler hMsg = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			if (wifiService == null)
				return;
			if (msg.what == WifiService.RELAYOPT) {
////				dataProcess.sendrelayCmd(msg.arg1, msg.arg2);
//				byte[] test = new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x00,(byte) 0x03,(byte) 0x11,(byte) 0xf6,(byte) 0x02,(byte) 0x0d};
//				try {
//					wifiService.sendData(test);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

			} else if (msg.what == WifiService.RELAYCHK) {
//				if (ckeck) {
//					dataProcess.sendrelayCmd(5, 0);
//					this.sendEmptyMessageDelayed(dataProcess.RELAYCHK, 2000);
//				}
			} else if (msg.what == WifiService.CLOSETCP) {
				isConnect = false;
				wifiService.stopConn();
				if(isFirstConnect){
					isFirstConnect = false;
					myHandler.postDelayed(connectDevice, 1 * 1000);//重新连接
				}
			} else if (msg.what == WifiService.RELAYSTATE) {
//				setRelayState(msg.arg1);
			} else if (msg.what == WifiService.APPQUIT) {
			} else if (msg.what == WifiService.MESSAGE_READ) {
				byte[] readBuf = (byte[]) msg.obj;
                changeUI(readBuf/*,msg.arg1*/);
			}
		}
	};
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 10001 && resultCode == 20001){
			orderTime = data.getStringExtra("time");
			showToast("预计完成时间为："+orderTime.substring(0, 2)+"小时"+orderTime.substring(2, 4)+"分钟");
		}else if(requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK){
			  // Get the device MAC address
            String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // Get the BLuetoothDevice object
            preference.setDeviceNameKey(address);
            connectBluetooth(address);
//            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//            // Attempt to connect to the device
//    		bluetoothDia = ProgressDialog.show(this, 
//    				"正在连接蓝牙",									// 对话框显示标题
//    				"请耐心等待...");	
//            bluetoothDia.show();
//            bluetoothService.connect(device);
		}else if(requestCode == REQUEST_ENABLE_BT){
			if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
				if(!"".equals(preference.getDeviceNameKey())){
	        		connectBluetooth(preference.getDeviceNameKey());
	        	}
            } else {
            	showToast("请打开蓝牙发送功能");
            }
		}
	}
	BluetoothDevice device = null;
	private void connectBluetooth(String address) {
        device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
		bluetoothDia = ProgressDialog.show(this, 
				"正在连接蓝牙",									// 对话框显示标题
				"请耐心等待...");	
        bluetoothDia.show();
        bluetoothService.connect(device);
	}
	  /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessageBuffer(byte[] message) {
    	if(connectType == 1){
    		try {
    			System.out.println("Wifi写：   "+CommUtil.printHexString(message));
				wifiService.sendData(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		// Check that we're actually connected before trying anything
    		if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
    			isConnect = false;
    			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		// Check that there's actually something to send
//        if (message.length() > 0) {
    		// Get the message bytes and tell the BluetoothChatService to write
//            byte[] send = message.getBytes();
    		bluetoothService.write(message);
//        }
    	}
    }
    
//    /** 字符转数组 */
//    private static byte[] HexString2Bytes(String hexstr) {
//	    byte[] b = new byte[hexstr.length() / 2];
//	    int j = 0;
//	    for (int i = 0; i < b.length; i++) {
//	        char c0 = hexstr.charAt(j++);
//	        char c1 = hexstr.charAt(j++);
//	        b[i] = (byte) ((parse(c0) << 4) | parse(c1));
//	    }
//	    return b;
//	}
//    private static int parse(char c) {
//	    if (c >= 'a')
//	        return (c - 'a' + 10) & 0x0f;
//	    if (c >= 'A')
//	        return (c - 'A' + 10) & 0x0f;
//	    return (c - '0') & 0x0f;
//	}
	// Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
	Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                 Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
//                    mConversationArrayAdapter.clear();
                    bluetoothDia.dismiss();
                	System.out.println("MainActivity.enclosing_method()     connected: ");
//            		getValue();
//                	isAutomation = true;
                	isConnect = true;
        			isFirst = false;
//        			sendMessageBuffer(Command.makeReadBatteryVoltage());
            		showToast("蓝牙连接成功，正在获取数据");
                    break;
                case BluetoothService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                	System.out.println("MainActivity.enclosing_method()     connecting... ");
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    bluetoothDia.dismiss();
            		showToast("蓝牙连接失败，请重新连接");
                	System.out.println("MainActivity.enclosing_method()     not connected ");
//                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
//                String readMessage = new String(readBuf, 0, msg.arg1);
                changeUI(readBuf/*,msg.arg1*/);
//                System.out.println(CollectorFactory.makeCollector(readBuf));
//                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
            	isConnect = false;
            	if(isFirstConnect){
            		isFirstConnect = false;
            		myHandler.postDelayed(connectDevice, 1 * 1000);//重新连接
            	}
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }

    };
    
	
    private void changeUI(byte[] readBuf/*,int len*/) {
    	String result = CollectorFactory.makeCollector(readBuf/*,len*/);
    	if(result == null){
//    		System.out.println("失败:  "+CommUtil.printHexString(readBuf));
//    		showToast("获取失败，请重新发送指令,收到的指令："+CommUtil.printHexString(readBuf/*,len*/));
//    		showToast("获取失败，请重新发送指令");
    	}else{
    		if(readBuf[2] == Command.WRITE){
    			switch (readBuf[3]) {//暂停  B5F30200010003  //充电  B5F30200010104   //预约时间  B5F3020102030F17 
    			case Command.OPERATION://预约时间  B5F3020102030F17 
    				showToast("操作成功");
    			break;
    			case Command.APPOINTMENT://预约时间  B5F3020102030F17 
    				showToast("预约时间设置成功");
    			break;
    			}
    		}else{
//	    		showToast("获取成功,收到的指令："+CommUtil.printHexString(readBuf/*,len*/));
//	    		showToast("获取成功");
				sendMessageBuffer(readBuf);//返回我获取到的指令
	    		switch (readBuf[3]) {
	    		case Command.BATTERY_VOLTAGE://电池电压
	    			main_battery_voltage_value.setText(result+"V");
//	    			if(isAutomation){
	    				sendMessageBuffer(Command.makeReadChargingCurrent());
//	    			}
	    			break;
	    		case Command.CHARGING_CURRENT://读取充电电流
	    			main_charge_current_value.setText(result+"A");
//	    			if(isAutomation){
//	    				sendMessageBuffer(Command.makeReadChargingPerentage());
//	    			}
	    			break;
	    		case Command.CHARGING_PERCENTAGE://充电完成百分比 B5F3 +01 +06+01 +53+59
	    			main_already_over_value.setText(result+"%");
//	    			if(isAutomation){
//	    				sendMessageBuffer(Command.makeReadChargingTime());
//	    			}
	    			break;
	    		case Command.CHARGING_TIME://读取已经充电时间 B5F3 +01 +07+03 +11 +24 +35 +75  07为小时数，08为分钟数，09为秒数
	    			String charging_time_arr[] = result.split(",");
	    			main_already_charge_time_value.setText(charging_time_arr[0]+"小时"+charging_time_arr[1]+"分钟"+charging_time_arr[2]+"秒");
//	    			if(isAutomation){
//	    				sendMessageBuffer(Command.makeReadChargingOver());
//	    			}
	    			break;
	    		case Command.CHARGING_OVER://读取预计完成时间 B5F3 +01 +0A+02 +02 +24 +33 0A为小时数，0B为分钟数
	    			String charging_over_arr[] = result.split(",");
	    			main_estimated_time_of_completion_value.setText(charging_over_arr[0]+"小时"+charging_over_arr[1]+"分钟");
	    			if(/*isAutomation && */!isLook){
//	    				sendMessageBuffer(Command.makeReadProductionDate());
	    			}
	    			break;
	    		case Command.PRODUCTION_DATE://读取生产日期  B5F3 +01 +0C+04 +07DE+02 +15 +0d 0C-0D为年份数，0E为月份，0F为日
	    			String production_date_arr[] = result.split(",");
	    			isLook = true;
	    			main_date_of_manufacture_value.setText(production_date_arr[0]+"年"+production_date_arr[1]+"月"+production_date_arr[2]+"日");
	    			break;
	    		default:
	    			break;
	    		}
    		}
    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    private long mExitTime;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				mExitTime = System.currentTimeMillis();
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			} else {
				this.finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
