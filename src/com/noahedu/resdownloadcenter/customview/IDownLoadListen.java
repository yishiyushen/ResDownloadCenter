package com.noahedu.resdownloadcenter.customview;

import com.noahedu.resdownloadcenter.search.Key;
import com.noahedu.ucache.LoadTaskInfo;

public interface IDownLoadListen {
	public void download(Key key);
	public void pauseDown(Key key);
	public void cancelDown(Key key);
	public void update(Key key);
	public void openfile(String fn);
	public LoadTaskInfo getTaskInfoUrl(String url);
}
