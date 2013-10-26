package com.cjuega.interviews.dropbox;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.main.DropboxLibraryApplication;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.InvalidParameter;
import com.dropbox.sync.android.DbxException.NotFound;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxException.Unauthorized;

public class DropboxManager {
	
	private static DropboxManager mInstance;
	
	private DbxAccountManager mDbxAcctMgr;
	private DbxFileSystem mDbxFs;
		
	private DropboxManager(){
		Context context = DropboxLibraryApplication.getAppContext();
		mDbxAcctMgr = DbxAccountManager.getInstance(context, 
												    context.getString(R.string.dropbox_app_key), 
												    context.getString(R.string.dropbox_secret_key));
		
		getDropboxFilesystem();
	}
	
	public static DropboxManager getInstance(){
		if (mInstance == null)
			mInstance = new DropboxManager();
		return mInstance;
	}
	
	public void dropboxLogin(Activity activity, int callbackRequestCode){
		if (!mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.startLink(activity, callbackRequestCode);
	}
	
	public void dropboxLogout(){
		if (mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.unlink();
	}
	
	public boolean isLoggedin(){
		return mDbxAcctMgr.hasLinkedAccount();
	}
	
	// This is a blocking call unless hasSynced() returns true
	public List<DbxFileInfo> getFiles(DbxPath path, String extension){
		if (mDbxFs == null)
			if (!getDropboxFilesystem())
				return null;
		
		try {
			if (!mDbxFs.hasSynced()){
				mDbxFs.awaitFirstSync();
			}
			
			ArrayList<DbxPath> pathsToExplore = new ArrayList<DbxPath>();
			pathsToExplore.add(path);
			
			return iterativeGetFiles(pathsToExplore, extension);
			
		} catch (DbxException e){
			// Whenever hasSynced() or awaitFirstSync() fail
			return null;
		}
	}
	
	private List<DbxFileInfo> iterativeGetFiles(List<DbxPath> paths, String fileExtension){
		ArrayList<DbxFileInfo> filesFound = new ArrayList<DbxFileInfo>();
		
		try {
			// iterates over all the directories
			while (!paths.isEmpty()){
				DbxPath path = paths.remove(0);
				List<DbxFileInfo> files = mDbxFs.listFolder(path);
				
				// for each file in a directory
				for (DbxFileInfo fileInfo : files) {
					// if it is a folder then we include it in the set of directories to explore
					if (fileInfo.isFolder)
						paths.add(fileInfo.path);
					// if it is the kind of file we are looking for we include it in filesFound
					else if (fileExtension == null || fileInfo.path.getName().contains(fileExtension))
						filesFound.add(fileInfo);
				}
			}
		} catch (NotFound e){
			// when the folder supplied to listFolder doesn't exist
			Log.e("DropboxManager", "You must supply an existing folder to listFolder");
			throw new IllegalStateException("The folder supplied to listFolder doesn't exist", e);
			
		} catch (InvalidParameter e) {
			// when the path supplied to listFolder refers to a file.
			Log.e("DropboxManager", "You must supply a folder's path to listFolder");
			throw new IllegalStateException("The path supplied to listFolder refers to a file", e);
			
		} catch (DbxException e) {
			// when another failure occurs in listFolder
			return null;
		}
		
		return filesFound;
	}
	
	public DbxFile open(DbxPath path) {
		if (mDbxFs == null)
			if (!getDropboxFilesystem())
				return null;
		
		try {
			DbxFile file = mDbxFs.open(path);
			return file;
			
		} catch (DbxException e){
			Log.e("DropboxManager", "Error while open file: "+path.getName());
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean getDropboxFilesystem() {
		if (mDbxAcctMgr.hasLinkedAccount()){
			try {
				mDbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
				return true;
				
			} catch (Unauthorized e){
				// This should never happen because we already check mDbxAcctMgr.hasLinkedAccount()
				return false;
			}
		}
		
		return false;
	}
	
	// TODO move the code of getFiles to this AsynTask, so we do not block the UI thread
	private class DropboxListingAsynTask extends AsyncTask<List<DbxPath>, Void, List<DbxFileInfo>> {
		private String fileExtension;

		public DropboxListingAsynTask (String extension){
			fileExtension = extension;
		}
		
		@Override
		protected List<DbxFileInfo> doInBackground(List<DbxPath>... params) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void onPostExecute(List<DbxFileInfo> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
	}
}
