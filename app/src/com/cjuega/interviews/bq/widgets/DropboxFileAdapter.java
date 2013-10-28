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
import com.dropbox.sync.android.DbxFileInfo;

public class DropboxFileAdapter extends EndlessSortedListAdapter<DbxFileInfo>{
	
	public DropboxFileAdapter(Context context,
			Comparator<? super DbxFileInfo> comparator, DataRequester requester) {
		super(context, R.layout.listitem_book, 0, comparator, requester);
	}

	public DropboxFileAdapter(Context context,
			DbxFileInfo[] objects, Comparator<? super DbxFileInfo> comparator,
			DataRequester requester) {
		super(context, R.layout.listitem_book, 0, objects, comparator, requester);
	}

	public DropboxFileAdapter(Context context,
			List<DbxFileInfo> objects,
			Comparator<? super DbxFileInfo> comparator, DataRequester requester) {
		super(context, R.layout.listitem_book, 0, objects, comparator, requester);
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
		
		DbxFileInfo fileInfo = getItem(position);
		
		dropboxFileHolder.logo.setImageResource(R.drawable.ic_epub);
		dropboxFileHolder.title.setText(R.string.library_book_title_no_sync);
		dropboxFileHolder.filename.setText(fileInfo.path.getName());
		dropboxFileHolder.size.setText(Utils.humanReadableByteCount(fileInfo.size, false));
		
		return view;
	}

	static class DropboxFileHolder {
		ImageView logo;
		TextView title;
		TextView filename;
		TextView size;
	}
}
