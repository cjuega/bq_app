package com.cjuega.interviews.bq.widgets;

import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.dropbox.DropboxManager.SimpleCallback;
import com.cjuega.interviews.epub.EPubHelper;
import com.dropbox.sync.android.DbxFileInfo;

/**
 * 
 * @author cjuega
 *
 * This class is an implementation of {@link SimpleCallback SimpleCallback}. It is used by 
 * {@link DropboxFileAdapter DropboxFileAdapter} when the adapter forces the Dropbox Sync API to synchronize all 
 * not synchronized files. The goal is to get the book's titles, so the adapter can sort the list by them.
 *
 */
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
