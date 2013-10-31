package com.cjuega.interviews.dropbox;

import java.io.IOException;
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
import com.dropbox.sync.android.DbxFileStatus;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxFileSystem.PathListener;
import com.dropbox.sync.android.DbxFileSystem.PathListener.Mode;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxException.Unauthorized;

/**
 * 
 * @author cjuega
 *
 * This class deals with the Dropbox Sync API. It uses several classes that inherit 
 * of {@link AsyncTask AsyncTask} to offload work (like list and read files) to secondary threads. Whenever 
 * the work is done, a {@link SimpleDropboxCallback callback} processes the downloaded data. 
 *  
 */
public class DropboxManager {
	
	private static DropboxManager mInstance;
	
	private DbxAccountManager mDbxAcctMgr;
	private DbxFileSystem mDbxFs;
	
	private List<PathListener> mPathListeners;
	
	private Object mLock = new Object();
		
	private DropboxManager(){
		Context context = DropboxLibraryApplication.getAppContext();
		mDbxAcctMgr = DbxAccountManager.getInstance(context, 
												    context.getString(R.string.dropbox_app_key), 
												    context.getString(R.string.dropbox_secret_key));
		
		mPathListeners = new ArrayList<PathListener>();
		
		getDropboxFilesystem();
	}
	
	public static DropboxManager getInstance(){
		if (mInstance == null)
			mInstance = new DropboxManager();
		return mInstance;
	}
	
