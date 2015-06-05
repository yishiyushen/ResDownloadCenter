package com.noahedu.resdownloadcenter;

import java.io.IOException;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.noahedu.resdownloadcenter.customview.AutoSlipingLayout;
import com.noahedu.resdownloadcenter.customview.ContentMenuManager;
import com.noahedu.resdownloadcenter.customview.MainListViewManager;
import com.noahedu.resdownloadcenter.customview.MainListViewManager.ILoadNextPageDataListen;
import com.noahedu.resdownloadcenter.customview.PopMenuManager;
import com.noahedu.resdownloadcenter.customview.PopMenuManager.IDoSearch;
import com.noahedu.resdownloadcenter.customview.PullToRefreshLayout;
import com.noahedu.resdownloadcenter.customview.PullableListView;
import com.noahedu.resdownloadcenter.db.CheckUserInfo;
import com.noahedu.resdownloadcenter.db.CommDataAccess;
import com.noahedu.resdownloadcenter.net.HttpSearcher;
import com.noahedu.resdownloadcenter.search.ResourceData;
import com.noahedu.resdownloadcenter.search.SearchArg;
import com.noahedu.resdownloadcenter.util.AndroidUtil;
import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.ucache.ConnectionListener;
import com.noahedu.ucache.ConnectionManager;
import com.noahedu.ucache.DownloadManager;





public class ResLoadCenterMainActivity extends Activity implements IDoSearch,ILoadNextPageDataListen{
	
	public static final int SEARCH_NEW = 1;
	public static final int SEARCH_NEXT = 2;
	public static final int SEARCH_PREV = 3;
	public static final int SEARCH_PAGE = 4;
	public static final int SEARCH_START = 5;
	public static final int SEARCH_ERROR = 6;
	public static final int SEARCH_RETRY = 7;
	public static final int SEARCH_ERROR_LEGAL = 11;
	public static final int DOWNLOADALL_STARTING = 20;
	public static final int DOWNLOADALL_STARTED = 21;
	public static final int MEDIA_UNMOUNT = 22;
	public static final int MSG_UPDATE = 23;
	
	private AutoSlipingLayout mSlipLayout;
	private Button mManagerBt;
	private Button mFilterBt;
	private PopupWindow mFilterWnd; //过滤窗口
	private View parentView;
	private RelativeLayout mLeadingLayout;
	private PopMenuManager mPopMenuManager;
	private ContentMenuManager mContentManager;
	private MainListViewManager mMainListViewManager;
	private ConnectionManager connectionManager;
	private DownloadManager downloadManager;
	
	private ResourceData data = null;
	private ListView mContentsListV;
	private PullToRefreshLayout refreshLayout;
	private PullableListView mMainListView;
	
	private ProgressDialog mpDialog;

	private Builder builder;
	private AlertDialog alert;
	private EditText etSearch;
	private Button searchBt;
	private TextView noSourceTip;
	
	private boolean exitThread;
	private boolean newSearch = true;
	private boolean doSearch;
	private boolean mConnect;
	private boolean bSetNet = false;
	private int errRetryCount = 1;
	private String curUsrGrade;
	
	int pageSum = 0;
	int currPage;
	int offset = 1;

	InputMethodManager inputMethodManager;
	Toast toast;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case SEARCH_START:
				Debug.debugLog("SEARCH_START");
				if (!exitThread) {
					try {
						if(alert.isShowing()){
							alert.dismiss();
						}
						mpDialog.show();
						
					} catch (Exception e) {

					}
				}
				break;
			case SEARCH_NEW:
				errRetryCount = 1;
				mpDialog.setMessage("获取列表中...");
				
				break;
			case SEARCH_NEXT:
				errRetryCount = 1;
				
