package com.noahedu.resdownloadcenter.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.customview.DownProgessButton;
import com.noahedu.resdownloadcenter.customview.DownProgessButton.DownProgessButtonClickListen;
import com.noahedu.resdownloadcenter.customview.DownProgessButton.STATE;
import com.noahedu.resdownloadcenter.customview.IDownLoadListen;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.search.StaticVar;
import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.resdownloadcenter.util.LoadImgUtil;
import com.noahedu.resdownloadcenter.util.LoadImgUtil.ImageDownloadCallBack;

public class MainListViewAdapter extends BaseAdapter {
	private LoadImgUtil loadImgUtil;
	private LayoutInflater inflater;
	private ArrayList<Key> keyList;
	private OnClickListener listen;
	private Context context;
	private IDownLoadListen downLoadListen;
	private View parent;
	private SparseArray<View> views;
	private static final int MSG_UPDATE = 1;
	private Handler handler;
	private boolean pauseAll;
	
	public MainListViewAdapter(Context context) {
		// TODO Auto-generated constructor stub
		loadImgUtil = LoadImgUtil.getSingleInstance(context,
				StaticVar.BOOK_COVER_IMG_DIR);
		inflater = LayoutInflater.from(context);
		this.context = context;
		views = new SparseArray<View>();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_UPDATE:
					handler.removeMessages(msg.what);
					doUpdate();
				//	doUpdate();
					break;
				// case MSG_DOWNLOADALL:
				// doDownloadAll();
				// break;
				}
			}
		};
	}
	
	public LoadImgUtil getLoadImgUtil(){
		return loadImgUtil;
	}

	public void setKeyList(ArrayList<Key> keyList) {
		this.keyList = keyList;
		views.clear();
	}

	public void setListen(OnClickListener listen) {
		this.listen = listen;
	}
	
	public void setIDownListen(IDownLoadListen downLoadListen){
		this.downLoadListen = downLoadListen;
	}
	
	public void updateAll(){
		
		if (views == null)
			return;
		for (int i = 0; i < views.size(); i++) {
			View rootView = views.get(i);

			// 如果当前ListView不可见，则不更新Item
			if (parent == null || !parent.isShown())
				return;

			// 如果当前Item不可见，则不更新Item
			if (rootView == null || !rootView.isShown())
				continue;

			Key key = null;
			Object tag = rootView.getTag();
			
			if (tag != null && tag instanceof ViewHolder)
			{
				ViewHolder holder = (ViewHolder)tag;
				Object tag2 = holder.downBtImg.getTag();
				if(tag2 != null && tag2 instanceof Key){
					key = (Key)tag2;
				}else{
					return;
				}
			}
			else
				return;

			if(downLoadListen != null){
				downLoadListen.update(key);
			}
		}

		handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE), 100);
	}
	
	public void doUpdate(){
		if (views == null)
			return;
		for (int i = 0; i < views.size(); i++) {
			View rootView = views.get(i);

			// 如果当前ListView不可见，则不更新Item
			if (parent == null || !parent.isShown())
				return;

			// 如果当前Item不可见，则不更新Item
			if (rootView == null || !rootView.isShown())
				continue;

			Key key = null;
			Object tag = rootView.getTag();
			
			if (tag != null && tag instanceof ViewHolder)
			{
				ViewHolder holder = (ViewHolder)tag;
				Object tag2 = holder.downBtImg.getTag();
				if(tag2 != null && tag2 instanceof Key){
					key = (Key)tag2;
					initDownBt(key, holder.downBtImg,holder.tipInfo);
				}else{
					return;
				}
			}
			else
				return;

		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (keyList == null) {
			return 0;
		}
		return keyList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (keyList == null) {
			return null;
		}
		return keyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (parent != null)
			this.parent = parent;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.main_list_view_item, null);
			holder.bookCover = (ImageView) convertView
					.findViewById(R.id.book_cover_img);
			holder.downBtImg = (DownProgessButton) convertView
					.findViewById(R.id.down_bt);
			holder.bookName = (TextView) convertView
					.findViewById(R.id.book_name_tv);
			holder.subject = (TextView) convertView
					.findViewById(R.id.subject_tv);
			holder.grade = (TextView) convertView.findViewById(R.id.grade_tv);
			holder.press = (TextView) convertView.findViewById(R.id.press_tv);
			holder.size = (TextView) convertView.findViewById(R.id.size_tv);
			holder.bookLayout = (RelativeLayout) convertView
					.findViewById(R.id.book_info_layout);
			holder.tipInfo = (TextView)convertView.findViewById(R.id.progress_tv);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Key key = keyList.get(position);
		final int w = (int) context.getResources().getDimension(R.dimen.book_cover_small_w);
		final int h = (int) context.getResources().getDimension(R.dimen.book_cover_small_h);
		holder.bookCover.setTag(key.bokcoverurl);
		Bitmap bookImg = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_cover_m_img);
		bookImg = ThumbnailUtils.extractThumbnail(bookImg, w, h);
		holder.bookCover.setImageBitmap(bookImg);
		
		holder.bookName.setText(key.fileName);
		String str = key.subject==null?"":key.subject;
		if(str.equals("null")){
			str = "";
		}
		holder.subject.setText("学    科：" + str);
		str = key.grade== null ? "":key.grade;
		if(str.equals("null")){
			str = "";
		}
		holder.grade.setText("年    级：" + str);
		str = key.press==null?"":key.press;
		if(str.equals("null")){
			str = "";
		}
		holder.press.setText("出版社：" + str);
		str = key.sfileSize==null ? "":key.sfileSize;
		if(str.equals("null")){
			str = "";
		}
		holder.size.setText("大    小：" + str);
		holder.downBtImg.setTag(key);
		initDownBt(key, holder.downBtImg, holder.tipInfo);
		

		holder.bookLayout.setTag(key);
		if (listen != null) {
			holder.bookLayout.setOnClickListener(listen);
		}
		
		holder.downBtImg.setClickListen(new DownProgessButtonClickListen() {
			
			@Override
			public void onClick(View view, STATE state) {
				// TODO Auto-generated method stub
				if(state == STATE.STATE_DOWNING){
					if(downLoadListen != null){
						holder.tipInfo.setVisibility(View.VISIBLE);
						downLoadListen.pauseDown(key);
					}
				}else if(state == STATE.STATE_PAUSE){
					if(downLoadListen != null){
						holder.tipInfo.setVisibility(View.VISIBLE);
						downLoadListen.download(key);
					}
				}else if(state == STATE.STATE_CLICK_DOWN){
					if(downLoadListen != null){
						
						downLoadListen.download(key);
						holder.tipInfo.setVisibility(View.VISIBLE);
						holder.tipInfo.setText("等待中...");
						notifyDataSetChanged();
					}
				}else if(state == STATE.STATE_OPEN){
					if(downLoadListen != null){downLoadListen.openfile(key.info.filename);}
				}
			}
		});
		
		Bitmap bitmap  = null;
		if(key.bokcoverurl == null||"null".equals(key.bokcoverurl)){
			holder.bookCover.setImageBitmap(bookImg);
		}else{
			//Log.e("--111--", "key.bokcoverurl="+key.bokcoverurl);
			bitmap = loadImgUtil.loadImage(holder.bookCover,
					key.bokcoverurl,w,h,new ImageDownloadCallBack() {

						@Override
						public void onImageDownload(ImageView imageview,
								Bitmap bitmap) {
							// TODO Auto-generated method stub
							if (imageview.getTag().equals(key.bokcoverurl)) {
							
								imageview.setImageBitmap(bitmap);
								imageview.invalidate();
							}
						}
					});
		}
		if (bitmap != null) {
			holder.bookCover.setImageBitmap(bitmap);
			holder.bookCover.invalidate();
		}
		convertView.setTag(holder);
		views.put(position, convertView);
		return convertView;
	}
	
	public static void initDownBt(Key key,DownProgessButton downBt,TextView tip){
		if(key==null || downBt==null || tip == null){
			return;
		}
		
		STATE state1 = downBt.getState();
		if(key.info != null){
			//Debug.debugLog(" 111 key.fileName"+key.fileName+" runStatus= "+key.info.runStatus+" finished= "+key.info.finished);
			downBt.setBclicked(false);

			
			if(key.info.finished == 1){
				downBt.setBclicked(false);
				downBt.setState(STATE.STATE_OPEN);
				tip.setVisibility(View.INVISIBLE);
			}else{
				if(key.info.runStatus == 1 ){ //正在下载
					STATE state = STATE.STATE_DOWNING;
					tip.setVisibility(View.VISIBLE);
					downBt.setState(state);
					downBt.update(key.info.downloadByte, key.info.filesize);
				}else if(key.info.runStatus == 2 ){ //暂停
					STATE state = STATE.STATE_PAUSE;
					downBt.setState(state);
					downBt.update(key.info.downloadByte, key.info.filesize);
					tip.setVisibility(View.VISIBLE);
				}else if(key.info.runStatus == 3){ //等待中
					if(state1 == STATE.STATE_PAUSE){
						tip.setText("");
					}else{
						tip.setText("等待中...");
						
					}
					STATE state = STATE.STATE_DOWNING;
					tip.setVisibility(View.VISIBLE);
					downBt.setState(state);
					downBt.update(key.info.downloadByte, key.info.filesize);
				}else if(key.info.runStatus == 0){ //stop
					tip.setText("");
					tip.setVisibility(View.INVISIBLE);
					downBt.setState(STATE.STATE_CLICK_DOWN);
				}
				
				if (key.info.filesize > 0 && key.info.error == 0 && key.info.runStatus != 3) {
					tip.setText(String.format("%.2fM/%.2fM",
							(float) key.info.downloadByte / 1048576,
							(float) key.info.filesize / 1048576));
					tip.invalidate();
				
				} else if (key.info.error != 0) {
					tip.setText(key.info.getErrorMessage());
					downBt.setState(STATE.STATE_PAUSE);
				} 
			}
			downBt.invalidate();
		}else{
			if(downBt.isBclicked()){
				return;
			}
			//Debug.debugLog("key.fileName"+key.fileName);
			tip.setText("");
			tip.setVisibility(View.INVISIBLE);
			downBt.setState(STATE.STATE_CLICK_DOWN);
			downBt.invalidate();
		}
	

	}

	private class ViewHolder {
		ImageView bookCover;
		DownProgessButton downBtImg;
		TextView bookName;
		TextView subject;
		TextView grade;
		TextView press;
		TextView size;
		RelativeLayout bookLayout;
		TextView tipInfo;
	}

}
