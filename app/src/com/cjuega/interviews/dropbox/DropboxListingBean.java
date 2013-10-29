package com.cjuega.interviews.dropbox;

import java.util.List;

import com.dropbox.sync.android.DbxPath;

/**
 * 
 * @author cjuega
 *
 * Simple helper class used by {@link DropboxManager.DropboxListingTask DropboxListingTask} to pass both 
 * the list of files found and the list of folders that are not explore yet.
 *
 */
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

