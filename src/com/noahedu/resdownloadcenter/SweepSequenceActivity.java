package com.noahedu.resdownloadcenter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.noahedu.resdownloadcenter.net.HttpMyGet;
import com.noahedu.resdownloadcenter.qcode.ScanerFragment;
import com.noahedu.resdownloadcenter.qcode.ScanerFragment.IGetISBNStringCallBack;
import com.noahedu.resdownloadcenter.search.ResourceData;
import com.noahedu.resdownloadcenter.search.SearchArg;
import com.noahedu.resdownloadcenter.util.Debug;



public class SweepSequenceActivity extends Activity  implements IGetISBNStringCallBack{
	

	
	private EditText mBarCodeEt;
	private Button mSearchBt;
	private ScanerFragment scanerFragment;
	private FragmentManager manager;
	private ProgressDialog mpDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_sweep);
		init();
		initView();
	}
	
	private void init(){
		
	}
	
	private void initView(){

		mBarCodeEt = (EditText)this.findViewById(R.id.barcode_et);
		mSearchBt = (Button)this.findViewById(R.id.search_bt);
		manager = getFragmentManager();
		scanerFragment = new ScanerFragment();			
		manager.beginTransaction().replace(R.id.container, scanerFragment).commit();
		scanerFragment.setIGetISBNStringListen(this);
		mSearchBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String code = mBarCodeEt.getText().toString();
				if(code == null ||"".equals(code) ){
					return;
				}
				
				SearchArg arg = new SearchArg();
				arg.reset();
				arg.barcode = code;
				new SearchBookAsyncTask().execute(arg.getSearchResourceUrl());
			}
		});
		
		mpDialog = new ProgressDialog(this, R.style.Theme_Dialog_Translucent_NoTitleBar);
		// mpDialog = new ProgressDialog(this, android.R.style.Theme_Panel);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		// mpDialog.setTitle("提示");// 设置标题
		// mpDialog.setIcon(R.drawable.ic_launcher);// 设置图标
		mpDialog.setMessage("获取数据中...");
		mpDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mpDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
		findViewById(R.id.img_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	
	@Override
	public void importISBNString(String isbn) {
		// TODO Auto-generated method stub
		mBarCodeEt.append(isbn);
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Debug.debugLog2();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Debug.debugLog2();
		super.onResume();
	}
	
	private class SearchBookAsyncTask extends AsyncTask<String, Void, String>{
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mpDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = params[0];
			String str = null;
			Debug.debugLog("url= "+url);
			try {
				str = new HttpMyGet().doGet(url);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//ResourceData data = new ResourceData(str, null, null);
			return str;
		}
		
		@Override
		protected void onPostExecute(String results) {
			// TODO Auto-generated method stub
			mpDialog.dismiss();
			ResourceData result = new ResourceData(results, null, null);
			if(result == null || result.keys == null || result.keys.size() == 0){
				Toast.makeText(SweepSequenceActivity.this, "抱歉服务器上暂时还没有收藏本书!", Toast.LENGTH_LONG).show();
			}else{
				Intent intent = new Intent("com.noahedu.resdownloadcenter.bookdetailactivity");
				intent.putExtra("key", result.keys.get(0));
				startActivity(intent);
			}
			super.onPostExecute(results);
		}
		
	}
	
}
