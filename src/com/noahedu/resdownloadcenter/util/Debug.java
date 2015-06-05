package com.noahedu.resdownloadcenter.util;

import android.util.Log;

public class Debug {
	public static final String TAG = "USEARCH";
	private static final boolean DEBUG = true;

	public static final void debugLog() {
		if(DEBUG) {
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			String fileName = Thread.currentThread().getStackTrace()[3].getFileName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.i(TAG, String.format("at %s.%s(%s:%d)", className, methodName, fileName, lineNumber));
		}
	}

	public static final void debugLog(String str) {
		if(DEBUG) {
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			String fileName = Thread.currentThread().getStackTrace()[3].getFileName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.i(TAG, String.format("at %s.%s(%s:%d):%s", className, methodName, fileName, lineNumber, str));
		}
	}
	
	public static final void debugLog2() {
		if(DEBUG) {
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			String fileName = Thread.currentThread().getStackTrace()[3].getFileName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.d(TAG, String.format("at %s.%s(%s:%d)", className, methodName, fileName, lineNumber));
		}
	}
	
	public static final void debugLog2(String str) {
		if(DEBUG) {
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			String fileName = Thread.currentThread().getStackTrace()[3].getFileName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.d(TAG, String.format("at %s.%s(%s:%d):%s", className, methodName, fileName, lineNumber, str));
		}
	}

	public static final void debugTrace() {
		try {
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
