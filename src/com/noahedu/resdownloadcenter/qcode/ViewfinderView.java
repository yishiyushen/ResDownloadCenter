/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noahedu.resdownloadcenter.qcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.noahedu.resdownloadcenter.qcode.camera.CameraManager;
import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.resdownloadcenter.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
	private static final long ANIMATION_DELAY = 50L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final int POINT_SIZE = 6;

	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private final int laserColor;
	private int scannerAlpha;
	private int scannerY;
	private Drawable mFrameDrawable;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize these once for performance rather than calling them every time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = getResources();
		mFrameDrawable = resources.getDrawable(R.drawable.scan_frame);
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);
		laserColor = resources.getColor(R.color.viewfinder_laser);
		scannerAlpha = 0;
		scannerY = 0;
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		Debug.debugLog("----w= "+w+"  ----h= "+h);
		cameraManager.setScreenResolution(w, h);
		cameraManager.resetFramingRect();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = cameraManager.getFramingRect();
		
		Rect previewFrame = cameraManager.getFramingRectInPreview();    
		if (frame == null || previewFrame == null) {
			return;
		}
		Debug.debugLog(" left="+frame.left+" top="+frame.top);
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		mFrameDrawable.setBounds(frame);
		mFrameDrawable.draw(canvas);
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);				
		paint.setTextSize(24);
		paint.setColor(Color.WHITE);
		paint.setTextAlign(Paint.Align.CENTER);
		
		canvas.drawText("把书本条形码放入框中,即可自动扫描", 
				frame.centerX(), 
				frame.bottom + paint.getFontSpacing() * 3 / 2, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(CURRENT_POINT_OPACITY);
			canvas.drawBitmap(resultBitmap, null, frame, paint);
			scannerY = 0;
		} else {

			// Draw a red "laser scanner" line through the middle to show decoding is active
			paint.setColor(laserColor);
//			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
			paint.setAlpha(255);
			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
			int middle = frame.height() / 2 + frame.top;
			scannerY += 6;
			if (scannerY >= frame.height()){
				scannerY = 0;
			}
			middle = frame.top + scannerY;
			canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
			// Request another update at the animation interval, but only repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY,
					frame.left - POINT_SIZE,
					frame.top - POINT_SIZE,
					frame.right + POINT_SIZE,
					frame.bottom + POINT_SIZE);
		}
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live scanning display.
	 *
	 * @param barcode An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}
}