				//mpDialog.setMessage("获取列表中...");
				if (data != null) {
					mMainListViewManager.addDataToListView(data);
					refreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
//					if (mpDialog.isShowing()) {
//						try {
//							mpDialog.dismiss();
//						} catch (Exception e) {
//
//						}
//					}
				}else{
					refreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
				}
				break;
			case SEARCH_PAGE:
				errRetryCount = 1;
				mpDialog.setMessage("获取列表中...");
				if (data != null) {
					mMainListViewManager.setListViewData(data);
					if (mpDialog.isShowing()) {
						try {
							mpDialog.dismiss();
						} catch (Exception e) {

						}
					}
					
					if(data.keys == null || data.keys.size() == 0){
						refreshLayout.setVisibility(View.INVISIBLE);
						noSourceTip.setText(ResLoadCenterMainActivity.this.getResources().getString(R.string.sorry_search_no));
						noSourceTip.setVisibility(View.VISIBLE);
					}else{
						refreshLayout.setVisibility(View.VISIBLE);
						noSourceTip.setVisibility(View.INVISIBLE);
					}
				}
				break;
			case SEARCH_ERROR:
				Debug.debugLog("SEARCH_ERROR");
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
				boolean isConnected = false;
				if(alert.isShowing()){
					mpDialog.dismiss();
					break;
				}
				if (networkInfo != null)
					isConnected = networkInfo.isConnected();
				if (!isConnected) {
					if (mpDialog.isShowing()) {
						try {
							mpDialog.dismiss();
						} catch (Exception e) {
						}
					}
					if(!exitThread) {
						try {
							alert.show();
						} catch (Exception e) {
							
						}
					}
				} else {
					if (errRetryCount < 6) {
						mpDialog.setMessage(String.format("获取列表失败，重新尝试连接%d次，请稍等....", errRetryCount));
						sendMessageDelayed(handler.obtainMessage(SEARCH_RETRY), 2000);
						errRetryCount++;
					} else {
						errRetryCount = 1;
						if (mpDialog.isShowing()) {
							try {
								mpDialog.dismiss();
							} catch (Exception e) {

							}
						}
						if(!exitThread) {
							try {
								alert.show();
							} catch (Exception e) {
								
							}
						}
						mpDialog.setMessage("获取列表中...");
					}
				}
				break;
			case SEARCH_RETRY:
				doSearch(newSearch);
				break;
			case SEARCH_ERROR_LEGAL:
//				etSearch.setText("");
				if (mpDialog.isShowing()) {
					try {
						mpDialog.dismiss();
					} catch (Exception e) {

					}
				}
				toast.show();
				break;
			case MEDIA_UNMOUNT:
				if (mpDialog.isShowing()) {
					try {
						mpDialog.dismiss();
					} catch (Exception e) {

					}
				}
				
