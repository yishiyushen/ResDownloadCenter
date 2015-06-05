package com.noahedu.resdownloadcenter.adapter;

import java.util.ArrayList;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.search.MenuItem;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContentsListAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<MenuItem> list;
	private int curSelIndex = -1; 
	
	public ContentsListAdapter(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		
	}
	public void setData( ArrayList<MenuItem> list){
		this.list = list;
	}
	
	
	public void setCurSelIndex(int curSelIndex) {
		this.curSelIndex = curSelIndex;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null){
			convertView = new TextView(mContext);
		}
		convertView.setBackground(mContext.getResources().getDrawable(R.drawable.contents_selector));
		MenuItem data = list.get(position);
		if(curSelIndex == position){
			convertView.setBackground(mContext.getResources().getDrawable(R.drawable.content_selected));
		}else{
			convertView.setBackground(mContext.getResources().getDrawable(R.drawable.content_nomal));
		}
		((TextView)convertView).setTextColor(Color.WHITE);
		((TextView)convertView).setTextSize(mContext.getResources().getDimension(R.dimen.contents_text_size));
		((TextView)convertView).setText(data.name);
		((TextView)convertView).setPadding(20, 0, 0, 0);
		((TextView)convertView).setSingleLine(true);
		((TextView)convertView).setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
	
		convertView.setTag(data);
		return convertView;
	}

}
