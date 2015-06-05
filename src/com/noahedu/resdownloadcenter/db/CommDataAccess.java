package com.noahedu.resdownloadcenter.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;



public class CommDataAccess {
	public static final String TNAME = "personalinfo";
	public static final String AUTOHORITY = "com.noahedu.provider.personalinfo";

	public static CheckUserInfo getUserInfo(Context context){
		CheckUserInfo info = null;
		ContentResolver contentResolver = context.getContentResolver();
		Uri cu = Uri.parse("content://" + AUTOHORITY + "/" + TNAME);
		Cursor cursor = contentResolver.query(cu, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			int realname = cursor.getColumnIndex("realname");
			int uid = cursor.getColumnIndex("uid");
			int id = cursor.getColumnIndex("_id");
			
			int usersp = cursor.getColumnIndex("edu_stage");
			int usergrade = cursor.getColumnIndex("grade_id");
			
			String strName = cursor.getString(realname);
			String strUId = cursor.getString(uid);
			String strTmpUId = cursor.getString(id);
			
			String strUsersp = cursor.getString(usersp);
			String strUsergrade = cursor.getString(usergrade);
			
			info = new CheckUserInfo();
			info.strName = strName;
 			info.strUId = strUId;
			info.strId = strUsersp;
			
			info.strUsersp = strUsersp;
			info.strUsergrade = strUsergrade;
		}
		
		if(cursor != null){
			cursor.close();
			cursor = null;
		}
		
		return info;
		
	}
}
