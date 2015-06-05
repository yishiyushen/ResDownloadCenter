package com.noahedu.resdownloadcenter.customview;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.noahedu.resdownloadcenter.R;

public class AutoSlipingLayout extends RelativeLayout {
	public static final String TAG = "AutoSlipingLayout";
	public static final int CLOSE_STATE = 0;
	public static final int OPEN_STATE = 1;
	public static final int SLIPING_OPEN_STATE =2;
	public static final int SLIPING_CLOSE_STATE =3;
	public static final int MOVE_SPEED = 8;
	
	private View leftView;
	private View rightView;
	private int leftViewX;
	private int initialX;
	private int curState = CLOSE_STATE;
	private int initialW;
	private boolean isLayout = false;
	private MyTimer mTimer;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			if(curState == SLIPING_OPEN_STATE){
				leftViewX += MOVE_SPEED;
				if(leftViewX>=0){
					leftViewX = 0;
					curState = OPEN_STATE;
					if(mTimer != null){
						mTimer.cancel();
					}
				}
			}else if(curState == SLIPING_CLOSE_STATE){
				leftViewX -= MOVE_SPEED;
				if(leftViewX <= -leftView.getMeasuredWidth()){
					leftViewX = initialX;
					curState = CLOSE_STATE;
					leftView.setVisibility(View.INVISIBLE);
					if(mTimer != null){
						mTimer.cancel();
					}
				}
			}
			requestLayout();
		};
	};
	
	
	public AutoSlipingLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public AutoSlipingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	private void init(Context context){
		initialW = (int) context.getResources().getDimension(R.dimen.menu_layout_with);
		mTimer = new MyTimer(mHandler);
		curState = CLOSE_STATE;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		if(getChildCount() == 0){
			return;
		}
		if(!isLayout){
			leftView = getChildAt(1);
			rightView = getChildAt(0);
			isLayout = true;
			leftViewX = initialW-leftView.getMeasuredWidth();
			initialX = leftViewX;
		}
		Log.e(TAG, "leftViewX="+leftViewX+"  leftView.getMeasuredWidth()+leftViewX= "+leftView.getMeasuredWidth()+leftViewX);
		leftView.layout(leftViewX, 0, leftView.getMeasuredWidth()+leftViewX, leftView.getMeasuredHeight());
		rightView.layout(0, 0, rightView.getMeasuredWidth(), rightView.getMeasuredHeight());		
	}
	
	public void startOpen(){
		if(curState == CLOSE_STATE){
			Log.e(TAG, "----startOpen()----");
			leftView.setVisibility(View.VISIBLE);
			curState=SLIPING_OPEN_STATE;
			if(mTimer != null){
				mTimer.cancel();
			}
			mTimer.schedule(5);
		}
	}
	
	public void startClose(){
		if(curState == OPEN_STATE){
			Log.e(TAG, "----startClose()----");
			curState=SLIPING_CLOSE_STATE;
			if(mTimer != null){
				mTimer.cancel();
			}
			mTimer.schedule(5);
		}
	}
	
	public void setState(int state){
		if(state == curState){
			return;
		}
		
		if(state == OPEN_STATE){
			leftViewX = 0;
		}else if(state == CLOSE_STATE){
			leftViewX = initialX;
		}else{
			return;
		}
		if(mTimer != null){
			mTimer.cancel();
		}
		curState = state;
		requestLayout();
	}
	
	class MyTimer {
		private Handler handler;
		private Timer timer;
		private MyTask mTask;

		public MyTimer(Handler handler) {
			this.handler = handler;
			timer = new Timer();
		}

		public void schedule(long period) {
			if (mTask != null) {
				mTask.cancel();
				mTask = null;
			}
			mTask = new MyTask(handler);
			timer.schedule(mTask, 0, period);
		}

		public void cancel() {
			if (mTask != null) {
				mTask.cancel();
				mTask = null;
			}
		}

		class MyTask extends TimerTask {
			private Handler handler;

			public MyTask(Handler handler) {
				this.handler = handler;
			}

			@Override
			public void run() {
				handler.obtainMessage().sendToTarget();
			}

		}
	}
	
	

}
