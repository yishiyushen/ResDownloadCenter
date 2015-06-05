package com.noahedu.resdownloadcenter.customview;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.widget.Toast;

import com.noahedu.noahfile.NoahFileUtil;
import com.noahedu.resdownloadcenter.net.HttpMyGet;
import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.resdownloadcenter.search.StaticVar;
import com.noahedu.resdownloadcenter.util.Debug;
import com.noahedu.ucache.DownloadManager;
import com.noahedu.ucache.LoadTaskInfo;




public class LoadDataManager implements IDownLoadListen {
	
	private DownloadManager downloadManager;
	private Context mContext;
	public LoadDataManager(Context context){
		this.mContext = context;
	}
	
	public void setDownloadManager(DownloadManager downloadManager){
		this.downloadManager = downloadManager;
	}
	
	@Override
	public void download(Key key) {
		// TODO Auto-generated method stub
		if (key.info == null) {
			newTask(key);
		} else {
			Debug.debugLog("before start task key.info.fileid="
					+ key.info.fileid);
			// try {
			// downloadManager.startTask(key.info.fileid);
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			new StartTaskThread(key).start();
			Debug.debugLog("after start task");
		}
	}

	@Override
	public void pauseDown(Key key) {
		// TODO Auto-generated method stub
		Debug.debugLog("key=" + key + ", key.info=" + key.info);
		if (key.info == null) {
			// do nothing
		} else {
			Debug.debugLog("before pause task key.info.fileid="
					+ key.info.fileid);
			// try {
			// downloadManager.pauseTask(key.info.fileid);
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			new PauseTaskThread(key).start();
			Debug.debugLog("after pause task");
		}
	}

	@Override
	public void cancelDown(Key key) {
		// TODO Auto-generated method stub
		Debug.debugLog("key=" + key + ", key.info=" + key.info);
		if (key.info == null) {
			// do nothing
		} else {
			Debug.debugLog("before cancel task key.info.fileid="
					+ key.info.fileid);
			// try {
			// downloadManager.pauseTask(key.info.fileid);
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			new RemoveTaskThread(key).start();
			Debug.debugLog("after cancel task");
		}
	}

