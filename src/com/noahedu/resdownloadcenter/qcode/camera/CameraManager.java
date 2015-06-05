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

package com.noahedu.resdownloadcenter.qcode.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.noahedu.resdownloadcenter.qcode.PlanarYUVLuminanceSource;
import com.noahedu.resdownloadcenter.qcode.camera.open.OpenCameraInterface;
import com.noahedu.resdownloadcenter.util.Debug;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

  private static final String TAG = CameraManager.class.getSimpleName();

  private static final int MIN_FRAME_WIDTH = 240;
  private static final int MIN_FRAME_HEIGHT = 240;
  private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
  private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

  private final Context context;
  private final CameraConfigurationManager configManager;
  private Camera camera;
  private AutoFocusManager autoFocusManager;
  private Rect framingRect;
  private Rect framingRectInPreview;
  private boolean initialized;
  private boolean previewing;
  private int requestedFramingRectWidth;
  private int requestedFramingRectHeight;
  private byte[] rotateData=null;
  private int rotateWidth = 0;
  private int rotateHeight = 0;
  /**
   * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
   * clear the handler so it will only receive one message.
   */
  private final PreviewCallback previewCallback;

  public CameraManager(Context context) {
    this.context = context;
    this.configManager = new CameraConfigurationManager(context);
    previewCallback = new PreviewCallback(configManager);
  }
  
  public void setScreenResolution(int w, int h){
	configManager.setScreenResolution(w, h);
  }
  /**
   * Opens the camera driver and initializes the hardware parameters.
   *
   * @param holder The surface object which the camera will draw preview frames into.
   * @throws IOException Indicates the camera driver failed to open.
   */
  public synchronized void openDriver(SurfaceHolder holder) throws IOException {
    Camera theCamera = camera;
    if (theCamera == null) {
      theCamera = OpenCameraInterface.open();
      if (theCamera == null) {
        throw new IOException();
      }
      camera = theCamera;
    }
    theCamera.setPreviewDisplay(holder);

    configManager.setCameraPreviewOrientation(OpenCameraInterface.getCameraId(), theCamera);
    
    if (!initialized) {
      initialized = true;
      configManager.initFromCameraParameters(theCamera);
      if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
        setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
        requestedFramingRectWidth = 0;
        requestedFramingRectHeight = 0;
      }
    }

    Camera.Parameters parameters = theCamera.getParameters();
    String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
    try {
      configManager.setDesiredCameraParameters(theCamera, false);
    } catch (RuntimeException re) {
      // Driver failed
      Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
      Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
      // Reset:
      if (parametersFlattened != null) {
        parameters = theCamera.getParameters();
        parameters.unflatten(parametersFlattened);
        try {
          theCamera.setParameters(parameters);
          configManager.setDesiredCameraParameters(theCamera, true);
        } catch (RuntimeException re2) {
          // Well, darn. Give up
          Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
        }
      }
    }
    
  }

  public synchronized boolean isOpen() {
    return camera != null;
  }

  /**
   * Closes the camera driver if still in use.
   */
  public synchronized void closeDriver() {
    if (camera != null) {
      camera.release();
      camera = null;
      // Make sure to clear these each time we close the camera, so that any scanning rect
      // requested by intent is forgotten.
      framingRect = null;
      framingRectInPreview = null;
    }
  }

  /**
   * Asks the camera hardware to begin drawing preview frames to the screen.
   */
  public synchronized void startPreview() {
    Camera theCamera = camera;
    if (theCamera != null && !previewing) {
      theCamera.startPreview();
      previewing = true;
      autoFocusManager = new AutoFocusManager(context, camera);
    }
  }

  /**
   * Tells the camera to stop drawing preview frames.
   */
  public synchronized void stopPreview() {
    if (autoFocusManager != null) {
      autoFocusManager.stop();
      autoFocusManager = null;
    }
    if (camera != null && previewing) {
      camera.stopPreview();
      previewCallback.setHandler(null, 0);
      previewing = false;
    }
  }

  /**
   * Convenience method for {@link com.noahedu.onlineanswer.qcode.ScanerFragment}
   */
  public synchronized void setTorch(boolean newSetting) {
    if (newSetting != configManager.getTorchState(camera)) {
      if (camera != null) {
        if (autoFocusManager != null) {
          autoFocusManager.stop();
        }
        configManager.setTorch(camera, newSetting);
        if (autoFocusManager != null) {
          autoFocusManager.start();
        }
      }
    }
  }

  /**
   * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
   * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
   * respectively.
   *
   * @param handler The handler to send the message to.
   * @param message The what field of the message to be sent.
   */
  public synchronized void requestPreviewFrame(Handler handler, int message) {
    Camera theCamera = camera;
    if (theCamera != null && previewing) {
      previewCallback.setHandler(handler, message);
      theCamera.setOneShotPreviewCallback(previewCallback);
    }
  }

  /**
   * Calculates the framing rect which the UI should draw to show the user where to place the
   * barcode. This target helps with alignment as well as forces the user to hold the device
   * far enough away to ensure the image will be in focus.
   *
   * @return The rectangle to draw on screen in window coordinates.
   */
  public synchronized Rect getFramingRect() {
    if (framingRect == null) {
      if (camera == null) {
        return null;
      }
      Point screenResolution = configManager.getScreenResolution();
      
      if (screenResolution == null) {
        // Called early, before init even finished
    	  Debug.debugLog2("screenResolution == null " );
        return null;
      }

      int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
      int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

      int leftOffset = (screenResolution.x - width) / 2;
      int topOffset = (screenResolution.y - height) / 2;
      framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
      Debug.debugLog2("Calculated framing rect: " + framingRect);
      Log.e(TAG, "Calculated framing rect: " + framingRect);
    }
    return framingRect;
  }
  
  public void resetFramingRect(){
	  framingRect = null;
  }
  
  private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
    int dim = 5 * resolution / 8; // Target 5/8 of each dimension
    if (dim < hardMin) {
      return hardMin;
    }
    if (dim > hardMax) {
      return hardMax;
    }
    return dim;
  }

  /**
   * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
   * not UI / screen.
   */
  public synchronized Rect getFramingRectInPreview() {
    if (framingRectInPreview == null) {
      Rect framingRect = getFramingRect();
      if (framingRect == null) {
        return null;
      }
      Rect rect = new Rect(framingRect);
      Point cameraResolution = configManager.getCameraResolution();
      Point screenResolution = configManager.getScreenResolution();
      if (cameraResolution == null || screenResolution == null) {
        // Called early, before init even finished
        return null;
      }
      int rotation = configManager.getDisplayOrientation();
      int crx = cameraResolution.x;
      int cry = cameraResolution.y;
      if (rotation==90 || rotation==270){
    	  crx = cameraResolution.y;
    	  cry = cameraResolution.x;
        }
      rect.left = rect.left * crx / screenResolution.x;
      rect.right = rect.right * crx / screenResolution.x;
      rect.top = rect.top * cry / screenResolution.y;
      rect.bottom = rect.bottom * cry / screenResolution.y;
      framingRectInPreview = rect;
    }
    return framingRectInPreview;
  }

  /**
   * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
   * them automatically based on screen resolution.
   *
   * @param width The width in pixels to scan.
   * @param height The height in pixels to scan.
   */
  public synchronized void setManualFramingRect(int width, int height) {
    if (initialized) {
      Point screenResolution = configManager.getScreenResolution();
      if (width > screenResolution.x) {
        width = screenResolution.x;
      }
      if (height > screenResolution.y) {
        height = screenResolution.y;
      }
      int leftOffset = (screenResolution.x - width) / 2;
      int topOffset = (screenResolution.y - height) / 2;
      framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
      Log.d(TAG, "Calculated manual framing rect: " + framingRect);
      framingRectInPreview = null;
    } else {
      requestedFramingRectWidth = width;
      requestedFramingRectHeight = height;
    }
  }

  /**
   * A factory method to build the appropriate LuminanceSource object based on the format
   * of the preview buffers, as described by Camera.Parameters.
   *
   * @param data A preview frame.
   * @param width The width of the image.
   * @param height The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    Rect rect = getFramingRectInPreview();
    if (rect == null) {
      return null;
    }
    setRotatedImageData(data, width, height);
    // Go ahead and assume it's YUV rather than die.
//    saveYUVtoPicture(rotateData, rotateWidth, rotateHeight, rect);
    return new PlanarYUVLuminanceSource(rotateData, rotateWidth, rotateHeight, rect.left, rect.top,
                                        rect.width(), rect.height(), false);
  }

  private void setRotatedImageData(byte[] data, int width, int height){
	  if (rotateData==null || rotateData.length<data.length){
		  rotateData = new byte[data.length];
	  }
	  
		rotateWidth = width;
		rotateHeight = height;
		    
		    switch(configManager.getDisplayOrientation())
		     {
		        case 0:
		        case 360:
					System.arraycopy(data, 0, rotateData, 0, data.length);		
		        	break;
		        case 90:
		    		rotateWidth = height;
		    		rotateHeight = width;
					rotateYuvData(rotateData, data, width, height, 0);
		        	break;
		        case 270:
		    		rotateWidth = height;
		    		rotateHeight = width;
					rotateYuvData(rotateData, data, width, height, 1);
		        	break;
		        case 180:
		        	rotateYuvData(rotateData, data,width, height, 2);
		        	break;
		     }
   }
	public void rotateYUV240SP_FlipY180(byte[] src,byte[] des,int width,int height) 
	{           
		int wh = width * height; 
		//旋转Y  
		int k = 0; 
		for(int i=0;i<height;i++) { 
		    for(int j=width-1; j>-1; j--)  
		    { 
			  des[k] = src[width*(height-i-1) + j];             
			  k++; 
		    } 
		}
		
		for(int i=0;i<height/2;i++) { 
		    for(int j=width-2; j>-1; j -=2)  
		    {    
			  des[k]   = src[wh + width*(height/2 -i-1) + j]; 
			  des[k+1] = src[wh + width*(height/2 -i-1) + j+1];     
			  k+=2; 
		    } 
		}
		 
	}

	 public void rotateYUV240SP_Rotate180(byte[] src,byte[] dest,int width,int height)  
	 {          
	        final int wh = width * height;
	        int i = height-1;
	        for(int j=0; j<height; j++)   
	           {
	        		System.arraycopy(src, j*width, dest, i*width, width); // y
	        		i --;
	           }
	        
	        i = height/2-1;
	        final int uvw = width;
	        for(int j=0; j<height/2; j++)   
	           {
	        		System.arraycopy(src, wh+j*uvw, dest, wh+i*uvw, uvw); 
	        		i --;
	           }
	 }
	
	 public void rotateYUV240SP_Clockwise(byte[] src,byte[] des,int width,int height)  
	 {          
	        int wh = width * height;  
	        //旋转Y   
	        int k = 0;  
	        for(int i=0;i<width;i++) {  
	            for(int j=0;j<height;j++)   
	            {  
	                  des[k] = src[width*(height-j-1) + i];              
	                  k++;  
	            }  
	        }  
	          
	        for(int i=0;i<width;i+=2) {  
	            for(int j=0;j<height/2;j++)   
	            {     
	                  des[k] = src[wh+ width*(height/2-j-1) + i];      
	                  des[k+1]=src[wh + width*(height/2-j-1) + i+1];  
	                  k+=2;  
	            }  
	        }            
	          
	 }
	 public void rotateYUV240SP_AntiClockwise(byte[] src,byte[] des,int width,int height)  
	 {  	         
	        int wh = width * height;  
	        //旋转Y   
	        int k = 0;  
	        for(int i=0;i<width;i++) {  
	            for(int j=0;j<height;j++)   
	            {  
	                  des[k] = src[width*j + width-i-1];              
	                  k++;  
	            }  
	        }  
	          
	        for(int i=0;i<width;i+=2) {  
	            for(int j=0;j<height/2;j++)   
	            {     
	                  des[k+1] = src[wh+ width*j + width-i-1];      
	                  des[k]=src[wh + width*j + width-(i+1)-1];  
	                  k+=2;  
	            }  
	        } 
	          
	}

	 //it works becuase in YCbCr_420_SP and YCbCr_422_SP, the Y channel is planar and appears first
  public void rotateYuvData(byte[] rotatedData, byte[] data, int width, int height,int nCase)
  {
  	if( nCase == 0)
  	{
  		rotateYUV240SP_Clockwise(data,rotatedData,width,height);
  	}
  	else if (nCase == 1)
  	{
  		rotateYUV240SP_AntiClockwise(data,rotatedData,width,height);
  	}
  	else
  	{
  		rotateYUV240SP_FlipY180(data,rotatedData,width,height);
  	}
  }

	public void saveYUVtoPicture(byte[] data,int width,int height, Rect rect){
		FileOutputStream outStream = null;
		File file = new File("/tmp/qrcode.jpg");
		if(!file.exists()){
			try {
				file.createNewFile();
				file.setWritable(true, false);
				file.setReadable(true, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		
		try {
			YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg(rect, 100, baos);
			Bitmap bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
			outStream = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.write(baos.toByteArray());
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}
  
}
