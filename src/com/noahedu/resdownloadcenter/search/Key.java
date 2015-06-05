package com.noahedu.resdownloadcenter.search;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.ucache.DownloadManager;
import com.noahedu.ucache.LoadTaskInfo;

public class Key implements Parcelable{

	public String name = ""; //课件名
	public String id = "";	 //课件id
	public String type = ""; // 模块名
	public String typeReal = "";
	public String book = ""; //书名
	public String description = ""; //描述
	public String subject = ""; // 科目
	public String grade = ""; // 年级
	public String term = "";  //学期
	public String press = ""; //出版社
	public String version = ""; //参考版本
	public String products = "";//适用机型
	public String updateTime = "";//更新时间
	public String function = "";  //目录id
	public String fileName = "";  //文件名
	public int fileSize = 0;	  //文件大小 字节
	public String bokcoverurl = ""; //资源封面图片链接
	public String bokbackcoverurl = ""; 
	public String sourtypeico = "";
	public String souraddr = "";    //资源下载地址
	public String sfileSize = "";   //资源大小 m单位
	public String md5Code = "";
	public String url = ""; //拼接好的资源下载地址
	public String typeID = ""; //资源类型id  小学同步 451 中学同步82
	
	
	

	public LoadTaskInfo info;
	

	
	public Key(JSONObject obj, DownloadManager downloadManager, String function) {
		if (obj == null)
			return;

		try {
			this.function = function;
			name = new String(obj.optString("name").getBytes("UTF-8"), "UTF-8");
			if (name.contains("<em>")) {
				name = name.replaceAll("<[/]?em>", "");
			}
			id = new String(obj.optString("id").getBytes("UTF-8"), "UTF-8");
			souraddr = new String(obj.optString("souraddr").getBytes("UTF-8"), "UTF-8");

			type = new String(obj.optString("type").getBytes("UTF-8"), "UTF-8");

			type = type.replaceAll("<[/]?em>", "");
			typeReal = type;

			if (type.contains("实验室")) {
				typeReal = type.replaceAll("flash.*实验室", "实验室");
				// type = "理化实验室";
			}

			if (type.contains("精灵")) {
				typeReal = type.replaceAll("精灵.*漫游记", "精灵漫游记");
				// 精灵奥数漫游记和阶梯奥数冲突
				type = typeReal;
			}

			if (type.contains("奥数")) {
				typeReal = "阶梯奥数";
				// type = typeReal;
			}

			if (type.contains("分级词汇")) {
				typeReal = "黄金单词";
				// type = typeReal;
			}

			if (type.contains("词典")) {
				typeReal = "诺亚词霸";
			}

			if (type.contains("作文")) {
				typeReal = "魔法作文";
			}

			if (type.contains("冒险")) {
				typeReal = "大冒险";
			}

			if (type.contains("搜学")) {
				typeReal = "万维搜学";
			}

			if (type.contains("语法") && name.contains("初中")) {
				typeReal = "初中英语语法";
			}

			if (type.contains("语法") && name.contains("高中")) {
				typeReal = "高中英语语法";
			}

			// if (type.contains("<em>")) {
			// Pattern pattren = Pattern.compile(regExString);
			// Matcher matcher = pattren.matcher(type);
			// StringBuffer buffer = new StringBuffer();
			// while (matcher.find()) {
			// buffer.append(matcher.group());
			// }
			// type = type.replaceAll("<[/]?em>", "");
			// // typeReal = buffer.toString().replaceAll("<[/]?em>", "");
			// } else {
			// typeReal = type;
			// }
			book = new String(obj.optString("book").getBytes("UTF-8"),"UTF-8");
			description = new String(obj.optString("description").getBytes("UTF-8"),"UTF-8");
			subject = new String(obj.optString("subject").getBytes("UTF-8"), "UTF-8");
			subject = subject.replaceAll("<[/]?em>", "");
			grade = new String(obj.optString("grade").getBytes("UTF-8"), "UTF-8");
			term = new String(obj.optString("term").getBytes("UTF-8"), "UTF-8");
			press = new String(obj.optString("press").getBytes("UTF-8"), "UTF-8");
			version = new String(obj.optString("version").getBytes("UTF-8"), "UTF-8");
			products = new String(obj.optString("products").getBytes("UTF-8"), "UTF-8");
			updateTime = new String(obj.optString("updateTime").getBytes("UTF-8"), "UTF-8");
			fileSize = obj.optInt("fileSize");
			fileName = new String(obj.optString("fileName").getBytes("UTF-8"), "UTF-8");
			md5Code = new String(obj.optString("md5Code").getBytes("UTF-8"), "UTF-8");
			bokcoverurl = new String(obj.optString("bokcoverurl").getBytes("UTF-8"), "UTF-8");
			bokbackcoverurl = new String(obj.optString("bokbackcoverurl").getBytes("UTF-8"), "UTF-8");
			sourtypeico = new String(obj.optString("sourtypeico").getBytes("UTF-8"), "UTF-8");
			sfileSize = new String(obj.optString("sfileSize").getBytes("UTF-8"), "UTF-8");
			url = new String(obj.optString("url").getBytes("UTF-8"), "UTF-8");
			typeID = new String(obj.optString("typeID").getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			Debug.debugLog("before get task info");
			if (downloadManager != null) {
				if (url != null && url.length() > 0) {
					// info = downloadManager.getTaskInfoUrl(SearchArgResolver.mDownloadURLPrefix_CDN + Uri.encode(souraddr));
					info = downloadManager.getTaskInfoUrl(url);
//					Debug.debugLog("key -----  info=   "+info);
				} else {
					//info = downloadManager.getTaskInfoUrl(SearchArgResolver.mDownloadURLPrefix + Uri.encode(id));
				}
			}
			Debug.debugLog("after get task info");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public Key(Parcel source){
		name = source.readString();
		id = source.readString();
		type = source.readString();
		typeReal = source.readString();
		book = source.readString();
		description = source.readString();
		subject = source.readString();
		grade = source.readString();
		term = source.readString();
		press = source.readString();
		version = source.readString();
		products = source.readString();
		updateTime = source.readString();
		function = source.readString();
		fileName = source.readString();
		fileSize = source.readInt();
		bokcoverurl = source.readString();
		bokbackcoverurl = source.readString();
		sourtypeico = source.readString();
		souraddr = source.readString();
		sfileSize = source.readString();
		md5Code = source.readString();
		url = source.readString();
		typeID = source.readString();
		info = source.readParcelable(LoadTaskInfo.class.getClassLoader());
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "\n\t{name:" + name;
		str += ", id:" + id;
		str += ", souraddr:" + souraddr;
		str += ", type:" + type;
		str += ", typeReal:" + typeReal;
		str += ", function:" + function;
		str += ", fileName:" + fileName;
		 str += ", version:" + version;
		 str += ", term:" + term;
		str += ", subject:" + subject;
		 str += ", updateTime:" + updateTime;
		str += ", grade:" + grade;
		str += ", fileSize:" + fileSize;
		str += ", press:" + press;
		 str += ", sourtypeico:" + sourtypeico;
		 str += ", bokbackcoverurl:" + bokbackcoverurl;
		 str += ", bokcoverurl:" + bokcoverurl;
		 str += ", md5Code:" + md5Code;
		str += ", sfileSize:" + sfileSize + "}\n";
		return str;
	}

	public void writeTo(FileWriter fr) throws IOException {
		fr.write(toString());
	}
	

	
	public static final Parcelable.Creator<Key> CREATOR = new Parcelable.Creator<Key>(){
		public Key createFromParcel(Parcel source) {
			return new Key(source);
		};
		
		public Key[] newArray(int size) {
			return new Key[size];
		};
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(name); //课件名
		dest.writeString(id);	 //课件id
		dest.writeString( type); // 模块名
		dest.writeString(typeReal);
		dest.writeString(book ); //书名
		dest.writeString(description); //描述
		dest.writeString(subject); // 科目
		dest.writeString(grade); // 年级
		dest.writeString(term);  //学期
		dest.writeString(press); //出版社
		dest.writeString(version); //参考版本
		dest.writeString(products);//适用机型
		dest.writeString(updateTime);//更新时间
		dest.writeString(function);  //目录id
		dest.writeString(fileName);  //文件名
		dest.writeInt(fileSize);	  //文件大小 字节
		dest.writeString(bokcoverurl); //资源封面图片链接
		dest.writeString(bokbackcoverurl); 
		dest.writeString(sourtypeico);
		dest.writeString(souraddr);    //资源下载地址
		dest.writeString(sfileSize);   //资源大小 m单位
		dest.writeString(md5Code);
		dest.writeString(url); //拼接好的资源下载地址
		dest.writeString(typeID); //资源类型id  小学同步 451 中学同步82
		dest.writeParcelable(info, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
	}
	

}
