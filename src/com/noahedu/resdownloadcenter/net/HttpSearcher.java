package com.noahedu.resdownloadcenter.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.noahedu.resdownloadcenter.search.ResourceData;
import com.noahedu.resdownloadcenter.search.SearchArg;
import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.ucache.DownloadManager;

public class HttpSearcher {

	private static final int timeout = 10000;
	
	private String readContentFromGet(String link) throws IOException {
		Debug.debugLog();
		HttpURLConnection connection = null;
		InputStream is = null;
		String str = "";

		try {
			URL getUrl = new URL(link);
			Debug.debugLog(getUrl.toString());
			connection = (HttpURLConnection) getUrl.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			Debug.debugLog("before connect");
			connection.connect();
			Debug.debugLog("after connect");
			is = connection.getInputStream();
			Debug.debugLog("after getInputStream");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int nRead;
			byte[] buffer = new byte[1024];
			while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {
				Debug.debugLog("nRead="+nRead);
				bos.write(buffer, 0, nRead);
			}
			bos.flush();
			
			str = bos.toString();
			bos.close();
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		Debug.debugLog(str);
		return str;
	}

	public ResourceData start(SearchArg arg, DownloadManager downloadManager) throws IOException {
		String str = new HttpMyGet().doGet(arg.getSearchResourceUrl());// readContentFromGet(arg.getSearchResourceUrl());
		ResourceData data = new ResourceData(str, downloadManager, arg.function);
		return data;
	}
}