	@Override
	public void update(Key key) {
		// TODO Auto-generated method stub
		try {
			if (key != null && key.info != null)
				key.info = downloadManager.getTaskInfo(key.info.fileid);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void openfile(String fn) {

		File f = new File(fn);
		if (!f.exists()) {
			Toast.makeText(mContext, "文件已经不存在，无法打开", Toast.LENGTH_SHORT).show();
			return;
		}

		Intent intent = new Intent();

		// 跳出列表供选择
		String type = "*/*";
		String actionname = "android.intent.action.";
		Boolean noahFlag = false;

		type = NoahFileUtil.getMIMEType(f, true);
		Debug.debugLog("type="+type);
		if (type.contains("application/x-")) {
			actionname += (NoahFileUtil.getMIMEType(f, false));
			actionname += "ExplorerView";
			noahFlag = true;
		} else {
			actionname = "android.intent.action.VIEW";
			noahFlag = false;
		}

		Debug.debugLog("actionname=" + actionname + ", type=" + type);
		if (type.compareTo("*") == 0 || type.compareTo("*/*") == 0) {
			Toast.makeText(mContext, "不支持此类型文件，无法打开", Toast.LENGTH_SHORT)
					.show();
			noahFlag = false;
			return;
		}

		// 设置intent的file与MimeType
		intent.setAction(actionname);
		intent.putExtra("ExplorerOpen", noahFlag);
		intent.setDataAndType(Uri.fromFile(f), type);
		try {
			mContext.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(mContext, "不支持此类型文件，无法打开.", Toast.LENGTH_SHORT)
					.show();
			noahFlag = false;
			return;
		}

	}
	
	private void newTask(final Key key) {
		// 启线程的目的是防止downloadAll的时候卡住后面的队列以及UI线程
		new Thread() {
			public void run() {
				try {
					String url = "";
					if (key.url != null && key.url.length() > 0) {
						// url = SearchArgResolver.mDownloadURLPrefix_CDN +
						// Uri.encode(key.souraddr);
						url = StaticVar.GET_BOOK_SV_DIR+"?productname="+"U30"+"&sourid="+key.id;
					} else {
						return;
					}
					String dir = getDirFromNet(key,url);
					if (dir == null || dir.length() == 0)
						return;
					Debug.debugLog("before new task. key.url=" + key.url + ", dir="
							+ dir);
					key.info = downloadManager
							.newTaskKjID(key.url, "", dir, key.id);
					Debug.debugLog("after new task. key.info=" + key.info);

					if (key.info != null) {
						Debug.debugLog("before start task key.info.fileid="
								+ key.info.fileid);
						downloadManager.startTask(key.info.fileid);
						Debug.debugLog("after start task");
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	@Override
	public LoadTaskInfo getTaskInfoUrl(String url) {
		// TODO Auto-generated method stub
		if(url == null){
			return null;
		}
		
		LoadTaskInfo info = null ;
		try {
			info = downloadManager.getTaskInfoUrl(url);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info;
	}
	
	private String getDirFromNet(Key key, String url){
		String id = key.id;
		String dir = key.function;
		String subject = key.subject;
		String function = key.function;
		String typeReal = key.typeReal;
		String result ;
		if (id != null && id.length() > 0) {
			dir = "";
			for (int i = 1; i <= 5; i++) {
				try {
					Thread.sleep(100);
					Debug.debugLog("try times: " + i);
					result = new HttpMyGet().doGet(url);
					dir = paseDirDataFromJson(result);
					if(dir.length() > 0)
						break;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			dir = dir.replaceAll("\\\\", "/");
			Debug.debugLog("dir=[" + dir + "]");

			return dir;
		}
		

		Debug.debugLog("function=" + function + ", subject=" + subject);
		if (typeReal.equals("动漫课堂")) {
			dir = typeReal;
			if (subject.equals("语文")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("英语")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("数学")) {
				dir += "/" + subject + typeReal;
			}
		} else if (typeReal.equals("精品课件")) {
			dir = typeReal;
			if (subject.equals("语文")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("英语")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("数学")) {
				dir += "/" + subject + typeReal;
			}
		} else if (typeReal.equals("海淀视频")) {
			dir = typeReal;
			if (subject.equals("语文")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("英语")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("数学")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("物理")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("化学")) {
				dir += "/" + subject + typeReal;
			}
		} else if (typeReal.equals("小学视频")) {
			dir = typeReal;
			if (subject.equals("语文")) {
				dir += "/小学语文视频";
			} else if (subject.equals("英语")) {
				dir += "/小学英语视频";
			} else if (subject.equals("数学")) {
				dir += "/小学数学视频";
			}
		} else if (typeReal.equals("实验室")) {
			dir = "理化实验室";
			if (subject.equals("化学")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("物理")) {
				dir += "/" + subject + typeReal;
			} else if (subject.equals("生物")) {
				dir += "/" + subject + typeReal;
			}
			// } else if (typeReal.equals("")) {
			// if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// } else if (subject.equals("")) {
			// }
		} else {
			dir = typeReal;
		}
		return dir;
	}
	
	private String paseDirDataFromJson(String json){
		String data = null;
		try {
			JSONObject jObj = new JSONObject(json);
			String msgCode = jObj.optString("msgCode");
			data = jObj.optString("data");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	private class StartTaskThread extends Thread {
		private int fileid = -1;

		StartTaskThread(Key key) {
			if (key != null && key.info != null) {
				fileid = key.info.fileid;
			}
		}

		public void run() {
			try {
				if (fileid >= 0) {
					Debug.debugLog("before start task fileid=" + fileid);
					downloadManager.startTask(fileid);
					Debug.debugLog("after start task");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private class PauseTaskThread extends Thread {
		private int fileid = -1;

		PauseTaskThread(Key key) {
			if (key != null && key.info != null) {
				fileid = key.info.fileid;
			}
		}

	public void run() {
			try {
				if (fileid >= 0) {
					Debug.debugLog("before pause task fileid=" + fileid);
					downloadManager.pauseTask(fileid);
					Debug.debugLog("after pause task");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private class RemoveTaskThread extends Thread {
		private Key key;
		private int fileid = -1;

		RemoveTaskThread(Key key) {
			this.key = key;
			if (key != null && key.info != null) {
				fileid = key.info.fileid;
			}
		}

	public void run() {
			try {
				if (key != null && key.info != null) {
					Debug.debugLog("before remove task fileid=" + fileid);
					key.info = null;
					downloadManager.removeTask(fileid);
					Debug.debugLog("after remove task");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	

}
