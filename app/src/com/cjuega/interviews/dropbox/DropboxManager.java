package com.cjuega.interviews.dropbox;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
import com.dropbox.sync.android.DbxFile.Listener;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileStatus;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxException.Unauthorized;

/**
 * 
 * @author cjuega
 *
 * This class is in charge of deal with the Dropbox Sync API. It uses several classes that inherit 
 * of {@link AsyncTask AsyncTask} to offload work (like list and read files) to secondary threads. Whenever 
 * the work is done, a {@link SimpleCallback callback} processes the downloaded data. 
 *  
 */
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
	
	/**
	 * 
	 * This method handles the Dropbox login process.
	 * 
	 * @param activity				The activity that launches the Dropbox Activity and hence acts as its parent.
	 * @param callbackRequestCode	The response that will receive the {@code activity} on its {@code onActivityResult} 
	 */
	public void dropboxLogin(Activity activity, int callbackRequestCode){
		if (!mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.startLink(activity, callbackRequestCode);
	}
	
	/**
	 * 
	 * This method handles the Dropbox logout process.
	 * 
	 */
	public void dropboxLogout(){
		if (mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.unlink();
	}
	
	/**
	 * 
	 * This method checks if there is already a Dropbox session started
	 * 
	 * @return true if there is already a Dropbox session, false otherwise.
	 */
	public boolean isLoggedin(){
		return mDbxAcctMgr.hasLinkedAccount();
	}
	
	/**
	 * 
	 * This method list all files (up to 1000 files which a Dropbox Sync API limit) of a given {@code extension} 
	 * that are in the {@code path} folder and all its subdirectories. It is an asynchronous call 
	 * (implemented by {@link DropboxManager.DropboxListingTask DropboxListingTask}).
	 * 
	 * @param path			List of paths to explore when looking for files.
	 * @param extension		File extension this method will look for. If it is null then all files are selected.
	 * @param callback		The callback when the files are already listed. 
	 */
	public void getAllFiles(DbxPath path, String extension, SimpleCallback callback){
		if (mDbxFs == null)
			if (!getDropboxFilesystem())
				return;
		
		DropboxListingTask task = new DropboxListingTask(extension, callback);
		task.execute(path);
	}
	
	
	/**
	 * 
	 * This method returns a Dropbox file descriptor.
	 * 
	 * @param path	The file path that will open this method.
	 * @return	The file descriptor.
	 */
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
	
	/**
	 * 
	 * This method adds a listener to a Dropbox file, so a {@code callback} can be called when the 
	 * file is cached by the Dropbox Sync API. In contrast to 
	 * {@link DropboxManager#forceReading(DbxFile, SimpleCallback) forceReading}, this method do not force 
	 * the API to start the synchronization immediately. Instead it lets the API synchronize the file whenever 
	 * it want. 
	 * 
	 * @param file		The Dropbox file descriptor.
	 * @param callback	The callback when the file is cached by the Dropbox Sync API.
	 */
	public void prepareToRead (DbxFile file, SimpleCallback callback) {
		try {
			DbxFileStatus fileStatus = file.getSyncStatus();
			
			if (!fileStatus.isCached){
				file.addListener(new FileListener(callback));
			}
			
		} catch (DbxException e){
			
		}
	}
	
	/**
	 * 
	 * This method forces the Dropbox Sync API to download the given file if it is not already cached. The 
	 * download is done in a separate thread (implemented by {@link DropboxManager.DropboxReadFileTask DropboxReadFileTask}.
	 * 
	 * @param file		The Dropbox file descriptor.
	 * @param callback	The callback when the file is cached by the Dropbox Sync API.
	 */
	public void forceReading (DbxFile file, SimpleCallback callback) {
		try {
			DbxFileStatus fileStatus = file.getSyncStatus();
			
			if (fileStatus.isCached){
				callback.call(file);
			} else {
				DropboxReadFileTask task = new DropboxReadFileTask(callback);
				task.execute(file);
			}
		} catch (DbxException e){
			
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
	
	/**
	 * 
	 * @author cjuega
	 *
	 * Simple interface to perform callbacks.
	 * 
	 */
	public interface SimpleCallback {
		public void call(Object object);
	}
	
	/**
	 * 
	 * @author cjuega
	 * 
	 * Class that implements the Dropbox file listener. It notifies (through a 
	 * {@link DropboxManager.SimpleCallback SimpleCallback} when a file is cached.
	 *
	 */
	private class FileListener implements Listener {
		WeakReference<SimpleCallback> mCallbackRef;
		
		public FileListener (SimpleCallback callback){
			mCallbackRef = new WeakReference<SimpleCallback>(callback);
		}
		
		@Override
		public void onFileChange(DbxFile file) {
			boolean listenerCanBeRemoved = false;
			try {
				DbxFileStatus fileStatus = file.getSyncStatus();
				if (fileStatus.isCached){
					listenerCanBeRemoved = true;
					if (mCallbackRef != null && mCallbackRef.get() != null)
						mCallbackRef.get().call(file);
				}
				
			} catch (DbxException e){
				
			} finally {
				if (listenerCanBeRemoved)
					file.removeListener(this);
			}
		}
	}
	
	/**
	 * 
	 * @author cjuega
	 * 
	 * Class that offloads the heavy task of list all files of a given extension to a separate thread.
	 *
	 */
	private class DropboxListingTask extends AsyncTask<DbxPath, Void, List<DbxFileInfo>> {
		private String fileExtension;
		private WeakReference<SimpleCallback> mCallbackRef;

		public DropboxListingTask (String extension, SimpleCallback callback){
			fileExtension = extension;
			mCallbackRef = new WeakReference<DropboxManager.SimpleCallback>(callback);
		}
		
		@Override
		protected List<DbxFileInfo> doInBackground(DbxPath... params) {
			try {
				List<DbxPath> paths = new ArrayList<DbxPath>();
				for (int i=0; i<params.length; i++){
					paths.add(params[i]);
				}
				
				if (!mDbxFs.hasSynced()){
					mDbxFs.awaitFirstSync();
				}

				return iterativeGetFiles(paths, fileExtension);
				
			} catch (DbxException e){
				// Whenever hasSynced() or awaitFirstSync() fail
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<DbxFileInfo> result) {
			if (result != null && mCallbackRef != null && mCallbackRef.get() != null){
				mCallbackRef.get().call(result);
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
				Log.e("DropboxManager.DropboxListingTask", "You must supply an existing folder to listFolder");
				throw new IllegalStateException("The folder supplied to listFolder doesn't exist", e);
				
			} catch (InvalidParameter e) {
				// when the path supplied to listFolder refers to a file.
				Log.e("DropboxManager.DropboxListingTask", "You must supply a folder's path to listFolder");
				throw new IllegalStateException("The path supplied to listFolder refers to a file", e);
				
			} catch (DbxException e) {
				// when another failure occurs in listFolder
				return null;
			}
			
			return filesFound;
		}
	}

	/**
	 * 
	 * @author cjuega
	 *
	 * Class that offloads the heavy task of downloading a file to a separate thread.
	 * 
	 */
	private class DropboxReadFileTask extends AsyncTask<DbxFile, Void, DbxFile> {
		private WeakReference<SimpleCallback> mCallbackRef;
		
		public DropboxReadFileTask(SimpleCallback callback){
			mCallbackRef = new WeakReference<DropboxManager.SimpleCallback>(callback);
		}

		@Override
		protected DbxFile doInBackground(DbxFile... params) {
			try {
				params[0].getReadStream();
				return params[0];
				
			} catch (DbxException e){
				return null;
				
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(DbxFile result) {
			if (result != null && mCallbackRef != null && mCallbackRef.get() != null){
				try {
					DbxFileStatus fileStatus = result.getSyncStatus();
					if (fileStatus.isCached){
						mCallbackRef.get().call(result);
					}
				} catch (DbxException e){
				
				}
			}
		}
	}
}
