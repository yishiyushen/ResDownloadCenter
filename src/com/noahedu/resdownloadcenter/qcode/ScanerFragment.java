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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.common.BitMatrix;
import com.noahedu.resdownloadcenter.qcode.camera.CameraManager;
import com.noahedu.resdownloadcenter.qcode.result.ResultHandler;
import com.noahedu.resdownloadcenter.qcode.result.ResultHandlerFactory;
import com.noahedu.resdownloadcenter.util.Debug;

import com.noahedu.resdownloadcenter.R;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class ScanerFragment extends Fragment implements SurfaceHolder.Callback {

	private static final String TAG = ScanerFragment.class.getSimpleName();

	private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;

	private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	private static final Collection<ResultMetadataType> DISPLAYABLE_METADATA_TYPES =
			EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
					ResultMetadataType.SUGGESTED_PRICE,
					ResultMetadataType.ERROR_CORRECTION_LEVEL,
					ResultMetadataType.POSSIBLE_COUNTRY);

	public static final String EXTRA_ISBN = "isbn";
	public static final String EXTRA_CAPTURE = "capture";

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private View resultView;
	private Result lastResult;
	private boolean hasSurface;
	private IntentSource source;
	private String sourceUrl;
	private ScanFromWebPageManager scanFromWebPageManager;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType,?> decodeHints;
	private String characterSet;
//	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;

	private SurfaceView previewSurfaceView;
	private Bitmap mCapture;
	private String mCode;

	private boolean firstShowPreview;

	private Toast mToast;
	
	private IGetISBNStringCallBack getISBNCallBk;
	
	public void setIGetISBNStringListen(IGetISBNStringCallBack listen){
		getISBNCallBk = listen;
	}
	
	public interface IGetISBNStringCallBack{
		public void importISBNString(String isbn);
	}

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		hasSurface = false;
//		inactivityTimer = new InactivityTimer(getActivity());
		beepManager = new BeepManager(getActivity());
		ambientLightManager = new AmbientLightManager(getActivity());
		mToast = Toast.makeText(getActivity(), "请扫描ISBN条形码", Toast.LENGTH_SHORT);
		
		PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.scaner_fragment, container, false);
		viewfinderView = (ViewfinderView) view.findViewById(R.id.viewfinder_view);
		previewSurfaceView = (SurfaceView) view.findViewById(R.id.preview_view);
		resultView = view.findViewById(R.id.result_view);
		
//		View edit = view.findViewById(R.id.edit_isbn);
//		edit.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(!firstShowPreview) {
//					firstShowPreview = true;
//					SurfaceHolder surfaceHolder = previewSurfaceView.getHolder();
//					if (hasSurface) {
//						// The activity was paused but not stopped, so the surface still exists. Therefore
//						// surfaceCreated() won't be called, so init the camera here.
//						initCamera(surfaceHolder);
//					}
//					restartPreviewAfterDelay(0);
//				} else {
//					cameraManager.startPreview();
//					restartPreviewAfterDelay(0);
//				}
//			}
//		});
		
//		View confirm = view.findViewById(R.id.complete_isbn);
//		confirm.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Uri uri = getISBNCacheUri();				
//				Utils.saveBitmap2File(mCapture, uri.getPath());
//				
//				Intent data = new Intent();
//				data.putExtra(QuestionSourceActivity.EXTRA_SOURCE,
//						new QuestionSource(QuestionSource.TYPE_ISBN, mCode, uri));
//				getActivity().setResult(Activity.RESULT_OK, data);
//				getActivity().finish();
//			}
//		});

		hasSurface = false;
		firstShowPreview = true;
