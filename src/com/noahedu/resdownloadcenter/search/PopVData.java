package com.noahedu.resdownloadcenter.search;

public class PopVData {
	public static int TYPE_GRADE = 1;
	public static int TYPE_SUBJECT = 2;
	public static int TYPE_TERM = 3;
	public static int TYPE_PUBLISHER = 4;
	public static int TYPE_BOKEDITION = 5;
	public static int TYPE_CONTENTS = 0;
	
	public String value; //列表项文字内容
	public boolean bSelected = false; //是否被选中
	public int type;
	public int key;
	public String remark; //备注

	
	public PopVData() {
		super();
	}


	public PopVData(String name, boolean bSelected,int type,int key) {
		super();
		this.value = name;
		this.bSelected = bSelected;
		this.type = type;
		this.key = key;
	}
	
}
