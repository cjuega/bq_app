package com.cjuega.interviews.bq.main;

import android.app.Application;
import android.content.Context;

public class DropboxLibraryApplication extends Application {
	
	private static Context context;

	public void onCreate() {
		super.onCreate();
		DropboxLibraryApplication.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return DropboxLibraryApplication.context;
	}
}
