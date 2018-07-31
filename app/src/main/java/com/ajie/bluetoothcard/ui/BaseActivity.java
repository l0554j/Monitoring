package com.ajie.bluetoothcard.ui;

import java.util.HashMap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class BaseActivity extends Activity implements OnClickListener{
	protected View btn_next,backBtn;
	protected ImageView right_icon;
	protected TextView top_title_right;
	protected static HashMap<String, BluetoothDevice> BluetoothDevicesALL = new HashMap<String, BluetoothDevice>();
	protected static  BluetoothAdapter mBtAdapter;
	/**
	 * 头部操作，点击操作的时候，返回前一个页面
	 * 
	 * @param titleStr
	 *            顶部的名称 id
	 */
	protected void showTop(int titleStr, Boolean isVisible) {
		backBtn =  findViewById(R.id.btn_back);
		btn_next =  findViewById(R.id.btn_next);
		right_icon =  (ImageView) findViewById(R.id.right_icon);
		top_title_right =  (TextView) findViewById(R.id.top_title_right);
		btn_next.setOnClickListener(this);
		TextView topTitel = (TextView) findViewById(R.id.top_title);
//		if (null == titleStr) {
//			titleStr = getString(R.string.app_name);
//		}
		topTitel.setText(titleStr);

		if (isVisible) {
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void initView() {
		
	}

	@Override
	public void onClick(View v) {
		
	}
	
	
	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
