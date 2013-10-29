package com.cjuega.interviews.bq.widgets;

import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.dropbox.DropboxManager.SimpleCallback;
import com.cjuega.interviews.epub.EPubHelper;
import com.dropbox.sync.android.DbxFileInfo;

public class SyncFileAdapterListener implements SimpleCallback {
	private DbxEPubInfo mFileInfo;
	private DropboxFileAdapter mAdapter;
	
	public SyncFileAdapterListener(DbxEPubInfo fileInfo, DropboxFileAdapter adapter){
		mFileInfo = fileInfo;
		mAdapter = adapter;
	}

	@Override
	public void call(Object dbxFileInfo) {
		if (dbxFileInfo != null && dbxFileInfo instanceof DbxFileInfo){
			EPubHelper.getInstance().openBookFromFileInfo((DbxFileInfo)dbxFileInfo,
													  	  new BookAdapterListener(mFileInfo, mAdapter),
													  	  false);
		}
	}
}
