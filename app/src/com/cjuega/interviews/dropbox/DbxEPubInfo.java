package com.cjuega.interviews.dropbox;

import com.dropbox.sync.android.DbxFileInfo;

public class DbxEPubInfo {
	private DbxFileInfo mFileInfo;
	private String mEPubBookName;
	
	public DbxEPubInfo (DbxFileInfo fileInfo){
		mFileInfo = fileInfo;
		mEPubBookName = null;
	}
	
	public DbxEPubInfo (DbxFileInfo fileInfo, String epubBookName){
		mFileInfo = fileInfo;
		mEPubBookName = epubBookName;
	}

	public String getEPubBookName() {
		return mEPubBookName;
	}

	public void setEPubBookName(String mEPubBookName) {
		this.mEPubBookName = mEPubBookName;
	}

	public DbxFileInfo getFileInfo() {
		return mFileInfo;
	}
}
