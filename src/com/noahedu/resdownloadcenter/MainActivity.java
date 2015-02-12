package com.noahedu.resdownloadcenter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.noahedu.resdownloadcenter.customview.AutoSlipingLayout;

public class MainActivity extends Activity {
	private AutoSlipingLayout mSlipLayout;
	private Button mManagerBt;
	private Button mFilterBt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView(){
		mSlipLayout = (AutoSlipingLayout)findViewById(R.id.auto_slip_layout);
		OnClickListen listen = new OnClickListen();
		mManagerBt = (Button)findViewById(R.id.manager_bt);
		mFilterBt = (Button)findViewById(R.id.filter_bt);
		mManagerBt.setOnClickListener(listen);
		mFilterBt.setOnClickListener(listen);
	}
	
	private class OnClickListen implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.manager_bt:
				mSlipLayout.startOpen();
				break;
			case R.id.filter_bt:
				mSlipLayout.startClose();
				break;
			}
			
		}
	}
}
