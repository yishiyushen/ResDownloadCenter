package com.noahedu.resdownloadcenter.search;

import android.os.Build;

public class SearchArg {
	public String gradeid;// 年级
	public String subjectid;// 科目
	public String termid;   // 学年
	public String publishid;// 出版社
	public String function;// 模块 id
	public String bokeditionid; //版本id;
	public String barcode; //条形码
	public String productName =Build.MODEL;// 产品型号
	public String keyword;// 关键字
	public int pageNo = 1;// 页码序号
	public int pageSize = 10;// 每页显示的条目数量
	public String apid;
	public String functionName; //模块名称
	public String gradeName; //年级名称
	public String subjectName; //科目名称
	public String termName; //学期名称

	public void reset() {
		gradeid = null;
		subjectid = null;
		termid = null;
		publishid = null;
		function = null;
		bokeditionid = null;
		barcode = null;
		keyword = null;
		pageNo = 1;
		pageSize = 10;
		apid = null;
		productName ="U30";// Build.MODEL;
		functionName = null;
		gradeName = null;
		subjectName = null;
		termName = null;

	}
	
	public String getSearchResourceUrl(){
		String url = null;
		StringBuilder builder = new StringBuilder(StaticVar.SEARCH_RESOURCE_URL);
		builder.append("?productName="+productName/*Build.ID*/);
		builder.append("&pageNo="+pageNo);
		builder.append("&pageSize="+pageSize);
		if(barcode != null){
			builder.append("&barcode="+barcode);
			return builder.toString();
		}
		if(gradeid != null){
			builder.append("&gradeid="+gradeid);
		}
		if(subjectid != null){
			builder.append("&subjectid="+subjectid);
		}
		if(termid != null){
			builder.append("&termid="+termid);
		}
		if(publishid != null){
			builder.append("&publishid="+publishid);
		}
		if(function != null){
			builder.append("&function="+function);
		}
		if(bokeditionid != null){
			builder.append("&bokeditionid="+bokeditionid);
		}

		if(keyword != null){
			builder.append("&keyword="+keyword);
		}
		if(apid != null){
			builder.append("&apid="+apid);
		}


		return builder.toString();
	}
	

}
