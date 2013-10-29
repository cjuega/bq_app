package com.cjuega.interviews.epub;

import java.io.IOException;

import android.os.AsyncTask;

import com.cjuega.interviews.dropbox.DropboxManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class EPubHelper {
	
	private static EPubHelper mInstance;
	
	private EPubHelper(){}
	
	public interface BookListener {
		public void OnBookReady(Book book);
	}
	
	public static EPubHelper getInstance() {
		if (mInstance == null){
			mInstance = new EPubHelper();
		}
		return mInstance;
	}
	
	public void openBookFromFileInfo(DbxFileInfo fileInfo, BookListener listener, boolean checkSync) {
		OpenBookTask openBookTask = new OpenBookTask(listener, checkSync);
		openBookTask.execute(fileInfo);
	}
	
	private class OpenBookTask extends AsyncTask<DbxFileInfo, Void, Book>{
		private BookListener mBookListener;
		private boolean mCheckSync;
		
		public OpenBookTask(BookListener listener, boolean checkSync){
			mBookListener = listener;
			mCheckSync = checkSync;
		}

		@Override
		protected Book doInBackground(DbxFileInfo... params) {
			DbxFileInfo fileInfo = params[0];
			Book book = null;
			// To ensure that only only DbxFile is opened
			synchronized (DropboxManager.getInstance().getLock()) {
				DbxFile file = null;
				try {
					if (!mCheckSync || DropboxManager.getInstance().isSync(fileInfo)){
						file = DropboxManager.getInstance().open(fileInfo.path);
						
						EpubReader reader = new EpubReader();
						book = reader.readEpub(file.getReadStream());
					}
				} catch (DbxException e) {
					
				} catch (IOException e) {
					
				} finally {
					if (file != null){
						file.close();
					}
				}
			}
			return book;
		}

		@Override
		protected void onPostExecute(Book result) {
			if (mBookListener != null && result != null){
				mBookListener.OnBookReady(result);
			}
		}
	}
}