//		inactivityTimer = new InactivityTimer(getActivity());
		
		Bundle args = getArguments();
		if(args != null) {
//			previewSurfaceView.setVisibility(View.INVISIBLE);
			viewfinderView.setVisibility(View.GONE);		
			resultView.setVisibility(View.VISIBLE);
			
			String text = args.getString(EXTRA_ISBN);
			Uri uri = args.getParcelable(EXTRA_CAPTURE);
			
			mCode = text;
			mCapture = BitmapFactory.decodeFile(uri.getPath());
			
			ImageView barcodeImageView = (ImageView) view.findViewById(R.id.barcode_image_view);			
			barcodeImageView.setImageURI(uri);
			
			//TextView contentsTextView = (TextView) view.findViewById(R.id.contents_text_view);
			//contentsTextView.setText(text);	
			if(getISBNCallBk != null){
				getISBNCallBk.importISBNString(text);
			}
			
			firstShowPreview = false;
		} else {
			resetStatusView();
		}
		
		return view;
	}
	
	public Uri getISBNCacheUri() {		
		return Uri.fromFile(new File(getActivity().getCacheDir(), "isbn"));
	}

	@Override
	public void onResume() {
		super.onResume();
		Debug.debugLog2();
		// CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
		// want to open the camera driver and measure the screen size if we're going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getActivity().getApplication());
		cameraManager.setManualFramingRect(420, 340);

		viewfinderView.setCameraManager(cameraManager);

		handler = null;
		lastResult = null;

		SurfaceHolder surfaceHolder = previewSurfaceView.getHolder();
		if (hasSurface ) {
			if(firstShowPreview) {
				// The activity was paused but not stopped, so the surface still exists. Therefore
				// surfaceCreated() won't be called, so init the camera here.
				initCamera(surfaceHolder);
			}
		} else {
			// Install the callback and wait for surfaceCreated() to init the camera.
			surfaceHolder.addCallback(this);
		}
		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

//		inactivityTimer.onResume();


		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;

		Set<BarcodeFormat> formats = EnumSet.noneOf(BarcodeFormat.class);
		formats.add(BarcodeFormat.EAN_13);
		
		decodeFormats = formats;
		
//		Intent intent = getActivity().getIntent();
//		if (intent != null) {
//
//			String action = intent.getAction();
//			String dataString = intent.getDataString();
//
//			if (Intents.Scan.ACTION.equals(action)) {
//
//				// Scan the formats the intent requested, and return the result to the calling activity.
//				source = IntentSource.NATIVE_APP_INTENT;
//				decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
//				decodeHints = DecodeHintManager.parseDecodeHints(intent);
//
//				if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
//					int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
//					int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
//					if (width > 0 && height > 0) {
//						cameraManager.setManualFramingRect(width, height);
//					}
//				}
//			} else if (dataString != null &&
//					dataString.contains("http://www.google") &&
//					dataString.contains("/m/products/scan")) {
//
//				// Scan only products and send the result to mobile Product Search.
//				source = IntentSource.PRODUCT_SEARCH_LINK;
//				sourceUrl = dataString;
//				decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;
//
//			} else if (isZXingURL(dataString)) {
//
//				// Scan formats requested in query string (all formats if none specified).
//				// If a return URL is specified, send the results there. Otherwise, handle it ourselves.
//				source = IntentSource.ZXING_LINK;
//				sourceUrl = dataString;
//				Uri inputUri = Uri.parse(dataString);
//				scanFromWebPageManager = new ScanFromWebPageManager(inputUri);
//				decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
//				// Allow a sub-set of the hints to be specified by the caller.
//				decodeHints = DecodeHintManager.parseDecodeHints(inputUri);
//
//			}
//
//			characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
//
//		}
	}
	

	private int getCurrentOrientation() {
		int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_90:
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		default:
			return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		}
	}

	private static boolean isZXingURL(String dataString) {
		if (dataString == null) {
			return false;
		}
		for (String url : ZXING_URLS) {
			if (dataString.startsWith(url)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
//		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceHolder surfaceHolder = previewSurfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
//		inactivityTimer.shutdown();
		super.onDestroy();
	}

	//  @Override
	//  public boolean onKeyDown(int keyCode, KeyEvent event) {
	//    switch (keyCode) {
	//      case KeyEvent.KEYCODE_BACK:
	//        if (source == IntentSource.NATIVE_APP_INTENT) {
	//          setResult(RESULT_CANCELED);
	//          finish();
	//          return true;
	//        }
	//        if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
	//          restartPreviewAfterDelay(0L);
	//          return true;
	//        }
	//        break;
	//      case KeyEvent.KEYCODE_FOCUS:
	//      case KeyEvent.KEYCODE_CAMERA:
	//        // Handle these events so they don't launch the Camera app
	//        return true;
	//      // Use volume up/down to turn on light
	//      case KeyEvent.KEYCODE_VOLUME_DOWN:
	//        cameraManager.setTorch(false);
	//        return true;
	//      case KeyEvent.KEYCODE_VOLUME_UP:
	//        cameraManager.setTorch(true);
	//        return true;
	//    }
	//    return super.onKeyDown(keyCode, event);
	//  }

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			if(firstShowPreview) {
				initCamera(holder);
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show the results.
	 *
	 * @param rawResult The contents of the barcode.
	 * @param scaleFactor amount by which thumbnail was scaled
	 * @param barcode   A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), rawResult);
		if(!BarcodeFormat.EAN_13.equals(rawResult.getBarcodeFormat())
				|| !ParsedResultType.ISBN.equals(resultHandler.getType())) {
			Log.i(TAG, "Wrong format or type: format=" + rawResult.getBarcodeFormat() 
					+ ", type=" + resultHandler.getType());
			
			mToast.cancel();
			mToast.show();

			restartPreviewAfterDelay(0);
			return;
		}
		
//		inactivityTimer.onActivity();
		lastResult = rawResult;
		
		if(mCapture != null) {
			mCapture.recycle();
		}
		mCapture = barcode;
		mCode = lastResult.getText();
		

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			// Then not from history, so beep/vibrate and we have an image to draw on
			beepManager.playBeepSoundAndVibrate();
//			drawResultPoints(barcode, scaleFactor, rawResult);
		}

		switch (source) {
		case NATIVE_APP_INTENT:
		case PRODUCT_SEARCH_LINK:
			handleDecodeExternally(rawResult, resultHandler, barcode);
			break;
		case ZXING_LINK:
			if (scanFromWebPageManager == null || !scanFromWebPageManager.isScanFromWebPage()) {
				handleDecodeInternally(rawResult, resultHandler, barcode);
			} else {
				handleDecodeExternally(rawResult, resultHandler, barcode);
			}
			break;
		case NONE:
			handleDecodeInternally(rawResult, resultHandler, barcode);
			break;
		}
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
	 *
	 * @param barcode   A bitmap of the captured image.
	 * @param scaleFactor amount by which thumbnail was scaled
	 * @param rawResult The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			} else if (points.length == 4 &&
					(rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
					rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and metadata
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					if (point != null) {
						canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
					}
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), 
					scaleFactor * a.getY(), 
					scaleFactor * b.getX(), 
					scaleFactor * b.getY(), 
					paint);
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {

		CharSequence displayContents = resultHandler.getDisplayContents();

		cameraManager.stopPreview();
		
//		previewSurfaceView.setVisibility(View.INVISIBLE);
		viewfinderView.setVisibility(View.GONE);		
		resultView.setVisibility(View.VISIBLE);

		ImageView barcodeImageView = (ImageView) getView().findViewById(R.id.barcode_image_view);
		if (barcode == null) {
			barcodeImageView.setImageBitmap(encode(displayContents.toString()));
		} else {
			barcodeImageView.setImageBitmap(barcode);
		}

		//    formatTextView.setText(rawResult.getBarcodeFormat().toString());
//		Log.i(TAG, "format:" + rawResult.getBarcodeFormat().toString());

		//    typeTextView.setText(resultHandler.getType().toString());
//		Log.i(TAG, "type:" + resultHandler.getType().toString());

		//    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		//    timeTextView.setText(formatter.format(new Date(rawResult.getTimestamp())));

		//    Map<ResultMetadataType,Object> metadata = rawResult.getResultMetadata();
		//    if (metadata != null) {
		//      StringBuilder metadataText = new StringBuilder(20);
		//      for (Map.Entry<ResultMetadataType,Object> entry : metadata.entrySet()) {
		//        if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
		//          metadataText.append(entry.getValue()).append('\n');
		//        }
		//      }
		//      if (metadataText.length() > 0) {
		//        metadataText.setLength(metadataText.length() - 1);       
		//      }
		//    }

//		TextView contentsTextView = (TextView) getView().findViewById(R.id.contents_text_view);
//		contentsTextView.setText(displayContents);	
		if(getISBNCallBk != null){
			getISBNCallBk.importISBNString(displayContents.toString());
		}
	}

	// Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
	private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {

		if (barcode != null) {
			viewfinderView.drawResultBitmap(barcode);
		}

		long resultDurationMS;
		if (getActivity().getIntent() == null) {
			resultDurationMS = DEFAULT_INTENT_RESULT_DURATION_MS;
		} else {
			resultDurationMS = getActivity().getIntent().getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS,
					DEFAULT_INTENT_RESULT_DURATION_MS);
		}

		if (resultDurationMS > 0) {
			String rawResultString = String.valueOf(rawResult);
			if (rawResultString.length() > 32) {
				rawResultString = rawResultString.substring(0, 32) + " ...";
			}
		}

		if (source == IntentSource.NATIVE_APP_INTENT) {

			// Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
			// the deprecated intent is retired.
			Intent intent = new Intent(getActivity().getIntent().getAction());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
			intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
			byte[] rawBytes = rawResult.getRawBytes();
			if (rawBytes != null && rawBytes.length > 0) {
				intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
			}
			Map<ResultMetadataType,?> metadata = rawResult.getResultMetadata();
			if (metadata != null) {
				if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) {
					intent.putExtra(Intents.Scan.RESULT_UPC_EAN_EXTENSION,
							metadata.get(ResultMetadataType.UPC_EAN_EXTENSION).toString());
				}
				Number orientation = (Number) metadata.get(ResultMetadataType.ORIENTATION);
				if (orientation != null) {
					intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
				}
				String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
				if (ecLevel != null) {
					intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
				}
				@SuppressWarnings("unchecked")
				Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
				if (byteSegments != null) {
					int i = 0;
					for (byte[] byteSegment : byteSegments) {
						intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
						i++;
					}
				}
			}
			sendReplyMessage(R.id.return_scan_result, intent, resultDurationMS);

		} else if (source == IntentSource.PRODUCT_SEARCH_LINK) {

			// Reformulate the URL which triggered us into a query, so that the request goes to the same
			// TLD as the scan URL.
			int end = sourceUrl.lastIndexOf("/scan");
			String replyURL = sourceUrl.substring(0, end) + "?q=" + resultHandler.getDisplayContents() + "&source=zxing";      
			sendReplyMessage(R.id.launch_product_query, replyURL, resultDurationMS);

		} else if (source == IntentSource.ZXING_LINK) {

			if (scanFromWebPageManager != null && scanFromWebPageManager.isScanFromWebPage()) {
				String replyURL = scanFromWebPageManager.buildReplyURL(rawResult, resultHandler);
				sendReplyMessage(R.id.launch_product_query, replyURL, resultDurationMS);
			}

		}
	}

	private void sendReplyMessage(int id, Object arg, long delayMS) {
		if (handler != null) {
			Message message = Message.obtain(handler, id, arg);
			if (delayMS > 0L) {
				handler.sendMessageDelayed(message, delayMS);
			} else {
				handler.sendMessage(message);
			}
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			cameraManager.startPreview();
			return;
		} 
		
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.app_decode));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(getActivity()));
		builder.setOnCancelListener(new FinishListener(getActivity()));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
//		previewSurfaceView.setVisibility(View.VISIBLE);
		resultView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
	
	private Bitmap encode(String contentsToEncode) {
		final int WHITE = 0xFFFFFFFF;
		final int BLACK = 0xFF000000;

		BitMatrix result;
		try {
			result = new MultiFormatWriter().encode(contentsToEncode, 
					BarcodeFormat.EAN_13, 320, 240);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
		
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}
