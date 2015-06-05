package com.noahedu.resdownloadcenter.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
/**
 * 网络图片下载缓存工具类
 * @author zhonglh
 *
 */
public class LoadImgUtil {
	/** 最大并行线程数 */
	private static final int Max = 5;
	/** java 自带的线程池 */
	private ExecutorService threadPools = null;

	private LruCacheUtil<Bitmap> mMemoryCache;

	private DiskLruCacheUtil mDiskCache;
	
	private static LoadImgUtil loadImgUtil;

	private LoadImgUtil(Context context, String uniqueName) {
		// TODO Auto-generated constructor stub
		mMemoryCache = new LruCacheUtil<Bitmap>();
		mDiskCache = new DiskLruCacheUtil(context);
		mDiskCache.open(uniqueName);
	}
	
	public static LoadImgUtil getSingleInstance(Context context, String uniqueName){
		if(loadImgUtil == null){
			loadImgUtil = new LoadImgUtil(context, uniqueName);
		}
		return loadImgUtil;
	}

	public Bitmap loadImage(final ImageView imageView, final String imageUrl,final int w,final int h,
			final ImageDownloadCallBack imageDownloadCallBack) {
		Bitmap bitmap = null;
		/** 从内存缓存中读取 */
		bitmap = mMemoryCache.getBitmapFromMemoryCache(imageUrl);
		
		if (bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h);
			bitmap = getRoundedCornerBitmap(bitmap ,10);
			return bitmap;
		}
		/** 从磁盘缓存中读取 */
		InputStream in = mDiskCache.readFromDiskCache(imageUrl);
		if (in != null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			bitmap = BitmapFactory.decodeStream(in, null, options);
			
			if (bitmap != null) {
				bitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h);
				bitmap = getRoundedCornerBitmap(bitmap ,10);
				return bitmap;
			}
		}

		if (imageUrl != null && !"".equals(imageUrl)) {
			if (threadPools == null) {
				threadPools = Executors.newFixedThreadPool(Max);
			}

			final Handler hand = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					if (msg.what == 111 && imageDownloadCallBack != null) {
						Bitmap bit = (Bitmap) msg.obj;
						imageDownloadCallBack.onImageDownload(imageView, bit);
					}
					super.handleMessage(msg);
				}
			};

			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					InputStream in = getInputStreamFromURL(imageUrl);
					if(in != null){
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 2;
						Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
						mMemoryCache.addBitmapToMemoryCache(imageUrl, bitmap);
						mDiskCache.writeToDiskCache(imageUrl,in);
						Message msg = hand.obtainMessage();
						bitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h);
						bitmap = getRoundedCornerBitmap(bitmap ,10);
						msg.what = 111;
						msg.obj = bitmap;
						hand.sendMessage(msg);
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

	//				loadBitmapFromNet(imageUrl,hand);
				}
			});

			threadPools.execute(thread);
		}

		return bitmap;
	}
	
	public void executeThread(Thread thread){
		if (threadPools == null) {
			threadPools = Executors.newFixedThreadPool(Max);
		}
		threadPools.execute(thread);
	}
	
	public void flushDiskCache(){
		if(mDiskCache != null){
			mDiskCache.flush();
		}
	}
	
	public void closeDiskCache(){
		if(mDiskCache != null){
			mDiskCache.close();
			mDiskCache = null;
		}
		
		if(mMemoryCache != null){
			mMemoryCache.clearCache();
			mMemoryCache = null;
		}
		loadImgUtil = null;
	}
	


	private InputStream getInputStreamFromURL(String url) {
		Log.e("-------", "url= "+url);
		HttpGet httpGet = new HttpGet(url);
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 30 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 30 * 1000);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		try {
			HttpResponse res = httpClient.execute(httpGet);
			if (res.getStatusLine().getStatusCode() == 200) {
				return res.getEntity().getContent();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void loadBitmapFromNet(String imageUrl,Handler hand){
		HttpURLConnection urlConnection = null;  
		InputStream in = null;
        try {  
            final URL url = new URL(imageUrl);  
            urlConnection = (HttpURLConnection) url.openConnection();  
            in = urlConnection.getInputStream();
			if(in != null){
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
				mMemoryCache.addBitmapToMemoryCache(imageUrl, bitmap);
				mDiskCache.writeToDiskCache(imageUrl,in);
				Message msg = hand.obtainMessage();
				msg.what = 111;
				msg.obj = bitmap;
				hand.sendMessage(msg);
			}
        } catch (final IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (urlConnection != null) {  
                urlConnection.disconnect();  
            }  
            try {  

                if (in != null) {  
                    in.close();  
                }  
            } catch (final IOException e) {  
                e.printStackTrace();  
            }  
        }  
	}

	public interface ImageDownloadCallBack {
		public void onImageDownload(ImageView imageview, Bitmap bitmap);
	}
	
    //获得圆角图片的方法  
   public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx){  
         
	   if(bitmap == null){
		   return null;
	   }
       Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap  
               .getHeight(), Config.ARGB_8888);  
       Canvas canvas = new Canvas(output);  
  
       final int color = 0xff424242;  
       final Paint paint = new Paint();  
       final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
       final RectF rectF = new RectF(rect);  
  
       paint.setAntiAlias(true);  
       canvas.drawARGB(0, 0, 0, 0);  
       paint.setColor(color);  
       canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
  
       paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
       canvas.drawBitmap(bitmap, rect, rect, paint);  
  
       return output;  
   } 

}
