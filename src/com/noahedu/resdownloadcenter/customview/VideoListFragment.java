package com.noahedu.resdownloadcenter.customview;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.adapter.AssociateDataListAdapter;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.util.Debug;

public class VideoListFragment extends Fragment implements IDownloadManagerFragment{
	
	private ArrayList<Key> listKeys = null;
	private IDownLoadListen downLoadListen;
	private ListView listView;
	private AssociateDataListAdapter adapter;
	private Context context;
	public boolean bSelectedAll;
	public boolean bDownAllEnabled;
	public CheckBox checkBoxAll;
	public Button downAll;

	

	public VideoListFragment() {
		// TODO Auto-generated constructor stub
		bSelectedAll = false;
		bDownAllEnabled = false;
		Debug.debugLog("new bSelectall = "+bSelectedAll);
	}
	
	public VideoListFragment(ArrayList<Key> lists) {
		// TODO Auto-generated constructor stub
		listKeys = lists;
		Debug.debugLog();
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		context = activity;
		Debug.debugLog();
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Debug.debugLog();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Debug.debugLog();
		View view = inflater.inflate(R.layout.associate_resource_layout, null);
		listView = (ListView)view.findViewById(R.id.content_listv);
	//	Debug.debugLog("onCreateView --- listkeys.size = "+listKeys.size());
		adapter = new AssociateDataListAdapter(context, listKeys,this);
		adapter.setIDownLoadListen(downLoadListen);
		listView.setAdapter(adapter);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Debug.debugLog("bSelectall = "+bSelectedAll);
//		updateAll();
//		setBtState();
//		adapter.notifyDataSetChanged();
		super.onResume();
	}
	
	public void setBtState(){
		if(checkBoxAll != null){
			checkBoxAll.setChecked(bSelectedAll);
		}
		
		if(downAll != null){
			downAll.setEnabled(bDownAllEnabled);
		}
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}
	
	@Override
	public void updateAll() {
		// TODO Auto-generated method stub
		if(adapter != null){
			adapter.updateAll();

		}
	}
	
	public void downAll(){
		if(adapter != null){
			adapter.downAll();
			adapter.notifyDataSetChanged();
		}
	}
	
	public void selectAll(boolean bChecked){
		bSelectedAll = bChecked;
		Debug.debugLog("bSelectedAll= "+bSelectedAll);
		if(adapter != null){
			
			adapter.setCheckAll(bChecked);
			adapter.notifyDataSetChanged();
		}
	}
	
	public void cancelAll(){
		if(adapter != null){
			adapter.cancelAll();
		}
	}
	

	
	@Override
	public void setIDownLoadListen(IDownLoadListen listen) {
		// TODO Auto-generated method stub
		downLoadListen = listen;
	}


	
	
}
