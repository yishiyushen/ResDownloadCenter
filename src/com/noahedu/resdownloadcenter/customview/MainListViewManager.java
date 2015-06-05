package com.noahedu.resdownloadcenter.customview;


import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.sax.StartElementListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.adapter.MainListViewAdapter;
import com.noahedu.resdownloadcenter.customview.PullToRefreshLayout.OnRefreshListener;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.search.ResourceData;
import com.noahedu.ucache.DownloadManager;

public class MainListViewManager implements OnRefreshListener{
	private PullableListView mainListV;
	private Context context;
	private DownloadManager downloadManager;
	private PullToRefreshLayout pullLayout;
	private ILoadNextPageDataListen loadNextListen;
	private ArrayList<Key> keys;
	private MainListViewAdapter mMainListAdapter;
	private LoadDataManager loadDataManager;
	private boolean exitThread = false;
	private boolean bInitLoadDataMan = false;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
		};
	};
	
	public MainListViewManager(PullableListView listV, Context context) {
		// TODO Auto-generated constructor stub
		this.mainListV = listV;
		this.context = context;
		mMainListAdapter = new MainListViewAdapter(context);
		this.mainListV.setAdapter(mMainListAdapter);
		this.mainListV.setSelector(new ColorDrawable(Color.TRANSPARENT));
		this.mMainListAdapter.setListen(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Key key = (Key) v.getTag();
				switch(v.getId()){
				case R.id.book_info_layout:
					Intent intent = new Intent("com.noahedu.resdownloadcenter.bookdetailactivity");
					intent.putExtra("key", key);
					MainListViewManager.this.context.startActivity(intent);
					break;
				}
			}
		});
		loadDataManager = new LoadDataManager(
				context);
		this.mMainListAdapter.setIDownListen(loadDataManager);
		exitThread = false;
		UpDataDownInfoThread thread = new UpDataDownInfoThread();
		this.mMainListAdapter.getLoadImgUtil().executeThread(thread);
	}
	
	public void setListViewData(ResourceData data){
		this.keys = null;
		if(data != null){
			this.keys = data.keys;
		}
		mMainListAdapter.setKeyList(this.keys);
		mMainListAdapter.notifyDataSetChanged();
	}
	
	
	public void addDataToListView(ResourceData data){
		ArrayList<Key> keyData = data.keys;
		this.keys.addAll(keyData);
		mMainListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		if(this.loadNextListen != null){
			loadNextListen.onLoadNextPageData();
		}
	}
	
	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
		this.loadDataManager.setDownloadManager(downloadManager);
		bInitLoadDataMan = true;
	}
	
	
	
	public void setLoadNextListen(ILoadNextPageDataListen loadNextListen) {
		this.loadNextListen = loadNextListen;
	}

	/**清空列表*/
	public void reset(){
		keys=null;
		this.mMainListAdapter.setKeyList(keys);
		this.mMainListAdapter.notifyDataSetChanged();
	}
	
	public void destroyThread(){
		exitThread = true;
	}
	
	public interface ILoadNextPageDataListen{
		public void onLoadNextPageData();
	}
	
	private class UpDataDownInfoThread extends Thread{
		public UpDataDownInfoThread() {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!exitThread) {
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(!bInitLoadDataMan){
					continue;
				}
				
				mMainListAdapter.updateAll();
			}
		}
	}
}
