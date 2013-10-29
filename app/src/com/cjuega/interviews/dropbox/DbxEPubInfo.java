package com.cjuega.interviews.dropbox;

import com.dropbox.sync.android.DbxFileInfo;

public class DbxEPubInfo {
	private DbxFileInfo mFileInfo;
	private String mEPubBookTitle;
	private boolean mTitleAvailable;
	
	public DbxEPubInfo (DbxFileInfo fileInfo){
		mFileInfo = fileInfo;
		mEPubBookTitle = null;
		mTitleAvailable = false;
	}
	
	public DbxEPubInfo (DbxFileInfo fileInfo, String epubBookTitle){
		mFileInfo = fileInfo;
		mEPubBookTitle = epubBookTitle;
		mTitleAvailable = false;
	}

	public String getEPubBookTitle() {
		return mEPubBookTitle;
	}

	public void setEPubBookTitle(String mEPubBookTitle) {
		mTitleAvailable = true;
		this.mEPubBookTitle = mEPubBookTitle;
	}

	public DbxFileInfo getFileInfo() {
		return mFileInfo;
	}
	
	public boolean isTitleAvailable(){
		return mTitleAvailable;
	}
}
