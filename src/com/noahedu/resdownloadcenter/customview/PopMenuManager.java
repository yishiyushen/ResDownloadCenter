package com.noahedu.resdownloadcenter.customview;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.noahedu.resdownloadcenter.R;
import com.noahedu.resdownloadcenter.ResLoadCenterMainActivity.IGetSearchArg;
import com.noahedu.resdownloadcenter.db.CheckUserInfo;
import com.noahedu.resdownloadcenter.db.CommDataAccess;
import com.noahedu.resdownloadcenter.net.HttpGetThread;
import com.noahedu.resdownloadcenter.net.Parameter;
import com.noahedu.resdownloadcenter.search.MenuItem;
import com.noahedu.resdownloadcenter.search.PopVData;
import com.noahedu.resdownloadcenter.search.SearchArg;
import com.noahedu.resdownloadcenter.search.StaticVar;
import com.noahedu.resdownloadcenter.util.Debug;



public class PopMenuManager implements IContentsItemChangeListen,IGetSearchArg{
	private LayoutInflater inflate;
	private SearchArg searchArg;
	private Activity context;
	public String curGrade; //当前学龄 是幼教，小学，中学
	private boolean bGradeVisble = true;
	public CheckUserInfo userInfo;
	

	
	private MenuItem curContentsSel;
	private IDoSearch mIDosearch;
	
	View viewMain;
	LinearLayout selected;

	ProgressBar progress;

	PopupWindow mPopWnd;
	GridLayout mGradeGridLayout;
	GridLayout mSubjectGridLayout;
	GridLayout mTermGridLayout;
	GridLayout mPublishGridLayout;
	GridLayout mVersionGridLayout;
	ScrollView mContainer;
	LinearLayout progressContainer;
	Drawable selectedDrawable;

	/**您已选择*/
	ArrayList<PopVData> listSelected = new ArrayList<PopVData>();
	/**年级*/
	ArrayList<PopVData> listGrade = new ArrayList<PopVData>();
	/**科目*/
	ArrayList<PopVData> listSubject = new ArrayList<PopVData>();
	/**学年*/
	ArrayList<PopVData> listTerm = new ArrayList<PopVData>();
	/**出版社*/
	ArrayList<PopVData> listPubliser = new ArrayList<PopVData>();
	/**版本*/
	ArrayList<PopVData> listVirsion = new ArrayList<PopVData>();
	
	TextView curSelGrade;
	TextView curSelSubject;
	TextView curSelTerm;
	TextView curSelPublish;
	
	
	SparseArray<Parameter> urlParameter = new SparseArray<Parameter>();  //接口参数
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case HttpStatus.SC_OK:
				String json = (String) msg.obj;
				parseJsonStr(json);
				createFilterDataView();
				break;
			case HttpStatus.SC_NOT_FOUND:		//处理获取失败
			case HttpStatus.SC_CONTINUE:
			case StaticVar.GET_NET_DATA_FAILED:
				Toast.makeText(context, "获取筛选数据失败", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};
	
