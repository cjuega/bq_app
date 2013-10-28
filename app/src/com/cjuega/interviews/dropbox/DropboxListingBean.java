package com.cjuega.interviews.dropbox;

import java.util.List;

import com.dropbox.sync.android.DbxPath;

public class DropboxListingBean {

	private List<DbxPath> mPaths;
	private List<DbxEPubInfo> mFiles;
	
	public DropboxListingBean(List<DbxPath> paths, List<DbxEPubInfo> files){
		mPaths = paths;
		mFiles = files;
	}
	
	public List<DbxPath> getPaths() {
		return mPaths;
	}
	public List<DbxEPubInfo> getFiles() {
		return mFiles;
	}
}

