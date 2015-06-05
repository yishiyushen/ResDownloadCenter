package com.noahedu.resdownloadcenter.customview;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.util.Debug;

public class DownProgessButton extends View {

	
	public enum STATE{
		STATE_CLICK_DOWN,
		STATE_DOWNING,
		STATE_PAUSE,
		STATE_OPEN,
	};
	
	private STATE state;
	
	public STATE getState()
	{
		return state;
	}

	public void setState(STATE state)
	{
		this.state = state;
		requestLayout();
		
	}
	
	private Bitmap clickDownBmp;
	private Bitmap downingGrayBmp;
	private Bitmap downingLiteBmp;
	private Bitmap pauseGrayBmp;
	private Bitmap pauseLiteBmp;
	private Bitmap openPressedBmp;
	private Bitmap openNomalBmp;
	private Path path;

	private int width;
	private int height;
	private RectF rect;
	private float sweepAngle;
	private final long period = 100;
	private final float circle = 360f;
	private boolean bLeftDown = false;
	private DownProgessButtonClickListen listen;
	private boolean bclicked = false;
	
	public DownProgessButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public DownProgessButton(Context context){
		super(context);
		init();
	}
	
	private void init(){
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		state = STATE.STATE_CLICK_DOWN;
		bclicked = false;
		Resources r = this.getResources();
		if(r != null){
			clickDownBmp = BitmapFactory.decodeResource(r, R.drawable.click_down);
			downingGrayBmp = BitmapFactory.decodeResource(r, R.drawable.pause01);
			downingLiteBmp = BitmapFactory.decodeResource(r, R.drawable.pause02);
			pauseGrayBmp = BitmapFactory.decodeResource(r, R.drawable.play01);
			pauseLiteBmp = BitmapFactory.decodeResource(r, R.drawable.play02);
			openPressedBmp = BitmapFactory.decodeResource(r, R.drawable.open_pressed);
			openNomalBmp = BitmapFactory.decodeResource(r, R.drawable.open_nomal);
		}
		
		if(downingGrayBmp != null){
			width = downingGrayBmp.getWidth();
			height = downingGrayBmp.getHeight();
			rect = new RectF(0, 0, width, height);
		}
		
		path = new Path();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG));
		if(state == STATE.STATE_DOWNING){
			if(path != null){
				canvas.drawBitmap(downingGrayBmp, 0, 0, null);
				path.reset();
				path.moveTo(width / 2, height / 2);
				path.arcTo(rect, 270f, sweepAngle);
				path.close();

				canvas.save();
				canvas.clipPath(path);
				canvas.drawBitmap(downingLiteBmp, 0, 0, null);
				canvas.restore();
			}
		}else if(state == STATE.STATE_PAUSE){
			if(path != null){
				canvas.drawBitmap(pauseGrayBmp, 0, 0, null);
				path.reset();
				path.moveTo(width / 2, height / 2);
				path.arcTo(rect, 270f, sweepAngle);
				path.close();

				canvas.save();
				canvas.clipPath(path);
				canvas.drawBitmap(pauseLiteBmp, 0, 0, null);
				canvas.restore();
			}
		}else if(state == STATE.STATE_CLICK_DOWN){
			//Debug.debugLog("------------on Draw()--------------");
			canvas.drawBitmap(clickDownBmp, 0,0,null);
			
		}else if(state == STATE.STATE_OPEN){
			if(bLeftDown){
				canvas.drawBitmap(openPressedBmp, 0, 0, null);
			}else{
				canvas.drawBitmap(openNomalBmp, 0, 0, null);
			}
		}
	}
	
	public void update(int curDownSize, int totalSize){
		if(state == STATE.STATE_DOWNING||state == STATE.STATE_PAUSE){
			sweepAngle = circle*curDownSize/totalSize;
			//Debug.debugLog("curDownSize= "+curDownSize+"  totalSize= "+totalSize);
			if(curDownSize == totalSize&&totalSize != 0){
				state = STATE.STATE_OPEN;
				requestLayout();
			}
			//Debug.debugLog(" sweepAngle= "+sweepAngle);
			postInvalidate();
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(state == STATE.STATE_DOWNING||state == STATE.STATE_PAUSE){
			if(downingGrayBmp != null){
				width = downingGrayBmp.getWidth();
				height = downingGrayBmp.getHeight();
				rect = new RectF(0, 0, width, height);
			}
		}else if(state == STATE.STATE_CLICK_DOWN){
			if(clickDownBmp != null){
				width = clickDownBmp.getWidth();
				height = clickDownBmp.getHeight();
				rect = new RectF(0, 0, width, height);
			}
		}else if(state == STATE.STATE_OPEN){
			if(openNomalBmp != null){
				width = openNomalBmp.getWidth();
				height = openNomalBmp.getHeight();
				rect = new RectF(0, 0, width, height);
			}
		}
		setMeasuredDimension(width, height);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			doAction(true);
			break;
		case MotionEvent.ACTION_UP:
			doAction(false);
			break;
		case MotionEvent.ACTION_CANCEL:
			bLeftDown = false;
			postInvalidate();
			break;
		}
		return true;
	}
	
	public boolean isBclicked() {
		return bclicked;
	}

	public void setBclicked(boolean bclicked) {
		this.bclicked = bclicked;
	}

	private void doAction(boolean down){
		bLeftDown = down;
		bclicked = true;
		if(down){
			if(state == STATE.STATE_DOWNING){
				
				if(listen != null){
					listen.onClick(this, state);
				}
				state = STATE.STATE_PAUSE;
				requestLayout();
			}else if(state == STATE.STATE_PAUSE){
				if(listen != null){
					listen.onClick(this, state);
				}
				state = STATE.STATE_DOWNING;
				requestLayout();
			}else if(state == STATE.STATE_CLICK_DOWN){
				
				if(listen != null){
					listen.onClick(this, state);
				}
				state = STATE.STATE_DOWNING;
				requestLayout();
			}else if(state == STATE.STATE_OPEN){
				postInvalidate();
			}
		}else{
			if(state == STATE.STATE_OPEN){
				if(listen != null){
					listen.onClick(this, state);
				}
				
			}
			postInvalidate();
		}

	}
	
	
	public void setClickListen(DownProgessButtonClickListen listen) {
		this.listen = listen;
	}


	public interface DownProgessButtonClickListen{
		public void onClick(View view, STATE state);
	}
	
}
