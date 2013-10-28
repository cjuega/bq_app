package com.cjuega.interviews.dropbox;

import java.util.List;

import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxPath;

public class DropboxListingBean {

	private List<DbxPath> mPaths;
	private List<DbxFileInfo> mFiles;
	
	public DropboxListingBean(List<DbxPath> paths, List<DbxFileInfo> files){
		mPaths = paths;
		mFiles = files;
	}
	
	public List<DbxPath> getPaths() {
		return mPaths;
	}
	public List<DbxFileInfo> getFiles() {
		return mFiles;
	}
}

