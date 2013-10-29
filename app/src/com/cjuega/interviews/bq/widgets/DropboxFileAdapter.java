package com.cjuega.interviews.bq.widgets;

import java.util.Comparator;
import java.util.List;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.utils.Utils;
import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.dropbox.DropboxManager;
import com.cjuega.interviews.epub.EPubHelper;

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
		synchronized (DropboxManager.getInstance().getLock()) {
			for (DbxEPubInfo fileInfo : mData) {
				if (!DropboxManager.getInstance().isSync(fileInfo.getFileInfo()))
					bytesToDonwload += fileInfo.getFileInfo().size;
			}
		}
		return bytesToDonwload;
	}
	
	public boolean areTitlesAvailable(){
		for (DbxEPubInfo fileInfo : mData) {
			if (!fileInfo.isTitleAvailable())
				return false;
		}
		return true;
	}
	
	public void demandTitles(){
		for (DbxEPubInfo fileInfo : mData) {
			if (!fileInfo.isTitleAvailable()){
				EPubHelper.getInstance().openBookFromFileInfo(fileInfo.getFileInfo(),
															  new BookAdapterListener(fileInfo, this),
															  true);
			}
		}
	}
	
	public void syncAllFiles() {
		for (DbxEPubInfo fileInfo : mData) {
			DropboxManager.getInstance().forceReading(fileInfo.getFileInfo(), new SyncFileAdapterListener(fileInfo, this));
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
		dropboxFileHolder.title.setText(fileInfo.getEPubBookTitle());
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