				try {
					Builder builder = new AlertDialog.Builder(ResLoadCenterMainActivity.this);
					builder.setMessage("检测到当前学习机已通过USB连接至电脑，请断开与电脑的USB连接")
							.setCancelable(true).setTitle("提示");
					builder.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							});
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
					builder.create().show();
				} catch (Exception e) {

				}
				break;
			case MSG_UPDATE:
				
				break;
			case HttpStatus.SC_OK:
				mContentManager.parseJsonStr((String) msg.obj);
				break;
			case HttpStatus.SC_NOT_FOUND:
			case HttpStatus.SC_CONTINUE:
				if (mpDialog.isShowing()) {
					try {
						mpDialog.dismiss();
					} catch (Exception e) {

					}
				}
				alert.show();
				refreshLayout.setVisibility(View.INVISIBLE);
				noSourceTip.setText("服务器暂时无法访问或网络故障，可能您需要重新设置网络!");
				noSourceTip.setVisibility(View.VISIBLE);
				mContentManager.setDialogShow(false);
				break;
			case ContentMenuManager.MSG_GET_DATAED_NULL:
				if (errRetryCount < 6) {
					Toast.makeText(ResLoadCenterMainActivity.this, String.format("获取目录列表失败，重新尝试连接%d次，请稍等....", errRetryCount), Toast.LENGTH_LONG);
					errRetryCount++;
					mContentManager.setDialogShow(false);
					mContentManager.getMenuJSonFromNet();
				}

				break;
			}
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		parentView = LayoutInflater.from(this).inflate(R.layout.activity_main,null);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(parentView);
		initView();
		initSearchCondition();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();


		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}

					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						Debug.debugLog();
						connectionManager.connect();
						mConnect = true;
						return;
					}
				}
				handler.sendMessage(handler.obtainMessage(MEDIA_UNMOUNT));
			}
		}.start();

		if(mFilterWnd.isShowing()){
			mFilterWnd.dismiss();
		}
		CheckUserInfo checkUser = CommDataAccess.getUserInfo(this);
		if(!checkUser.strUsergrade.equals(curUsrGrade)||bSetNet){
			initSearchCondition();
		}
		
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Debug.debugLog2();
		exitThread = true;
		this.mMainListViewManager.destroyThread();
		super.onDestroy();

		System.runFinalization();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		mFilterWnd.dismiss();
		DisplayMetrics dm = AndroidUtil.getScreenInfo(this);
		mFilterWnd.setWidth(dm.widthPixels);
		//doSearch(true);
	}
	
	private void initView(){
		etSearch = (EditText) findViewById(R.id.editSearch);
		searchBt = (Button)findViewById(R.id.search_bt);
		// 设置输入法的确认按钮以及该按钮的响应事件
		etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		etSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch (actionId) {
				case EditorInfo.IME_ACTION_SEARCH:
					inputMethodManager.hideSoftInputFromWindow(ResLoadCenterMainActivity.this.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					doSearch(true);
					break;
				}
				return false;
			}
		});
		OnClickListen clickListen = new OnClickListen();
		searchBt.setOnClickListener(clickListen);
		findViewById(R.id.manager_bt).setOnClickListener(clickListen);
		findViewById(R.id.sweep_bt).setOnClickListener(clickListen);
		findViewById(R.id.img_back).setOnClickListener(clickListen);
		
		mLeadingLayout = (RelativeLayout)findViewById(R.id.leading_layout);
		mFilterBt = (Button)findViewById(R.id.filter_bt);
		mFilterBt.setOnClickListener(new OnClickListen());
		mFilterWnd = new PopupWindow(this);
		DisplayMetrics dm = AndroidUtil.getScreenInfo(this);
		mFilterWnd.setWidth(dm.widthPixels);
		mFilterWnd.setHeight(442);
		mFilterWnd.setFocusable(true);
		mFilterWnd.setTouchable(true);
		
		mFilterWnd.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_menu_bk));
		mFilterWnd.setAnimationStyle(android.R.style.Animation_Dialog);
		View popupView = LayoutInflater.from(this).inflate(R.layout.filter_menu_content, null);
		mFilterWnd.setContentView(popupView);
		
		mPopMenuManager = new PopMenuManager(this,popupView,mFilterWnd);
		mPopMenuManager.setmIDosearch(this);
		mContentsListV = (ListView)findViewById(R.id.menu_list);
		mContentManager = new ContentMenuManager(this, mContentsListV,handler);
		mContentManager.setOnClickContentsListen(mPopMenuManager);
		mContentManager.setIGetSearchArgListen(mPopMenuManager);
		refreshLayout =(PullToRefreshLayout)findViewById(R.id.pull_reflesh_layout);
		mMainListView = (PullableListView)findViewById(R.id.book_info_listv);
		mMainListViewManager = new MainListViewManager(mMainListView, this);
		refreshLayout.setOnRefreshListener(mMainListViewManager);
		mMainListViewManager.setLoadNextListen(this);

		connectionManager = new ConnectionManager(this, new ConnectionListener() {
			@Override
			public void onDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnected(DownloadManager downloadManager) {
				// TODO Auto-generated method stub
				ResLoadCenterMainActivity.this.downloadManager = downloadManager;
				mMainListViewManager.setDownloadManager(downloadManager);
				
//				if (mpDialog1.isShowing()) {
//					try {
//						mpDialog1.dismiss();
//					} catch (Exception e) {
//
//					}
//				}
				//doSearch(true);
			}
		});
		
		exitThread = false;
		new SearchThread().start();
		
		mpDialog = new ProgressDialog(this, R.style.Theme_Dialog_Translucent_NoTitleBar);
		// mpDialog = new ProgressDialog(this, android.R.style.Theme_Panel);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		// mpDialog.setTitle("提示");// 设置标题
		// mpDialog.setIcon(R.drawable.ic_launcher);// 设置图标
		mpDialog.setMessage("获取列表中...");
		mpDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mpDialog.setCancelable(true);// 设置进度条是否可以按退回键取消

		builder = new AlertDialog.Builder(this);
		builder.setMessage("服务器暂时无法访问或网络故障，可能您需要重新设置网络").setCancelable(true).setTitle("无法连接到服务器")
				.setPositiveButton("设置网络", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						alert.dismiss();
						Intent intent = new Intent();
						// intent.putExtra("PassWordDialogTypeExtra", 21);
						intent.setAction(Settings.ACTION_WIFI_SETTINGS);
						startActivity(intent);
						bSetNet = true;
					}
				}).setNegativeButton("取消", null);
		alert = builder.create();

		toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.toast, null);
		toast.setView(v);
		noSourceTip = (TextView) findViewById(R.id.sorry_tip);
		
	}
	
	private void initSearchCondition(){
		refreshLayout.setVisibility(View.INVISIBLE);
		noSourceTip.setVisibility(View.INVISIBLE);
		Intent i = getIntent();
		Debug.debugLog("" + i);
		if (i != null) {
			boolean bfromOther = false;
			String function = i.getStringExtra("function");
			Debug.debugLog("function=" + function);
			String functionId = i.getStringExtra("functionid");
			
			String subject = i.getStringExtra("subject");
			Debug.debugLog("subject=" + subject);

			String grade = i.getStringExtra("grade");
			Debug.debugLog("grade=" + grade);
			
			String term = i.getStringExtra("term");
			Debug.debugLog("term=" + term);
			
			String keyWord = i.getStringExtra("keyword");
			Debug.debugLog("keyWord=" + keyWord);
			SearchArg searchArg = mPopMenuManager.getSearchArg();
			searchArg.reset();
			
			if (function != null) {
				searchArg.functionName = function;
				bfromOther = true;
				if(functionId != null){
					searchArg.function = functionId;
					bfromOther = true;
				}
			}
			

			if (subject != null) {
				searchArg.subjectName = subject;
				bfromOther = true;
			}
			
			if (grade != null) {
				searchArg.gradeName = grade;
				bfromOther = true;
			}
			
			if (term != null) {
				searchArg.termName = term;
				bfromOther = true;
			}
			
			
			if (keyWord != null) {
				// etSearch.setText(keyWord);
				etSearch.setText("");
				etSearch.append(keyWord);
				searchArg.keyword = keyWord;
				bfromOther = true;
			}
			if(!bfromOther){
				CheckUserInfo checkUser = CommDataAccess.getUserInfo(this);
				curUsrGrade = checkUser.strUsergrade;
				Debug.debugLog("---"+checkUser.strUsergrade);
				if(curUsrGrade != null&& !"".equals(curUsrGrade)){
					searchArg.gradeid = curUsrGrade;
				}
			}
		}
		mpDialog.show();
		mContentManager.getMenuJSonFromNet();
	}
	
	private String getGradeName(String id){
		String name = null;
		if(id!=null){
			if(id.equals("1")){
				name="一年级";
			}else if(id.equals("2")){
				name="二年级";
			}else if(id.equals("3")){
				name="三年级";
			}else if(id.equals("4")){
				name="四年级";
			}else if(id.equals("5")){
				name="五年级";
			}else if(id.equals("6")){
				name="六年级";
			}else if(id.equals("7")){
				name="七年级";
			}else if(id.equals("8")){
				name="八年级";
			}else if(id.equals("9")){
				name="九年级";
			}else if(id.equals("10")){
				name="高一";
			}else if(id.equals("11")){
				name="高二";
			}else if(id.equals("12")){
				name="高三";
			}
		}
		return name;
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(mFilterWnd != null && mFilterWnd.isShowing() && mPopMenuManager != null){
			mPopMenuManager.cancelPopupWind();
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(!mContentManager.backPreviousContents()){
			super.onBackPressed();
		}
	}
	
	@Override
	public void onLoadNextPageData() {
		// TODO Auto-generated method stub
		if(currPage >= pageSum){
			return;
		}
		offset = 1;
		doSearch(false);
	}
	
	@Override
	public void doSearch(boolean isNew) {
		// TODO Auto-generated method stub
		if (mMainListViewManager == null) {
			Debug.debugLog("mainListViewManager=" + mMainListViewManager);
		}

		// 如果是重新搜索，则重置一些状态
		if (isNew) {
			if (mMainListViewManager != null){
				mMainListViewManager.reset();
			}

			pageSum = 0;
			offset = 0;
			mMainListView.setbCanPullUpFlg(true);
			currPage = 1;
//			etGoto.setText("");
			Debug.debugLog("currPage=" + currPage);
		}

		// 告诉搜索线程开始搜索
		doSearch = true;
		newSearch = isNew;
	}
	
	private class SearchThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!exitThread) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
				if (!doSearch || !mConnect)
					continue;
				Debug.debugLog("ready....");
				// 准备.....
				if(newSearch){
					handler.sendMessage(handler.obtainMessage(SEARCH_START, 0, 0));
				}
				// 防止用户多次翻页或重新搜索引起混乱，缓存该标志，直到本次搜索结束
				boolean mNewSearch = newSearch;

				// 从弹出窗口中获取搜索信息
				SearchArg searchArg = mPopMenuManager.getSearchArg();

				searchArg.pageNo = currPage + offset;
				if (mNewSearch){
					searchArg.keyword = etSearch.getText().toString();
					if(searchArg.keyword.equals("")||searchArg.keyword == null){
						searchArg.keyword = null;
					}
				}

				// 如果是非法字符，则停止搜索
				if (searchArg.keyword!=null && searchArg.keyword.matches("[\\[\\]!\"#$%&'()*+,./:;<=>?@\\\\^_`{|}~-]+")) {
					doSearch = false;
					handler.sendMessageDelayed(handler.obtainMessage(SEARCH_ERROR_LEGAL, 0, 0), 100);
					continue;
				}

				// 搜索开始...
				HttpSearcher c = new HttpSearcher();
				try {
					data = c.start(searchArg, downloadManager);
					pageSum = data.totalPage > 0 ? data.totalPage : 0;
					if(data.totalCount == 0){
						
					}
				} catch (IOException e) {
					e.printStackTrace();

					// 通知UI线程搜索结束
					doSearch = false;
					handler.sendMessageDelayed(handler.obtainMessage(SEARCH_ERROR, e.getMessage()), 100);
					continue;
				}

				// 新搜索，则通知UI线程更新page信息
				if (mNewSearch)
					handler.sendMessageDelayed(handler.obtainMessage(SEARCH_NEW, 0, 0), 100);

				if (offset > 0) {
					// 通知UI线程显示下一页
					handler.sendMessageDelayed(handler.obtainMessage(SEARCH_NEXT, 0, 0), 100);
				} else if (offset < 0) {
					// 通知UI线程显示上一页
					handler.sendMessageDelayed(handler.obtainMessage(SEARCH_PREV, 0, 0), 100);
				} else {
					// 通知UI线程显示第几页
					handler.sendMessageDelayed(handler.obtainMessage(SEARCH_PAGE, 0, 0), 100);
				}
				currPage = currPage+offset;
				if(currPage >= pageSum){
					mMainListView.setbCanPullUpFlg(false);
				}
				doSearch = false;
			}
			super.run();
		}
	}
	

	public interface IGetSearchArg{
		public SearchArg getSearchArg();
	}
	
	private class OnClickListen implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.manager_bt:{
				Intent intent = new Intent("com.anoah.ucachemanager.MainActivity");
				startActivity(intent);
				}
				break;
			case R.id.filter_bt:
				mFilterWnd.showAsDropDown(mLeadingLayout);
				mPopMenuManager.setBtClicke(false);
				break;
			case R.id.search_bt:
				String keyword = etSearch.getText().toString();
				if(keyword == null ||"".equals(keyword)){
					break;
				}
				doSearch(true);
				break;
			case R.id.sweep_bt:
				Intent intent = new Intent("com.noahedu.resdownloadcenter.sweepactivity");
				startActivity(intent);
				break;
			case R.id.img_back:
				onBackPressed();
				break;
			}
			
		}
	}
}
