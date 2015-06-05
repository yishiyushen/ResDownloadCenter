package com.noahedu.resdownloadcenter.customview;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.net.HttpMyGet;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.search.StaticVar;

public class ResourceDetailFragment extends Fragment implements IDownloadManagerFragment{

	private Context context;
	private Key key;
	private TextView products;
	private TextView detail;
	private TextView dirStr;
	
	public ResourceDetailFragment(){
		
	}
	public ResourceDetailFragment(Key key){
		this.key = key;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		context = activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.resource_detail_layout, null);
		products = (TextView)view.findViewById(R.id.adapter_product_tv);
		detail = (TextView)view.findViewById(R.id.detail);
		dirStr = (TextView)view.findViewById(R.id.save_dir);
		if(key!=null){
			products.setText("适用机型："+key.products);
		String detail1 = (key.description == null || "null".equals(key.description))?"":key.description;
		detail.setText("课件介绍:"+detail1);
		String url = StaticVar.GET_BOOK_SV_DIR+"?productname="+"U30"+"&sourid="+key.id;
		new loadDirAsynTask().execute(url);
		}
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
		super.onResume();
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
		
	}
	
	@Override
	public void setIDownLoadListen(IDownLoadListen listen) {
		// TODO Auto-generated method stub
		
	}
	private String paseDirDataFromJson(String json){
		String data = null;
		if(json == null){
			return null;
		}
		try {
			JSONObject jObj = new JSONObject(json);
			String msgCode = jObj.optString("msgCode");
			data = jObj.optString("data");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	private class loadDirAsynTask extends AsyncTask<String, Void, String>{
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
			return paseDirDataFromJson(result);
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			dirStr.setText("该资源存放在U盘目录："+result);
			super.onPostExecute(result);
		}
	}
}
