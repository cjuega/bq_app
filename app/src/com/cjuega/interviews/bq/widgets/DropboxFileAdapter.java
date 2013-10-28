package com.cjuega.interviews.bq.widgets;

import java.util.Comparator;
import java.util.List;

import nl.siegmann.epublib.domain.Book;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.utils.Utils;
import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.dropbox.DropboxManager;
import com.cjuega.interviews.dropbox.DropboxManager.SimpleCallback;
import com.cjuega.interviews.epub.EPubHelper;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileStatus;

public class DropboxFileAdapter extends EndlessSortedListAdapter<DbxEPubInfo> {
	
	public DropboxFileAdapter(Context context,
			Comparator<? super DbxEPubInfo> comparator, DataRequester requester) {
		super(context, R.layout.listitem_book, 0, comparator, requester);
	}

	public DropboxFileAdapter(Context context,
			DbxEPubInfo[] objects, Comparator<? super DbxEPubInfo> comparator,
			DataRequester requester) {
		super(context, R.layout.listitem_book, 0, objects, comparator, requester);
	}

	public DropboxFileAdapter(Context context,
			List<DbxEPubInfo> objects,
			Comparator<? super DbxEPubInfo> comparator, DataRequester requester) {
		super(context, R.layout.listitem_book, 0, objects, comparator, requester);
	}
	
	
	public long isSync(){
		long bytesToDonwload = 0;
		try{
			for (DbxEPubInfo fileInfo : mData) {
				DbxFile file = DropboxManager.getInstance().open(fileInfo.getFileInfo().path);
				DbxFileStatus fileStatus = file.getSyncStatus();
				if (!fileStatus.isCached){
					bytesToDonwload += fileInfo.getFileInfo().size;
				}
				file.close();
			}
			return bytesToDonwload;
			
		} catch (DbxException e){
			
			return -1;
		}
	}
	
	public void syncAllFiles() {
		for (DbxEPubInfo fileInfo : mData) {
			DbxFile file = DropboxManager.getInstance().open(fileInfo.getFileInfo().path);
			DropboxManager.getInstance().forceReading(file, new SyncFileCallback(fileInfo));
		}
	}
	
	private class SyncFileCallback implements SimpleCallback {
		private DbxEPubInfo epubInfo;
		
		public SyncFileCallback(DbxEPubInfo fileInfo){
			epubInfo = fileInfo;
		}

		@Override
		public void call(Object dbxFile) {
			if (dbxFile != null && dbxFile instanceof DbxFile){
				
				Book book = EPubHelper.openBookFromFile((DbxFile) dbxFile);
				if (book != null && book.getMetadata() != null){
					epubInfo.setEPubBookName(book.getMetadata().getFirstTitle());
					DropboxFileAdapter.this.notifyDataSetChanged();
				} else {
					((DbxFile) dbxFile).close();
				}
			}
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		DropboxFileHolder dropboxFileHolder;
		
		if (shouldLoadMoreData(mData, position)){
			mRequester.requestData();
		}
		
		if (view == null){
			view = mInflater.inflate(mResource, null);
			
			dropboxFileHolder = new DropboxFileHolder();
			
			dropboxFileHolder.logo = (ImageView) view.findViewById(R.id.list_item_book_thumbtail);
			dropboxFileHolder.title = (TextView) view.findViewById(R.id.list_item_book_title);
			dropboxFileHolder.filename = (TextView) view.findViewById(R.id.list_item_filename);
			dropboxFileHolder.size = (TextView) view.findViewById(R.id.list_item_book_filesize);
			
			view.setTag(dropboxFileHolder);
			
		} else {
			dropboxFileHolder = (DropboxFileHolder) view.getTag();
		}
		
		DbxEPubInfo fileInfo = getItem(position);
		
		// The logo is already set in the xml. We could here use a dynamic icon.
		//dropboxFileHolder.logo.setImageResource(R.drawable.ic_epub);
		if (fileInfo.getEPubBookName() != null)
			dropboxFileHolder.title.setText(fileInfo.getEPubBookName());
		else
			dropboxFileHolder.title.setText(R.string.library_book_title_no_sync);
		dropboxFileHolder.filename.setText(fileInfo.getFileInfo().path.getName());
		dropboxFileHolder.size.setText(Utils.humanReadableByteCount(fileInfo.getFileInfo().size, false));
		
		return view;
	}

	static class DropboxFileHolder {
		ImageView logo;
		TextView title;
		TextView filename;
		TextView size;
	}
}
