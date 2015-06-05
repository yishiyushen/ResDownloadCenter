package com.noahedu.resdownloadcenter.customview;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.ResLoadCenterMainActivity.IGetSearchArg;
import com.noahedu.resdownloadcenter.adapter.ContentsListAdapter;
import com.noahedu.resdownloadcenter.db.CheckUserInfo;
import com.noahedu.resdownloadcenter.db.CommDataAccess;
import com.noahedu.resdownloadcenter.net.HttpGetThread;
import com.noahedu.resdownloadcenter.search.MenuItem;
import com.noahedu.resdownloadcenter.search.SearchArg;
import com.noahedu.resdownloadcenter.search.StaticVar;
import com.noahedu.resdownloadcenter.util.Debug;

public class ContentMenuManager {

	private String menuJson;
	private Context context;
	private MenuItem[] mMenu; // 顶层菜单项；
	private MenuItem selMenuItem; // 当前选中的目录项
	private int curLevel = 0;
	private IGetSearchArg getSearchArgListen;
	private ListView mListV;
	private ProgressDialog mpDialog;
	private SparseArray<ArrayList<MenuItem>> mContentsArray = new SparseArray<ArrayList<MenuItem>>();
	private ContentsListAdapter mContentsAdapter;
	private IContentsItemChangeListen mClickContentsListen;
	private SparseArray<MenuItem> svClickMenu = new SparseArray<MenuItem>(); //保存点击的目录项
	public static final int MSG_GET_DATAED_NULL = 0x1001;
	
	private Handler handler ;
	
	public void parseJsonStr(String json){
		if(mpDialog!= null && mpDialog.isShowing()){
			mpDialog.dismiss();
		}
		menuJson = json;
		readFromJsonStr();
		createFirstContents();
	}
	
	public void setDialogShow(boolean bflag){
		if(bflag){
			if(mpDialog!= null &&!mpDialog.isShowing()){
				mpDialog.show();
			}
		}else{
			if(mpDialog!= null && mpDialog.isShowing()){
				mpDialog.dismiss();
			}
		}
	}

