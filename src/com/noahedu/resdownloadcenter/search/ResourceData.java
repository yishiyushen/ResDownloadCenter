package com.noahedu.resdownloadcenter.search;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.ucache.DownloadManager;

public class ResourceData {
	public int size;
	public int count;
	public int totalCount;
	public int pageNo;
	public int totalPage;
	public Props props;
	public ArrayList<Key> keys = new ArrayList<Key>();

	public ResourceData(String json, DownloadManager downloadManager, String function) {
		try {
			JSONObject jObject = new JSONObject(json);
			JSONObject obj = jObject.optJSONObject("data");
			if(obj == null){
				return;
			}
			size = obj.optInt("size");
			count = obj.optInt("count");
			totalCount = obj.optInt("totalCount");
			pageNo = obj.optInt("pageNo");
			totalPage = obj.optInt("totalPage");

			JSONObject propsObj = obj.optJSONObject("props");
			if (propsObj != null)
				props = new Props(propsObj);

			JSONArray keysArray = obj.optJSONArray("keys");
			if (keysArray != null) {
				for (int i = 0; i < keysArray.length(); i++) {
					Key key = new Key((JSONObject) keysArray.get(i), downloadManager, function);
					keys.add(key);
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		String str = "";
		str += "{size:" + size;
		str += ", count:" + count;

		str += ", keys:[";
		for (int i = 0; i < keys.size(); i++) {
			Key key = keys.get(i);
			str += key.toString();
			if (i < keys.size() - 1)
				str += ", ";
		}

		str += "], totalCount:" + totalCount;
		str += ", pageNo:" + pageNo;
		str += ", totalPage:" + totalPage;
		str += ", props:" + props.toString() + "}";

		return str;
	}

	public void writeTo(FileWriter fr) throws IOException {
		fr.write("{size:" + size + ", count:" + count + ", keys:[");

		for (int i = 0; i < keys.size(); i++) {
			Key key = keys.get(i);
			key.writeTo(fr);
			if (i < keys.size() - 1)
				fr.write(", ");
		}

		fr.write("], totalCount:" + totalCount);
		fr.write(", pageNo:" + pageNo);
		fr.write(", totalPage:" + totalPage + ", ");
		props.writeTo(fr);
		fr.write("}");
	}

}
