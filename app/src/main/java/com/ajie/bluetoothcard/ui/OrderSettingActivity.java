package com.ajie.bluetoothcard.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class OrderSettingActivity extends BaseActivity {
    private Button bt1;
    private EditText edit_minute,edit_hour;
    private String orderTime = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.dialog_set);
		bt1 =  (Button) findViewById(R.id.btn_01);
		bt1.setOnClickListener(this);
		edit_hour =  (EditText) findViewById(R.id.edit_hour);
		edit_minute =  (EditText) findViewById(R.id.edit_minute);
		orderTime = getIntent().getStringExtra("orderTime");
        if(orderTime!=null&&!"".equals(orderTime)){
        	edit_hour.setText(orderTime.substring(0, 2));
        	edit_minute.setText(orderTime.substring(2, 4));
        }
        edit_hour.setSelection(2);
    }
    
    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	super.onClick(v);
    	switch (v.getId()) {
		case R.id.btn_01:
			  String time_hour = edit_hour.getText().toString();
			  String time_minute = edit_minute.getText().toString();
			  String time = time_hour+time_minute;
			  if(time_hour.length()!=2||!time_hour.matches("\\d+")){
				  showToast("小时输入格式为2位数字，如：03");
			  }else if(time_minute.length()!=2||!time_minute.matches("\\d+")){
				  showToast("分钟输入格式为2位数字，如：15");
			  }else{
				  if(Integer.parseInt(time.substring(0, 2))>=24){
					  showToast("前两位数要小于24");
					  return;
				  }
				  if(Integer.parseInt(time.substring(2, 4))>=60){
					  showToast("后两位数要小于60");
					  return;
				  }
				  Intent intent = new Intent();
				  intent.putExtra("time", time);
				  setResult(20001, intent);
				  finish();
			  }
			break;

		default:
			break;
		}
    }
}
