package com.noahedu.resdownloadcenter.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.customview.DownProgessButton;
import com.noahedu.resdownloadcenter.customview.DownProgessButton.DownProgessButtonClickListen;
import com.noahedu.resdownloadcenter.customview.DownProgessButton.STATE;
import com.noahedu.resdownloadcenter.customview.IDownLoadListen;
import com.noahedu.resdownloadcenter.customview.VideoListFragment;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.util.Debug;


public class AssociateDataListAdapter extends BaseAdapter {
	private ArrayList<Key> lists = null;
	private LayoutInflater inflater;
	private IDownLoadListen listen;
	private View parent;
	private View[] views;
	private static final int MSG_UPDATE = 1;
	private Handler handler;
	private boolean pauseAll;
	private MyOncheckedListen checkedListen;
	private boolean[] bItemChecks;
	private boolean[] bItemDown;
	private VideoListFragment fragment;
	private int checkedItem;


	public AssociateDataListAdapter(Context context,ArrayList<Key> lists,VideoListFragment fragment) {
		// TODO Auto-generated constructor stub
		this.fragment = fragment;
		inflater = LayoutInflater.from(context);
		this.lists = lists;
		if(lists != null){
		views = new View[lists.size()];
		bItemChecks = new boolean[lists.size()];
		bItemDown = new boolean[lists.size()];
		}

		checkedListen = new MyOncheckedListen();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_UPDATE:
					handler.removeMessages(msg.what);
//					Debug.debugLog2();
					notifyDataSetChanged();
				//	doUpdate();
					break;
				// case MSG_DOWNLOADALL:
				// doDownloadAll();
				// break;
				}
			}
		};
	}
	
	public void setCheckAll(boolean bflage){
		for(int i=0;i<bItemChecks.length;i++){
			bItemChecks[i] = bflage;
		}
		if(!bflage){
			checkedItem = 0;
		}
	}
	
	public void setIDownLoadListen(IDownLoadListen listen){
		this.listen = listen;
	}
	
	public void updateAll(){
		if (views == null)
			return;
		for (int i = 0; i < views.length; i++) {
			View rootView = views[i];

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
				Object tag2 = holder.downBt.getTag();
				if(tag2 != null && tag2 instanceof Key){
					key = (Key)tag2;
				}else{
					return;
				}
			}
			else
				return;

			if(listen != null){
				listen.update(key);
			}
		}

		handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE), 20);
	}
	
	public void downAll(){
		if (parent == null || !parent.isShown()) {
			Debug.debugLog("parent not shown, not download all");
			return;
		}

		pauseAll = false;
		checkedItem = 0;
		Debug.debugLog("parent is shown, do download all");
		synchronized (lists) {
			for (int i=0;i<lists.size();i++) {
				if (pauseAll) {
					Debug.debugLog("pauseAll=" + pauseAll + ", break.");
					break;
				}
				if(!bItemChecks[i]){
					continue;
				}
				Key key = lists.get(i);
				if(key.info != null && key.info.runStatus == 1){
					continue;
				}
				Debug.debugLog("key=" + key + ", key.info=" + key.info);
				if(listen != null){
					listen.download(key);
				bItemDown[i] = true;
			}
		}
		notifyDataSetChanged();	
	}
	}
	
	public void pauseAll(){
		if (parent == null || !parent.isShown()) {
			Debug.debugLog("parent not shown, not pause all");
			return;
		}

		Debug.debugLog("start do pause all");
		pauseAll = true;

		synchronized (lists) {
			Debug.debugLog("do pause all!");

			for (int i=0;i<lists.size();i++)  {
				if(!bItemChecks[i]){
					continue;
				}
				Key key = lists.get(i);
				Debug.debugLog("key=" + key + ", key.info=" + key.info);
				if(listen != null){
					listen.pauseDown(key);
				}
			}
		}
	}
	
	
	public void selectAll(){

	}
	
	public void cancelAll(){
		if (parent == null || !parent.isShown()) {
			Debug.debugLog("parent not shown, not cancel all");
			return;
		}
		
		synchronized (lists) {
			Debug.debugLog("do cancel all!");

			for (int i=0;i<lists.size();i++)  {
				if(!bItemChecks[i]){
					continue;
				}
				Key key = lists.get(i);
				Debug.debugLog("key=" + key + ", key.info=" + key.info);
				if(listen != null){
					listen.cancelDown(key);
				}
			}
		}
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(this.lists != null){
			return lists.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(this.lists != null){
			return lists.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (parent != null)
			this.parent = parent;
		final ViewHolder holder ;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.associate_list_item, null);
			holder.selectCheck = (CheckBox) convertView.findViewById(R.id.checked);
			holder.resName = (TextView)convertView.findViewById(R.id.resource_name);
			holder.downBt = (DownProgessButton)convertView.findViewById(R.id.down_bt);
			holder.tipInfo = (TextView)convertView.findViewById(R.id.progress_tv);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Key key = lists.get(position);
		//initDownBt(key,holder.downBt);
		if(!bItemDown[position]){
			MainListViewAdapter.initDownBt(key, holder.downBt, holder.tipInfo);
		}else{
			STATE state = STATE.STATE_DOWNING;
			holder.downBt.setState(state);
			holder.downBt.invalidate();
		}
		if(key.info != null){
			bItemDown[position] = false;
		}
		
		holder.downBt.setClickListen(new DownProgessButtonClickListen() {
			
			@Override
			public void onClick(View view, STATE state) {
				// TODO Auto-generated method stub
				if(state == STATE.STATE_DOWNING){
					if(listen != null){
						holder.tipInfo.setVisibility(View.VISIBLE);
						listen.pauseDown(key);
					}
				}else if(state == STATE.STATE_PAUSE){
					if(listen != null){
						holder.tipInfo.setVisibility(View.VISIBLE);
						listen.download(key);
					}
				}else if(state == STATE.STATE_CLICK_DOWN){
					if(listen != null){
						holder.tipInfo.setVisibility(View.VISIBLE);
						holder.tipInfo.setText("等待中...");
						notifyDataSetChanged();
						listen.download(key);
					}
				}else if(state == STATE.STATE_OPEN){
					if(listen != null){listen.openfile(key.info.filename);}
				}
				
			}
		});
		holder.selectCheck.setOnCheckedChangeListener(checkedListen);
		holder.selectCheck.setTag(position);
		//Debug.debugLog("position="+position+" bItemChecks[position]= "+bItemChecks[position]);
		holder.selectCheck.setChecked(bItemChecks[position]);
		holder.resName.setText(key.name);
		holder.downBt.setTag(key);
		convertView.setTag(holder);
		views[position] = convertView;
		return convertView;
	}
	
	private void initDownBt(Key key,DownProgessButton downBt){
		if(key.info != null){
			downBt.setBclicked(false);
			if(key.info.runStatus == 1){ //正在下载
				STATE state = STATE.STATE_DOWNING;
				downBt.setState(state);
				downBt.update(key.info.downloadByte, key.info.filesize);
			}else if(key.info.runStatus == 2){ //暂停
				STATE state = STATE.STATE_PAUSE;
				downBt.setState(state);
				downBt.update(key.info.downloadByte, key.info.filesize);
			}else if(key.info.runStatus == 0 && key.info.finished == 0){
				downBt.setState(STATE.STATE_CLICK_DOWN);
			}
			
			if(key.info.finished == 1){
				downBt.setState(STATE.STATE_OPEN);
			}
		}else{
			if(downBt.isBclicked()){
				return;
			}
			downBt.setState(STATE.STATE_CLICK_DOWN);
		
		}
	}
	
	private class MyOncheckedListen implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if(buttonView instanceof CheckBox){
				//((CheckBox)buttonView).setChecked(isChecked);
				int position = (Integer) ((CheckBox)buttonView).getTag();
				Debug.debugLog("position="+position+" bItemChecks[position]= "+bItemChecks[position]);
				bItemChecks[position] = isChecked;
				Key key = lists.get(position);
				if(key.info==null||key.info.runStatus==0||key.info.runStatus==2&&!bItemDown[position]){
					if(isChecked){
						checkedItem++;
						fragment.bDownAllEnabled = true;
						fragment.setBtState();
					}else{
						checkedItem--;
						if(checkedItem <=0){
							checkedItem =0;
							fragment.bDownAllEnabled = false;
							fragment.setBtState();
						}
						
					}
					
				}
			}
		}
	}
	
	private class ViewHolder {
		CheckBox selectCheck;
		TextView resName;
		TextView tipInfo;
		DownProgessButton downBt;
	}
	


}
