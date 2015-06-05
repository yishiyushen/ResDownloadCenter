package com.noahedu.resdownloadcenter.search;

import org.json.JSONArray;

public class MenuItem {
	public int id; 		  //科目ID
	public int parentId;  //父科目ID
	public String name;   //科目名称
	public JSONArray childJson; //子科目JSon数据
	
	public MenuItem(){
		
	}
	
	public MenuItem(int id, int parentId, String name, JSONArray childJson) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.childJson = childJson;
	}

	
}
