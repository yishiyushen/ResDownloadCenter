package com.noahedu.resdownloadcenter.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.noahedu.resdownloadcenter.util.Debug;

public class HttpMyGet {
	public String doGet(String url)throws ConnectTimeoutException, ClientProtocolException, IOException{
		String response = null; //返回信息
		HttpGet httpGet = new HttpGet(url);
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 50*1000);
		HttpConnectionParams.setSoTimeout(httpParams, 50*1000);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpResponse res = httpClient.execute(httpGet);
		int statusCode = res.getStatusLine().getStatusCode();
		if(statusCode == HttpStatus.SC_OK){
			response = EntityUtils.toString(res.getEntity(),HTTP.UTF_8);
		}else{
			response = "返回码："+statusCode;
		}
		
		return response;
	}
	
	public  String httpGet(String url) throws IOException {
		Debug.debugLog();
//		String urlString = LINK + "?productname=" + Build.MODEL.toUpperCase() + "&sourid=" + URLEncoder.encode(id);
//		String urlString = LINK + "?productname=" + "U6S" + "&sourid=" + URLEncoder.encode(id);
		String urlString = url;
		HttpURLConnection connection = null;
		InputStream is = null;
		String str = "";
		try {
			URL getUrl = new URL(urlString);
			Debug.debugLog(getUrl.toString());
			connection = (HttpURLConnection) getUrl.openConnection();
			connection.setConnectTimeout(50000);
			connection.setReadTimeout(50000);
			Debug.debugLog("before connect");
			connection.connect();
			Debug.debugLog("after connect");
			is = connection.getInputStream();
			Debug.debugLog("after getInputStream");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int nRead;
			byte[] buffer = new byte[1024];
			while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {
				Debug.debugLog("nRead=" + nRead);
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
}