	public PopMenuManager(Activity context,View popupView,PopupWindow popWnd) {
		// TODO Auto-generated constructor stub
		userInfo = CommDataAccess.getUserInfo(context);
		curGrade = userInfo.strUsersp;
		Debug.debugLog("curGrade="+curGrade);
		viewMain = popupView;
		this.context = context;
		searchArg = new SearchArg();
//		searchArg.gradeid = userInfo.strUsergrade;
		searchArg.productName= "U30";//Build.MODEL;
		mPopWnd = popWnd;
		mPopWnd.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				if(!btCliced){
					cancelPopupWind();
				}
			}
		});
		inflate = LayoutInflater.from(context);
		
		init();
		initView();
	}
	
    private void showContainer(View view, boolean isVisiable)
    {
        if (isVisiable)
        {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
        }
        else
        {
            view.setVisibility(View.GONE);
            view.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
        }
    }
	
    private void showContentContainer(boolean isVisiable)
    {
        showContainer(mContainer, isVisiable);
    }
    
    private void showProgressContainer(boolean isVisiable){
    	showContainer(progressContainer, isVisiable);
    }
    
    private TextView createPopTextView(){
    	TextView tv = new ConditionView(context);
    	tv.setTextAppearance(context, R.style.pop_data_txt_style);
    	return tv;
    }
    
	public void createViewsGrade(ArrayList<PopVData> list){
		mGradeGridLayout.removeAllViews();
		if(list != null){
			for(int i=0;i<list.size();i++){
				PopVData data = list.get(i);
				if(data != null){
					final TextView tv = createPopTextView();
					tv.setText(data.value);
					tv.setTag(i);
					tv.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							pressViewGrade(v);
						}
					});

                    GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                    p.leftMargin = 25;
                    mGradeGridLayout.addView(tv, p);
				}
			}
		}
	}
	
	 private void pressViewGrade(View v){
		int index = (Integer) v.getTag();
		if(curSelGrade != null&& curSelGrade.equals(v)){
			return;
		}
		mGradeGridLayout.removeAllViews();
		curSelGrade = (TextView) v;
		PopVData pp = listGrade.get(index);
		pp.bSelected = true;
		changeSelected(pp);
		getFilterDataFromNet();
		
		final TextView tv = createPopTextView();
		tv.setText(pp.value);
		tv.setTag(index);
		mGradeGridLayout.addView(tv);
	 }
	
	private void createViewsSubject(ArrayList<PopVData> list){
		mSubjectGridLayout.removeAllViews();
		
		if (list != null)
        {
            for (int i = 0, size = list.size(); i < size; i++)
            {
            	PopVData data = list.get(i);
                if (data != null)
                {
        			final TextView tv = createPopTextView();
					tv.setText(data.value);
					tv.setTag(i);
					tv.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							pressViewSubject(v);
						}
					});

                    
                    if (i == size - 1)
                    {
                        int row = (size - 1) / mSubjectGridLayout.getColumnCount();
                        int column = (size - 1) % mSubjectGridLayout.getColumnCount();
                        GridLayout.Spec rowSpec = GridLayout.spec(row);
                        int s = 2;
                        if (column + s >= mSubjectGridLayout.getColumnCount())
                        {
                            s = 1;
                        }
                        Debug.debugLog(" row = " + row + " column = " + column + " s = " + s);
                        GridLayout.Spec columnSpec = GridLayout.spec(column, s);
                        GridLayout.LayoutParams p = new GridLayout.LayoutParams(rowSpec, columnSpec);
                        p.leftMargin = 30;
                        mSubjectGridLayout.addView(tv, p);
                    }
                    else
                    {
                        GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                        p.leftMargin = 30;
                        mSubjectGridLayout.addView(tv, p);
                    }
                }
            }
        }
	}
	
	 private void pressViewSubject(View v){
		int index = (Integer) v.getTag();
		if(curSelSubject != null&& curSelSubject.equals(v)){
			return;
		}
		mSubjectGridLayout.removeAllViews();
		curSelSubject = (TextView) v;
		PopVData pp = listSubject.get(index);
		pp.bSelected = true;
		changeSelected(pp);
		getFilterDataFromNet();
		
		final TextView tv = createPopTextView();
		tv.setText(pp.value);
		tv.setTag(index);
		mSubjectGridLayout.addView(tv);
	 }
	
	private void createViewsTerm(ArrayList<PopVData> list){
		mTermGridLayout.removeAllViews();
		if(list != null){
			for(int i=0;i<list.size();i++){
				PopVData data = list.get(i);
				if(data != null){
					final TextView tv = createPopTextView();
					tv.setText(data.value);
					tv.setTag(i);
					tv.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							pressViewTerm(v);
						}
					});

                    GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                    p.leftMargin = 25;
                    mTermGridLayout.addView(tv, p);
				}
			}
		}
	}
	
	 private void pressViewTerm(View v){
		int index = (Integer) v.getTag();
		if(curSelTerm != null&& curSelTerm.equals(v)){
			return;
		}
		mTermGridLayout.removeAllViews();
		curSelTerm = (TextView) v;
		PopVData pp = listTerm.get(index);
		pp.bSelected = true;
		changeSelected(pp);
		getFilterDataFromNet();
		
		final TextView tv = createPopTextView();
		tv.setText(pp.value);
		tv.setTag(index);
		mTermGridLayout.addView(tv);
	 }
	
	private void createViewsPublish(ArrayList<PopVData> list){
		mPublishGridLayout.removeAllViews();
		if(list != null){
			for(int i=0;i<list.size();i++){
				PopVData data = list.get(i);
				if(data != null){
					final TextView tv = createPopTextView();
					tv.setText(data.value);
					tv.setTag(i);
					tv.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							pressViewPublish(v);
						}
					});

                    mPublishGridLayout.addView(tv);
				}
			}
		}
	}
	
	 private void pressViewPublish(View v){
		int index = (Integer) v.getTag();
		if(curSelPublish != null&& curSelPublish.equals(v)){
			return;
		}
		mPublishGridLayout.removeAllViews();
		curSelPublish = (TextView) v;
		PopVData pp = listPubliser.get(index);
		pp.bSelected = true;
		changeSelected(pp);
		getFilterDataFromNet();
		
		final TextView tv = createPopTextView();
		tv.setText(pp.value);
		tv.setTag(index);
		mPublishGridLayout.addView(tv);
	 }
	
	private void createFilterDataView(){
		if(curSelGrade == null){
			createViewsGrade(listGrade);
		}
		if(curSelSubject == null)
			createViewsSubject(listSubject);
		if(curSelTerm == null)
			createViewsTerm(listTerm);
		if(curSelPublish == null)
			createViewsPublish(listPubliser);
	}
	
	public void getFilterDataFromNet(){
		String url = getFilterUrl();
		Debug.debugLog("PopMenu url="+url);
		//setProgressBarVisible(true);
		showProgressContainer(true);
//		showContentContainer(false);
		new Thread(new HttpGetThread(handler, url)).start();
	}
	

	
	private void parseJsonStr(String json){
		Debug.debugLog("-----json = "+json);
		try {

			JSONObject jObj = new JSONObject(json);
			
			if(jObj.getInt("msgCode")!=302){
				Message msg = handler.obtainMessage(StaticVar.GET_NET_DATA_FAILED);
				handler.sendMessage(msg);
				Debug.debugLog("-----msgCode != 302");
			}
			JSONObject jdata = jObj.getJSONObject("data");
			/**年级*/
			JSONArray jGrade = jdata.getJSONArray("grade");
			listGrade.clear();

			for(int i=0; i<jGrade.length();i++){
				JSONObject jgrade = jGrade.getJSONObject(i);
				PopVData pop = new PopVData();
				pop.bSelected = false;
				pop.key = jgrade.getInt("key");
				pop.value = jgrade.getString("value");
				pop.remark = jgrade.getString("remark");
				pop.type = PopVData.TYPE_GRADE;
//				if(searchArg.gradeid!=null && Integer.parseInt(searchArg.gradeid) == pop.key){
//					pop.bSelected = true;
//					changeSelected(pop);
//				}

				listGrade.add(pop);
			}
			
			/**学科*/
			JSONArray jSubject = jdata.getJSONArray("subject");
			listSubject.clear();
			

			
			for(int i=0;i<jSubject.length();i++){
				JSONObject jsub = jSubject.getJSONObject(i);
				int key = jsub.getInt("key");
				String name = jsub.getString("value");
				PopVData pop =new PopVData(name, false, PopVData.TYPE_SUBJECT,key);
				pop.remark = jsub.getString("remark");
//				if(searchArg.subjectid!=null&& Integer.parseInt(searchArg.subjectid) == pop.key){
//					pop.bSelected = true;
//					changeSelected(pop);
//				}
				listSubject.add(pop);
			}
			/**学期*/
			JSONArray jTerm = jdata.getJSONArray("term");
			listTerm.clear();
			
			for(int i=0; i<jTerm.length();i++){
				JSONObject jterm = jTerm.getJSONObject(i);
				PopVData pop = new PopVData();
				pop.bSelected = false;
				pop.key = jterm.getInt("key");
				pop.value = jterm.getString("value");
				pop.remark = jterm.getString("remark");
				pop.type = PopVData.TYPE_TERM;
//				if(searchArg.termid!=null && Integer.parseInt(searchArg.termid) == pop.key){
//					pop.bSelected = true;
//					changeSelected(pop);
//
//				}
				listTerm.add(pop);
			}
			/**出版社*/
			JSONArray jPublisher = jdata.getJSONArray("publish");
			listPubliser.clear();

			for(int i=0;i<jPublisher.length();i++){
				JSONObject jpub = jPublisher.getJSONObject(i);
				PopVData pop = new PopVData();
				pop.bSelected = false;
				pop.key = jpub.getInt("key");
				pop.value = jpub.getString("value");
				pop.remark = jpub.getString("remark");
				pop.type = PopVData.TYPE_PUBLISHER;
//				if(searchArg.publishid!=null && Integer.parseInt(searchArg.publishid) == pop.key){
//					pop.bSelected = true;
//					changeSelected(pop);
//				}
				listPubliser.add(pop);
			}
//			/**版本*/
//			JSONArray jBokEdi = jdata.getJSONArray("bokedition");
//			listVirsion.clear();
//			
//			for(int i=0; i<jBokEdi.length();i++){
//				JSONObject jbok = jBokEdi.getJSONObject(i);
//				PopVData pop = new PopVData();
//				pop.bSelected = false;
//				pop.key = jbok.getInt("key");
//				pop.value = jbok.getString("value");
//				pop.remark = jbok.getString("remark");
//				pop.type = PopVData.TYPE_BOKEDITION;
//				if(searchArg.bokeditionid!=null && Integer.parseInt(searchArg.bokeditionid) == pop.key){
//					pop.bSelected = true;
//			changeSelected(pop);
//				}
//				listVirsion.add(pop);
//			}
				
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**显示已选择的*/
		showSelected();
		showProgressContainer(false);
		showContentContainer(true);



	}
	

	
	private String getFilterUrl(){
		StringBuilder builder = new StringBuilder(StaticVar.FILTER_MENU_URL);
		builder.append("?");
		builder.append("productName="+"U30");//);
		builder.append("&");
		for(int i=0; i<urlParameter.size();i++){
			Parameter parameter = urlParameter.get(i);
			builder.append(parameter.getName());
			builder.append("=");
			builder.append(parameter.getValue());
			if(i<urlParameter.size()-1){
				builder.append("&");
			}
		}
		return builder.toString();
	}
	
	private void init(){
		
		 selectedDrawable = context.getResources().getDrawable(R.drawable.bt_select);
		
	}
	
	private void initView(){
		
		 
		selected = (LinearLayout)viewMain.findViewById(R.id.selected_layout);

		progress = (ProgressBar)viewMain.findViewById(R.id.progressBar);
		Button okBt = (Button)viewMain.findViewById(R.id.ok);
		Button cancelBt = (Button)viewMain.findViewById(R.id.cancel);
		
		PopVClickListen clickListen = new PopVClickListen();
		okBt.setOnClickListener(clickListen);
		cancelBt.setOnClickListener(clickListen);
		

		
		mGradeGridLayout = (GridLayout)viewMain.findViewById(R.id.layout_grade);
		mSubjectGridLayout = (GridLayout)viewMain.findViewById(R.id.layout_subject);
		mTermGridLayout = (GridLayout)viewMain.findViewById(R.id.layout_term);
		mPublishGridLayout = (GridLayout)viewMain.findViewById(R.id.layout_publish);
		mContainer = (ScrollView)viewMain.findViewById(R.id.container_layout);
		progressContainer = (LinearLayout)viewMain.findViewById(R.id.progress_content);
		/**显示已选择的*/
//		showSelected();
		showContentContainer(false);
		showProgressContainer(true);
	}
	
	
	private void showSelected(){
		
		selected.removeAllViews();
//		TextView label = new TextView(context);
//		label.setText("您已选择:");
//		label.setTextSize(26f);
//		label.setTextColor(context.getResources().getColor(R.color.popview_txt_color));
//		selected.addView(label);
		for(int i=0;i< listSelected.size();i++){
			TextView tv = new TextView(context);
			tv.setClickable(true);
			PopVData data = listSelected.get(i);
			tv.setText(data.value);
			tv.setTextSize(22f);
			tv.setTextColor(context.getResources().getColor(R.color.popview_txt_color));
			tv.setTag(data);
			tv.setBackgroundResource(R.drawable.txt_select_bk);
			tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.delete_txt, 0);
			tv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PopVData data = (PopVData) v.getTag();
					data.bSelected = false;
					selected.removeView(v);
					listSelected.remove(data);
					if(data.type == PopVData.TYPE_GRADE){
						curSelGrade = null;
					}else if(data.type == PopVData.TYPE_SUBJECT){
						curSelSubject = null;
					}else if(data.type == PopVData.TYPE_TERM){
						curSelTerm = null;
					}else if(data.type == PopVData.TYPE_PUBLISHER){
						curSelPublish = null;
					}else if(data.type == PopVData.TYPE_BOKEDITION){
						
					}
					getFilterDataFromNet();
				}
			});
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.leftMargin = 15;
			selected.addView(tv,params);
		}
	}
	
	private void setProgressBarVisible(boolean bVisible){
		if(bVisible){
			 selected.setVisibility(View.INVISIBLE);

			 if(progress != null)
				 progress.setVisibility(View.VISIBLE);;
		}else{
			 selected.setVisibility(View.VISIBLE);

			 if(progress != null)
				 progress.setVisibility(View.INVISIBLE);;
		}
	}
	
	private void changeSelected(PopVData data){
		PopVData removData = null;
		int index = -1;
		for(int i=0;i< listSelected.size();i++){
			PopVData da = listSelected.get(i);
			if(da.type == data.type){
				removData = da;
				index = i;
			}
		}
		
		if(removData != null){
			listSelected.remove(removData);
		}
		if(index != -1){
			listSelected.add(index, data);
		}else{
			listSelected.add(data);
		}
		/**显示已选择的*/
		showSelected();
	}
	
	@Override
	public void onClickContentsItem(MenuItem menu) {
		// TODO Auto-generated method stub
		curContentsSel = menu;
		Parameter parameter = new Parameter("apid",String.valueOf(menu.id));
		urlParameter.put(PopVData.TYPE_CONTENTS, parameter);
		getFilterDataFromNet();
//		searchArg.reset();
		searchArg.function = String.valueOf(menu.id);
		Debug.debugLog("onClickContentsItem="+searchArg.getSearchResourceUrl());
		if(mIDosearch != null){
			mIDosearch.doSearch(true);
		}
	}
	
	
	
	@Override
	public void onContentsBackUpLevel(MenuItem menu) {
		// TODO Auto-generated method stub
		curContentsSel = menu;
		Parameter parameter = new Parameter("apid",String.valueOf(menu.id));
		urlParameter.put(PopVData.TYPE_CONTENTS, parameter);
		getFilterDataFromNet();
		searchArg.reset();
		searchArg.function = String.valueOf(menu.id);
	}
	
	
	public SearchArg getSearchArg() {
		return searchArg;
	}

	
	public void setmIDosearch(IDoSearch mIDosearch) {
		this.mIDosearch = mIDosearch;
	}


	public interface IDoSearch{
		public void doSearch(boolean dosearch);
	}
	

	public void cancelPopupWind(){
		mPopWnd.dismiss();

	}
	
	private boolean btCliced = false;
	
	public void setBtClicke(boolean bflage){
		btCliced = bflage;
	}
	private class PopVClickListen implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.ok:
				btCliced = true;
				mPopWnd.dismiss();
				searchArg.reset();
				if(curContentsSel == null){
					break;
				}
				searchArg.function = String.valueOf(curContentsSel.id);
				for(int i=0;i<listSelected.size();i++){
					 PopVData data = listSelected.get(i);
					 String id = String.valueOf(data.key);
					 
					if(data.type ==PopVData.TYPE_GRADE){
						searchArg.gradeid = id ;
					}else if(data.type == PopVData.TYPE_SUBJECT){
						searchArg.subjectid = id ;
					}else if(data.type == PopVData.TYPE_TERM){
						searchArg.termid = id;
					}else if(data.type == PopVData.TYPE_PUBLISHER){
						searchArg.publishid = id;
					}else if(data.type == PopVData.TYPE_BOKEDITION){
						searchArg.bokeditionid = id;
					}
					searchArg.gradeName = null;

				}
				
				//进行搜索
				if(mIDosearch != null && listSelected.size() != 0){
					mIDosearch.doSearch(true);
				}
				break;
			case R.id.cancel:
				btCliced = true;
				cancelPopupWind();
				break;
			}
		}
	}
	
	 private class ConditionView extends TextView
	    {
	        
	        public ConditionView(Context context, AttributeSet attrs)
	        {
	            super(context, attrs);
	            // TODO Auto-generated constructor stub
	            init();
	        }
	        
	        public ConditionView(Context context)
	        {
	            super(context);
	            // TODO Auto-generated constructor stub
	            init();
	        }
	        
	        private View.OnClickListener onPressUpListener;
	        
	        public void setOnClickListener(View.OnClickListener onClickListener)
	        {
	            this.onPressUpListener = onClickListener;
	        }
	        
	        private void init()
	        {
	            setTextAppearance(getContext(), R.style.condition_text);
	            
	            setOnTouchListener(new View.OnTouchListener()
	            {
	                
	                @Override
	                public boolean onTouch(View v, MotionEvent event)
	                {
	                    // TODO Auto-generated method stub
	                    // log(" Action = " + event.getAction());
	                    switch (event.getAction())
	                    {
	                        case MotionEvent.ACTION_DOWN:
	                            setTextAppearance(getContext(), R.style.condition_text_selected);
	                            return true;
	                            
	                        case MotionEvent.ACTION_UP:
	                            setTextAppearance(getContext(), R.style.condition_text);
	                            if (onPressUpListener != null)
	                            {
	                                onPressUpListener.onClick(v);
	                            }
	                            return true;
	                        case MotionEvent.ACTION_CANCEL:
	                            setTextAppearance(getContext(), R.style.condition_text);
	                            break;
	                        
	                        case MotionEvent.ACTION_MOVE:
	                            
	                            break;
	                        
	                        default:
	                            break;
	                    }
	                    return false;
	                }
	            });
	        }
	        
	    }
}
