package com.noahedu.resdownloadcenter.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.search.PopVData;

public class PopListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Context mContext;
	private ArrayList<PopVData> list;
	private int curSelIndex = -1; 
	
	public PopListAdapter(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public void setData(ArrayList<PopVData> list){
		this.list = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list == null){
			return 0;
		}else{
			return list.size();
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getCurSelectIndex(){
		return curSelIndex;
	}
	public void resetCurSelectIndex(){
		curSelIndex = -1;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.text_view_layout, null);
		}
		convertView.setBackgroundColor(Color.TRANSPARENT);
		PopVData data = list.get(position);
		if(data.bSelected){
			curSelIndex = position;
			convertView.setBackgroundResource(R.drawable.bt_select);
		}
		String str = data.value;

		((TextView)convertView).setText(str);
		return convertView;
	}

}
