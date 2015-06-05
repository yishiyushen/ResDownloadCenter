package com.noahedu.resdownloadcenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.noahedu.resdownloadcenter.adapter.FragmentViewPagerAdapter;
import com.noahedu.resdownloadcenter.adapter.IExtraPageChangeListener;
import com.noahedu.resdownloadcenter.adapter.MainListViewAdapter;
import com.noahedu.resdownloadcenter.customview.DownProgessButton;
import com.noahedu.resdownloadcenter.customview.DownProgessButton.DownProgessButtonClickListen;
import com.noahedu.resdownloadcenter.customview.DownProgessButton.STATE;
import com.noahedu.resdownloadcenter.customview.LoadDataManager;
import com.noahedu.resdownloadcenter.customview.ResourceDetailFragment;
import com.noahedu.resdownloadcenter.customview.VideoListFragment;
import com.noahedu.resdownloadcenter.net.HttpMyGet;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.search.StaticVar;
import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.resdownloadcenter.util.LoadImgUtil;
import com.noahedu.resdownloadcenter.util.LoadImgUtil.ImageDownloadCallBack;
import com.noahedu.ucache.ConnectionListener;
import com.noahedu.ucache.ConnectionManager;
import com.noahedu.ucache.DownloadManager;

public class BookDetailActivity extends Activity implements
		IExtraPageChangeListener {
	private LoadImgUtil loadImgUtil;
	private Key key;
	private ImageView imgView;
	private TextView bookName;
	private TextView typeTv;
	private TextView gradeTv;
	private TextView subjectTv;
	private TextView pressTv;
	private TextView refPressTv;
	private TextView sizeTv;
	private TextView updateTimeTv;
	private DownProgessButton downBt;
	private LinearLayout tab_layout;
	private ViewPager viewPager;
	private CheckBox checkedAll;
	private Button downAll;
	private ImageView backBt;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private List<Fragment> fragments;
	private FragmentViewPagerAdapter fragmentAdapter;
	private ArrayList<Button> tabBtArray;
	private TabBtListen clickListen;
	private ConnectionManager connectionManager;
	private DownloadManager downloadManager;
	private LoadDataManager loadDataManager;
	private boolean mConnect;
	private boolean exitThread;
	private boolean bInitDownLoadManager = false;
	private  ProgressDialog mDialog;
	private boolean bFirstEnter =true;
	private int curIndex = 0;
	private TextView downProgresTv;
	
	
	private final String[] sourceStr = new String[] { "xxvSource", "msvSource",
			"dmSource", "kpSource", "xxsSource", "zxsSource", };

	private final String[] tabNameStr = new String[] { "小学视频", "名师视频", "动漫课堂",
			"知识点搜学", "小学试卷", "中学试卷", };

	private final int[] drawableId = new int[]{
		R.drawable.resource_detail,
		R.drawable.primary_video,
		R.drawable.video_select,
		R.drawable.comic_video,
		R.drawable.kownledge_pt,
		R.drawable.primary_paper,
		R.drawable.mid_paper,
	};
	public static final int MSG_UPDATE = 0;
	private UpdateDownInfoThread updateThread;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE:
				if(key.info!=null){
					Debug.debugLog(String.format("当前下载进度：%.2fM/%.2fM",
							(float) key.info.downloadByte / 1048576,
							(float) key.info.filesize / 1048576));
//					if(downBt != null&& key.info != null && key.info.filesize != 0){
//						Debug.debugLog2();
//						
//						//downBt.update(key.info.downloadByte, key.info.filesize);
//					}
					MainListViewAdapter.initDownBt(key, downBt, downProgresTv);
				}
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_detail);

		init();
		initView();
	}

	private void init() {
		Intent intent = this.getIntent();
		key = intent.getParcelableExtra("key");
		loadImgUtil = LoadImgUtil.getSingleInstance(this,
				StaticVar.BOOK_COVER_IMG_DIR);
		fragmentManager = this.getFragmentManager();
		fragments = new ArrayList<Fragment>();
		tabBtArray = new ArrayList<Button>();
		clickListen = new TabBtListen();
		loadDataManager = new LoadDataManager(
				BookDetailActivity.this);
		connectionManager = new ConnectionManager(this,
				new ConnectionListener() {

					@Override
					public void onDisconnected() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onConnected(DownloadManager downloadManager) {
						// TODO Auto-generated method stub
						BookDetailActivity.this.downloadManager = downloadManager;
						
						loadDataManager.setDownloadManager(downloadManager);
						bInitDownLoadManager = true;
						try {
							key.info = downloadManager.getTaskInfoUrl(key.url);
							MainListViewAdapter.initDownBt(key, downBt, downProgresTv);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		curIndex = 0;

		exitThread = false;
		updateThread=new UpdateDownInfoThread();
		updateThread.start();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					if (Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
						Debug.debugLog();
						connectionManager.connect();
						mConnect = true;
						return;
					}
				}
			}
		}.start();

		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Debug.debugLog2();
		if (mConnect) {
			try {
				connectionManager.disconnect();
			} catch (Exception e) {
			}
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Debug.debugLog2();

		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Debug.debugLog2();
		bInitDownLoadManager = true;
		exitThread = true;
		super.onDestroy();
	}

	private void initView() {
		mDialog = new ProgressDialog(this, R.style.Theme_Dialog_Translucent_NoTitleBar);
		mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		// mpDialog.setTitle("提示");// 设置标题
		// mpDialog.setIcon(R.drawable.ic_launcher);// 设置图标
		mDialog.setMessage("请稍候...");
		mDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
		imgView = (ImageView) findViewById(R.id.book_img);
		bookName = (TextView) findViewById(R.id.book_name_tv);
		typeTv = (TextView) findViewById(R.id.type_tv);
		gradeTv = (TextView) findViewById(R.id.grade_tv);
		subjectTv = (TextView) findViewById(R.id.subject_tv);
		pressTv = (TextView) findViewById(R.id.press_tv);
		refPressTv = (TextView) findViewById(R.id.references_tv);
		sizeTv = (TextView) findViewById(R.id.size_tv);
		updateTimeTv = (TextView) findViewById(R.id.update_time_tv);
		downBt = (DownProgessButton) findViewById(R.id.down_bt);
		tab_layout = (LinearLayout) findViewById(R.id.tab_layout);
		viewPager = (ViewPager) findViewById(R.id.content);
		downProgresTv = (TextView)findViewById(R.id.progress_tv);
		backBt = (ImageView)findViewById(R.id.img_back);
		
		 setBookImg();
		
		String str = key.fileName;
		if(str == null||"null".equals(str)){
			str = "";
		}
		bookName.setText(str);
		 str = key.type;
		if(str == null||"null".equals(str)){
			str = "";
		}
		typeTv.setText("分     类: "+str);
		 str = key.grade;
		if(str == null||"null".equals(str)){
			str = "";
		}
		gradeTv.setText("年     级: "+str);
		 str = key.subject;
		if(str == null||"null".equals(str)){
			str = "";
		}
		subjectTv.setText("学     科: "+str);
		 str = key.press;
		if(str == null||"null".equals(str)){
			str = "";
		}
		pressTv.setText("出版社: "+str);
		 str = key.version;
		if(str == null||"null".equals(str)){
			str = "";
		}
		refPressTv.setText("参考版本: "+str);
		 str = key.sfileSize;
		if(str == null||"null".equals(str)){
			str = "";
		}
		sizeTv.setText("大       小: "+str);
		 str = key.updateTime;
		if(str == null||"null".equals(str)){
			str = "";
		}
		updateTimeTv.setText("更新时间: "+str);
		
		//initDownBt();
		MainListViewAdapter.initDownBt(key, downBt, downProgresTv);
		downBt.setClickListen(new DownProgessButtonClickListen() {
			
			@Override
			public void onClick(View view, STATE state) {
				// TODO Auto-generated method stub
				if(state == STATE.STATE_DOWNING){
					loadDataManager.pauseDown(key);
				}else if(state == STATE.STATE_PAUSE){
					loadDataManager.download(key);
				}else if(state == STATE.STATE_CLICK_DOWN){
					loadDataManager.download(key);
				}else if(state == STATE.STATE_OPEN){
					
					loadDataManager.openfile(key.info.filename);
				}
			}
		});
		
		checkedAll = (CheckBox) this.findViewById(R.id.check_all);
		checkedAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				Debug.debugLog("curIndex= "+curIndex);
				VideoListFragment fragment = (VideoListFragment) fragments.get(curIndex);
				fragment.selectAll(isChecked);
			}
		});
		downAll = (Button)this.findViewById(R.id.down_all);
		downAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				VideoListFragment fragment = (VideoListFragment) fragments.get(curIndex);
				fragment.downAll();
				fragment.bDownAllEnabled = false;
				fragment.setBtState();
			}
		});
		backBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 finish();
				 System.runFinalization();
			}
		});
		
		mDialog.show();
		bInitDownLoadManager = false;
		String url = StaticVar.ASSOCIATE_RESOURCE_URL + "?productname=" +"U30"/*
				 * Build
				 * .
				 * MODEL
				 */
				 + "&sourid=" + key.id + "&ctype=" + key.typeID;
		Debug.debugLog("url=" + url);
		new LoadBookDetail().execute(url);
	}
	
	private void setBookImg(){
		int w = (int) this.getResources().getDimension(R.dimen.book_cover_large_w);
		int h = (int)this.getResources().getDimension(R.dimen.book_cover_large_h);
		Debug.debugLog("---w="+w+"  h="+h);
		Bitmap defImg = BitmapFactory.decodeResource(this.getResources(), R.drawable.book_cover_l_img);
		defImg = ThumbnailUtils.extractThumbnail(defImg, w, h);
		imgView.setImageBitmap(defImg);
		if (key.bokcoverurl == null || "null".equals(key.bokcoverurl)) {
			imgView.setImageBitmap(defImg);
		} else {
			Bitmap img = loadImgUtil.loadImage(imgView, key.bokcoverurl,w,h,
					new ImageDownloadCallBack() {

						@Override
						public void onImageDownload(ImageView imageview,
								Bitmap bitmap) {
							// TODO Auto-generated method stub
							imageview.setImageBitmap(bitmap);
							imageview.invalidate();
						}
					});
			if (img != null) {
				imgView.setImageBitmap(img);
				imgView.invalidate();
			}
		}
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		//setBookImg();
		super.onConfigurationChanged(newConfig);
	}
	
	private void initDownBt(){
		if(key.info != null){
			if(key.info.runStatus == 1){ //正在下载
				STATE state = STATE.STATE_DOWNING;
				downBt.setState(state);
				downBt.update(key.info.downloadByte, key.info.filesize);
			}else if(key.info.runStatus == 2){ //暂停
				STATE state = STATE.STATE_PAUSE;
				downBt.setState(state);
				downBt.update(key.info.downloadByte, key.info.filesize);
			}else if(key.info.runStatus == 0){
				downBt.setState(STATE.STATE_CLICK_DOWN);
			}
			
			if(key.info.finished == 1){
				downBt.setState(STATE.STATE_OPEN);
			}
		}else{
			downBt.setState(STATE.STATE_CLICK_DOWN);
		}
	}
	

	private void setTabBtSelected(int index) {
		for (int i = 0; i < tabBtArray.size(); i++) {
			Button bt = tabBtArray.get(i);
			Debug.debugLog("index= "+index+" bt == null ? "+(bt==null)+" tabBtArray.size()= "+tabBtArray.size());
			if (i == index) {
				bt.setSelected(true);
			} else {
				bt.setSelected(false);
			}
		}
		if(index !=0 ){//显示全选，批量下载，暂停，
			checkedAll.setVisibility(View.VISIBLE);
			downAll.setVisibility(View.VISIBLE);
		}else{
			checkedAll.setVisibility(View.INVISIBLE);
			downAll.setVisibility(View.INVISIBLE);
		}
		curIndex = index;
	}

	private void initFragment(String json) {

		tabBtArray.clear();
		tab_layout.removeAllViews();
		fragments.clear();
		Button tabBt = new Button(this);
		LinearLayout.LayoutParams params1 = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params1.leftMargin = (int) getResources().getDimension(
				R.dimen.tab_bt_margin_left);
		tabBt.setTag(0);
		tabBt.setOnClickListener(clickListen);
		tabBt.setBackground(this.getResources().getDrawable(drawableId[0]));
		tabBtArray.add(tabBt);
		tab_layout.addView(tabBt, params1);
		
		ResourceDetailFragment resFragment = new ResourceDetailFragment(key);
		fragments.add(resFragment);
		if(json == null){
			fragmentAdapter = new FragmentViewPagerAdapter(fragmentManager,
					viewPager, fragments);
			fragmentAdapter.setExtraPListener(this);
			viewPager.setAdapter(fragmentAdapter);
			viewPager.setCurrentItem(curIndex);
			setTabBtSelected(curIndex);
			return;
		}
		try {
			JSONObject jObj = new JSONObject(json);
			String msgCode = jObj.getString("msgCode");
			JSONObject jData = jObj.optJSONObject("data");
			if (msgCode.equals("302") && jData != null) {
				for (int i = 0; i < sourceStr.length; i++) {
					JSONArray jarray = jData.optJSONArray(sourceStr[i]);

					if (jarray == null || jarray.length() == 0) {
						continue;
					}
					ArrayList<Key> lists = parseKeyFromJSonArray(jarray);

					VideoListFragment fragment = new VideoListFragment(lists);

					Button tabBt0 = new Button(this);
					LinearLayout.LayoutParams params = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.leftMargin = (int) getResources().getDimension(
							R.dimen.tab_bt_margin_left);
					int index = tab_layout.getChildCount();
					tabBt0.setTag(index);
					tabBt0.setBackground(this.getResources().getDrawable(drawableId[i+1]));
					tabBt0.setOnClickListener(clickListen);
					tab_layout.addView(tabBt0, params);
					tabBtArray.add(tabBt0);
					fragment.setIDownLoadListen(loadDataManager);
					fragment.checkBoxAll = checkedAll;
					fragment.downAll = downAll;
					fragments.add(fragment);
				}

				fragmentAdapter = new FragmentViewPagerAdapter(fragmentManager,
						viewPager, fragments);
				fragmentAdapter.setExtraPListener(this);
				viewPager.setAdapter(fragmentAdapter);
				viewPager.setCurrentItem(curIndex);
				setTabBtSelected(curIndex);
			}else{
				fragmentAdapter = new FragmentViewPagerAdapter(fragmentManager,
						viewPager, fragments);
				fragmentAdapter.setExtraPListener(this);
				viewPager.setAdapter(fragmentAdapter);
				viewPager.setCurrentItem(curIndex);
				setTabBtSelected(curIndex);
				return;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private ArrayList<Key> parseKeyFromJSonArray(JSONArray jarray) {
		ArrayList<Key> list = new ArrayList<Key>();
		for (int i = 0; i < jarray.length(); i++) {
			try {
				Key tmpkey = new Key(jarray.getJSONObject(i), downloadManager,
						key.function);

				list.add(tmpkey);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	private class TabBtListen implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int index = (Integer) v.getTag();
			Debug.debugLog("-----index= "+index);
			curIndex = index;
			viewPager.setCurrentItem(index);
			//setTabBtSelected(index);
		}
	}

	private class LoadBookDetail extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = params[0];
			String result = null;
			try {
				result = new HttpMyGet().doGet(url);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (!bInitDownLoadManager) {
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			Debug.debugLog("result=" + result);
			mDialog.dismiss();
			initFragment(result);
			super.onPostExecute(result);
		}
	}

	private class UpdateDownInfoThread extends Thread {
		public UpdateDownInfoThread() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			while (!exitThread) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!bInitDownLoadManager){
					continue;
				}
				//for (int i = 0; i < fragments.size(); i++) {
				if(fragments.size() != 0 && fragments.size()>curIndex){
					Fragment fragment = fragments.get(curIndex);
					if (fragment instanceof VideoListFragment) {
						((VideoListFragment) fragment).updateAll();
					}
				}

				/** finished: 是否已经下载完成,0为未完成,1为已完成 */
				/** runStatus: 当前运行状态，0:stop, 1:run, 2:pause */	
				if (key.info == null
						|| (key.info.finished == 0)) {

					loadDataManager.update(key);
					handler.sendMessageDelayed(
							handler.obtainMessage(MSG_UPDATE), 20);
				}
			}
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
		curIndex = arg0;
		Fragment fragm = fragments.get(curIndex);
		if(fragm instanceof VideoListFragment){ 
			VideoListFragment fragment = (VideoListFragment)fragm ;
			fragment.updateAll();
			fragment.setBtState();
		}

		setTabBtSelected(arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

}