	public Object getLock(){
		return mLock;
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
		dropboxShutdown();
		if (mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.unlink();
	}
	
	/**
	 * 
	 * This method prepares the Dropbox sync API for shutdown. It removes all {@link PathListener PathListeners} (if any).
	 * 
	 */
	public void dropboxShutdown(){
		if (mDbxFs != null){
			if (mPathListeners != null && mPathListeners.size() > 0){
				for (PathListener listener : mPathListeners) {
					mDbxFs.removePathListenerForAll(listener);	
				}
				mPathListeners.clear();
			}
			
			mDbxFs.shutDown();
		}
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
	 * This method adds a {@link PathListener listener} to a given path.
	 * 
	 * @param listener	The listener that models the action to perform when a event happens.
	 * @param path		The {@link DbxPath path} to be listened.
	 * @param mode		The {@link Mode mode} in which the listener will operate.
	 */
	public void addListenerToPath(PathListener listener, DbxPath path, Mode mode){
		if (!getDropboxFilesystem())
			return;
		
		mDbxFs.addPathListener(listener, path, mode);
	}
	
	/**
	 * 
	 * This method list all files (up to 1000 files which a Dropbox Sync API limit) of a given {@code extension} 
	 * that are in the {@code DbxPath.ROOT} folder and all its subdirectories. See also {@link DropboxManager#getFiles(List, String, int, SimpleDropboxCallback) getFiles}
	 * 
	 * @param extension		File extension this method will look for. If it is null then all files are selected.
	 * @param callback		The callback when the files are already listed.
	 * @param pathListener	The listener to be added to the folder that the method explores.
	 * @param listen		Boolean that indicates if the method must add the listener or not.
	 */
	public void getAllFiles(String extension, SimpleDropboxCallback callback, PathListener pathListener, boolean listen){
		ArrayList<DbxPath> pathToRoot = new ArrayList<DbxPath>(1);
		pathToRoot.add(DbxPath.ROOT);
		
		getFiles(pathToRoot, extension, -1, callback, pathListener, listen);
	}
	
	/**
	 * 
	 * This method list all files of a given {@code extension} that are in the folders listed at {@code paths} 
	 * and "some" of their subdirectories. The method explores the subdirectories tree until {@code maxFiles} are 
	 * found. Eventually more files than {@code maxFiles} might be returned. That happens when the method 
	 * reaches the files limit but there are files left in the current directory. After those files are added 
	 * the method finishes. Use {@code maxFiles} carefully to control the downloaded data rate.
	 *   
	 * It is an asynchronous call (implemented by {@link DropboxManager.DropboxListingTask DropboxListingTask}).
	 * 
	 * @param paths			List of paths to explore when looking for files.
	 * @param extension		File extension this method will look for. If it is null then all files are selected.
	 * @param maxFiles		Relative max number of files to list. Negative values mean there is no limit.
	 * @param callback		The callback when the files are already listed.
	 * @param pathListener	The listener to be added to the folder that the method explores.
	 * @param listen		Boolean that indicates if the method must add the listener or not.
	 */
	public void getFiles(List<DbxPath> paths, String extension, int maxFiles, SimpleDropboxCallback callback, PathListener pathListener, boolean listen){
		if (!getDropboxFilesystem())
			return;
		
		DropboxListingTask task = new DropboxListingTask(paths, extension, maxFiles, callback, pathListener, listen);
		task.execute();
	}
	
	/**
	 * 
	 * This method returns a Dropbox file descriptor.
	 * 
	 * @param path	The file path that will open this method.
	 * @return	The file descriptor.
	 */
	public DbxFile open(DbxPath path) {
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
	 * This method checks if the given file is already cached.
	 * 
	 * @param file	The Dropbox file descriptor.
	 * @return		True if the file is cached, false otherwise.
	 */
	public boolean isSync(DbxFileInfo fileInfo){
		DbxFile file = null;
		try {
			file = open(fileInfo.path);
			DbxFileStatus fileStatus = file.getSyncStatus();
			return fileStatus.isCached;
			
		} catch (DbxException e){
			return false;
		} finally {
			if (file != null){
				file.close();
			}
		}
	}
	
	/**
	 * 
	 * This method forces the Dropbox Sync API to download the file at the given path if it is not already cached. The 
	 * download is done in a separate thread (implemented by {@link DropboxManager.DropboxReadFileTask DropboxReadFileTask}.
	 * 
	 * @param path		The Dropbox file path.
	 * @param callback	The callback when the file is cached by the Dropbox Sync API.
	 */
	public void forceReadingFromPath (DbxPath path, SimpleDropboxCallback callback) {
		try {
			DbxFileInfo fileInfo = mDbxFs.getFileInfo(path);
			forceReading(fileInfo, callback);
			
		} catch (NotFound e){
			Log.e("DropboxManager", "forceReadingFromPath -> NotFound exception: "+e.getMessage());
			
		} catch (DbxException e) {
			Log.e("DropboxManager", "forceReadingFromPath -> DbxException: "+e.getMessage());
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
	public void forceReading (DbxFileInfo fileInfo, SimpleDropboxCallback callback) {
		if (isSync(fileInfo)){
			callback.call(fileInfo);
		} else {
			DropboxReadFileTask task = new DropboxReadFileTask(callback);
			task.execute(fileInfo);
		}
	}
	
	/**
	 * 
	 * @return True if a {@link DbxFileSystem DbxFileSystem} is created or already exists, false otherwise.
	 */
	public boolean getDropboxFilesystem() {
		if (mDbxFs != null)
			return true;
		
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
	public interface SimpleDropboxCallback {
		public void call(Object object);
	}
	
	/**
	 * 
	 * @author cjuega
	 * 
	 * Class that offloads the heavy task of list all files of a given extension to a separate thread.
	 *
	 */
	private class DropboxListingTask extends AsyncTask<Void, Void, List<DbxEPubInfo>> {
		private List<DbxPath> pathsToExplore;
		private String fileExtension;
		private int maxFiles;
		private SimpleDropboxCallback mCallback;
		private PathListener mPathListener;
		private boolean mListen;

		public DropboxListingTask (List<DbxPath> paths, String extension, int maxfiles, SimpleDropboxCallback callback, PathListener listener, boolean listen){
			pathsToExplore = paths;
			fileExtension = extension;
			maxFiles = maxfiles;
			mCallback = callback;
			mPathListener = listener;
			mListen = listen;
		}
		
		@Override
		protected List<DbxEPubInfo> doInBackground(Void... params) {
			try {				
				if (!mDbxFs.hasSynced()){
					mDbxFs.awaitFirstSync();
				}

				return iterativeGetFiles(pathsToExplore, fileExtension, maxFiles);
				
			} catch (DbxException e){
				// Whenever hasSynced() or awaitFirstSync() fail
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<DbxEPubInfo> result) {
			if (result != null && mCallback != null){
				mCallback.call(new DropboxListingBean(pathsToExplore, result));
			}
		}
		
		private List<DbxEPubInfo> iterativeGetFiles(List<DbxPath> paths, String fileExtension, int maxfiles){
			ArrayList<DbxEPubInfo> filesFound = new ArrayList<DbxEPubInfo>();
			int nfiles = 0;
			
			try {
				// iterates over all the directories
				while (!paths.isEmpty() && (maxfiles < 0 || nfiles < maxfiles)){
					DbxPath path = paths.remove(0);
					List<DbxFileInfo> files = mDbxFs.listFolder(path);
					
					// for each file in a directory
					for (DbxFileInfo fileInfo : files) {
						// if it is a folder then we include it in the set of directories to explore
						if (fileInfo.isFolder){
							paths.add(fileInfo.path);
							// In addition it would be useful to add a listener to this folder as well. So 
							// if somebody adds new files (ebooks) to the account we will immediately know it.
							if (mListen && mPathListener != null)
								DropboxManager.getInstance().addListenerToPath(mPathListener, fileInfo.path, Mode.PATH_OR_CHILD);
						}
						// if it is the kind of file we are looking for we include it in filesFound
						else if (fileExtension == null || fileInfo.path.getName().contains(fileExtension)){
							DbxEPubInfo newItem = new DbxEPubInfo(fileInfo, fileInfo.path.getName());
								
							filesFound.add(newItem);
							nfiles++;
						}
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
	private class DropboxReadFileTask extends AsyncTask<DbxFileInfo, Void, DbxFileInfo> {
		private SimpleDropboxCallback mCallback;
		
		public DropboxReadFileTask(SimpleDropboxCallback callback){
			mCallback = callback;
		}

		@Override
		protected DbxFileInfo doInBackground(DbxFileInfo... params) {
			DbxFileInfo fileInfo = params[0];
			DbxFile file = null;
			
			// To ensure that only only DbxFile is opened
			synchronized (DropboxManager.getInstance().getLock()) {
				try {
					file = DropboxManager.getInstance().open(fileInfo.path);
					file.getReadStream();
					return params[0];
					
				} catch (DbxException e){
					return null;
					
				} catch (IOException e) {
					return null;
					
				} finally {
					if (file != null){
						file.close();
					}
				}
			}
		}

		@Override
		protected void onPostExecute(DbxFileInfo result) {
			if (result != null && mCallback != null){
				if (DropboxManager.getInstance().isSync(result)){
					mCallback.call(result);
				}
			}
		}
	}
}
