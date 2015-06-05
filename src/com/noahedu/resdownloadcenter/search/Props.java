package com.noahedu.resdownloadcenter.search;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONObject;

public class Props {
	public String time = "";
	public String sTotalCount = "";

	public Props(JSONObject obj) {
		try {
			time = new String(obj.optString("time").getBytes("UTF-8"), "UTF-8");
			sTotalCount = new String(obj.optString("sTotalCount").getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		String str = "";
		str += "{time:" + time;
		str += ", sTotalCount:" + sTotalCount + "}";
		return str;
	}

	public void writeTo(FileWriter fr) throws IOException {
		fr.write(toString());
	}
}
