package com.ajie.bluetoothcard.widget;


import com.ajie.bluetoothcard.ui.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class OrderSetting extends Dialog implements OnClickListener{
	protected OnClickListener mOkClickListener;
	protected View mRootView,bt1;
	EditText edit_time;

	public OrderSetting(Context context) {
		super(context);
		initView();
	}
	
	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_set);
		mRootView = findViewById(R.id.rootView);
		mRootView = findViewById(R.id.rootView);
		mRootView.setBackgroundDrawable(new ColorDrawable(0x0000ff00));
		bt1 =  findViewById(R.id.btn_01);
		bt1.setOnClickListener(this);
		
		
		
		Window dialogWindow = getWindow();       
		ColorDrawable dw = new ColorDrawable(0x0000ff00);
	    dialogWindow.setBackgroundDrawable(dw);
	}
	
	
	public void setOnOKButtonListener(OnClickListener onClickListener) {
		mOkClickListener = onClickListener;
	}
	
	
	
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_01:
			onButtonOK();
			break;
		}
	}
	
	protected void onButtonOK(){
		dismiss();
		if (mOkClickListener != null){
			mOkClickListener.onClick(this, 0);
		}
	}
}