	public ContentMenuManager(Context context, ListView listView,Handler handler) {
		// TODO Auto-generated constructor stub
		this.context = context;
		mListV = listView;
		mListV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				MenuItem data = (MenuItem) view.getTag();
				selMenuItem = data;
				SearchArg arg = getSearchArgListen.getSearchArg();
				arg.reset();
				arg.function = String.valueOf(data.id);
				if (mClickContentsListen != null) {
					mClickContentsListen.onClickContentsItem(data);
				}
				
				jumpContent(data,position); //跳转目录
			}
		});
		mContentsAdapter = new ContentsListAdapter(context);
		
		mpDialog = new ProgressDialog(context, R.style.Theme_Dialog_Translucent_NoTitleBar);
		// mpDialog = new ProgressDialog(this, android.R.style.Theme_Panel);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		// mpDialog.setTitle("提示");// 设置标题
		// mpDialog.setIcon(R.drawable.ic_launcher);// 设置图标
		mpDialog.setMessage("获取数据中...");
		mpDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mpDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		this.handler = handler;
		//getMenuJSonFromNet();
	}
	
	private void jumpContent(MenuItem data,int position){
		if (data.childJson!= null && data.childJson.length() != 0) {
			createChirdrenContents(data);
		} else {
			mContentsAdapter.setCurSelIndex(position);
			mContentsAdapter.notifyDataSetInvalidated();
		}
	}
	
	public void setIGetSearchArgListen(IGetSearchArg listen){
		this.getSearchArgListen = listen;
	}
	
	public void resetAdapter(){
		mContentsAdapter.setCurSelIndex(-1);
		mContentsAdapter.setData(null);
		mContentsAdapter.notifyDataSetChanged();
	}

	public void getMenuJSonFromNet() {
		mpDialog.show();
		resetAdapter();
		String url = StaticVar.CONTENT_MENU_URL + "?pname=" + "U28";
		Debug.debugLog("url=  " + url);
		// url =
		// "http://192.168.8.174:8085/MyWeb/GetCommentServlet?nid=1&startnid=0&count=10";
		HttpGetThread thread = new HttpGetThread(handler, url);
		new Thread(thread).start();
	}

	/** 生成第一级目录 */
	private void createFirstContents() {
		if(mMenu == null){
			return;
		}
		ArrayList<MenuItem> menusList = new ArrayList<MenuItem>();
		MenuItem menu = mMenu[0];
		SearchArg arg = getSearchArgListen.getSearchArg();
		if(arg.functionName != null && !"".equals(arg.functionName) && arg.function != null &&!"".equals(arg.function)){
			MenuItem m = new MenuItem();
			m.id =  Integer.valueOf(arg.function);
			m.name = arg.functionName;
			menusList.add(m);
			curLevel = 0;
			mContentsArray.put(curLevel, menusList);
			mContentsAdapter.setData(menusList);
			mListV.setAdapter(mContentsAdapter);
			svClickMenu.put(curLevel, m);
			jumpContent(m,0);
			if (mClickContentsListen != null) {
				mClickContentsListen.onClickContentsItem(m);
				
			}
			return;
			
		}
		for (int i = 0; i < menu.childJson.length(); i++) {
			try {
				menusList.add(parseMenuJson(menu.childJson.getJSONObject(i)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		menusList.add(mMenu[1]);
		curLevel = 0;
		mContentsArray.put(curLevel, menusList);
		mContentsAdapter.setData(menusList);
		mListV.setAdapter(mContentsAdapter);
		svClickMenu.put(curLevel, mMenu[0]);
		MenuItem temp = mMenu[0];
		
		if(arg.functionName != null && !"".equals(arg.functionName)){
			boolean bflage = false;
			for(int i=0;i<menusList.size();i++){
				MenuItem item = menusList.get(i);
				if(item.name.equals(arg.functionName)){
					temp = item;
					jumpContent(item,i);
					bflage = true;
					break;
				}
			}
			
			if(!bflage){
				MenuItem item = getMenuItemByName(arg.functionName,mMenu[1]);
				if(item != null){
					temp = item;
				}
			}
					
		}
		
		if (mClickContentsListen != null) {
			mClickContentsListen.onClickContentsItem(temp);
			
		}
	}
	
	private MenuItem getMenuItemByName(String name,MenuItem parent){
		ArrayList<MenuItem> menusList = getChirdrenContents(parent);
		MenuItem temp = null;
		int index = -1;
		for(int i=0;i<menusList.size();i++){
			MenuItem item = menusList.get(i);
			if(item.name.equals(name)){
				temp = item;
				index = i;
				break;
			}
		}
		
		if(temp != null){
			curLevel++;
			svClickMenu.put(curLevel, parent);
			mContentsArray.put(curLevel, menusList);
			mContentsAdapter.setData(menusList);
			mContentsAdapter.setCurSelIndex(-1); // 清除选择效果
			mContentsAdapter.notifyDataSetChanged();
			jumpContent(temp,index);
		}
		return temp;
	}
	
	private ArrayList<MenuItem> getChirdrenContents(MenuItem parent){
		ArrayList<MenuItem> menusList = new ArrayList<MenuItem>();
		for (int i = 0; i < parent.childJson.length(); i++) {
			try {
				menusList.add(parseMenuJson(parent.childJson.getJSONObject(i)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return menusList;
	}

	private void createChirdrenContents(MenuItem parent) {
		ArrayList<MenuItem> menusList = getChirdrenContents(parent);
		
		curLevel++;
		svClickMenu.put(curLevel, parent);
		mContentsArray.put(curLevel, menusList);
		mContentsAdapter.setData(menusList);
		mContentsAdapter.setCurSelIndex(-1); // 清除选择效果
		mContentsAdapter.notifyDataSetChanged();
	}

	public boolean backPreviousContents() {
		if (curLevel <= 0) {
			return false;
		}

		mContentsArray.remove(curLevel);
		svClickMenu.remove(curLevel);
		curLevel--;
		MenuItem item = svClickMenu.get(curLevel);
		if(mClickContentsListen != null){
			mClickContentsListen.onContentsBackUpLevel(item);
		}
		ArrayList<MenuItem> menusList = mContentsArray.get(curLevel);
		mContentsAdapter.setData(menusList);
		mContentsAdapter.setCurSelIndex(-1); // 清除选择效果
		mContentsAdapter.notifyDataSetChanged();
		return true;
	}

	private void readFromJsonStr() {
		try {
			Debug.debugLog("menuJson= " + menuJson);

			// File file = new File("/mnt/sdcard/dab.txt");
			// try {
			// FileOutputStream out=new FileOutputStream(file);
			// PrintStream p = new PrintStream(out);
			// p.print(menuJson.substring(menuJson.indexOf("\"")+1,menuJson.lastIndexOf("\"")));
			// } catch (FileNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			JSONObject jsonObj = new JSONObject(menuJson);

			JSONArray jsonArray = jsonObj.getJSONArray("data");
			CheckUserInfo userInfo = CommDataAccess.getUserInfo(context);
			Debug.debugLog(userInfo.strUsersp);
			if (userInfo.strUsersp.equals("1")) { // 幼教
				mMenu = getFromUserGrade("幼教", jsonArray);
			} else if (userInfo.strUsersp.equals("2")) { // 小学
				mMenu = getFromUserGrade("小学", jsonArray);
			} else if (userInfo.strUsersp.equals("3")
					|| userInfo.strUsersp.equals("4")) { // 中学
				mMenu = getFromUserGrade("中学", jsonArray);
			}
			if(mMenu == null){
				handler.sendMessage(handler.obtainMessage(MSG_GET_DATAED_NULL));
				return;
			}



			Debug.debugLog("mMenu.length = " + mMenu.length);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private MenuItem[] getFromUserGrade(String grade, JSONArray jArray) {
		MenuItem[] menus = new MenuItem[2];
		for (int i = 0; i < jArray.length(); i++) {
			try {
				JSONObject jobject = jArray.getJSONObject(i);
				Debug.debugLog(" name=" + jobject.getString("name"));
				if (grade.equals(jobject.getString("name"))) {
					menus[0] = parseMenuJson(jobject);
				}
				if (jobject.getString("name").equals("综合")) {
					menus[1] = parseMenuJson(jobject);
				}
				Debug.debugLog("menus.length = " + menus.length);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return menus;
	}

	private MenuItem parseMenuJson(JSONObject jObject) {
		MenuItem menu = null;
		try {
			int id = jObject.getInt("id");
			String name = jObject.getString("name");
			int parentId = jObject.getInt("parent");
			JSONArray jArray = jObject.getJSONArray("children");
			menu = new MenuItem(id, parentId, name, jArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return menu;
	}

	public MenuItem getSelectedMenuItem() {
		return selMenuItem;
	}

	public void setOnClickContentsListen(
			IContentsItemChangeListen mClickContentsListen) {
		this.mClickContentsListen = mClickContentsListen;
	}

}
