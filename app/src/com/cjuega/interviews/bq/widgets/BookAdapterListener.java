package com.cjuega.interviews.bq.widgets;

import nl.siegmann.epublib.domain.Book;

import android.util.Log;

import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.epub.EPubHelper.BookListener;

public class BookAdapterListener implements BookListener {
	private DbxEPubInfo mFileInfo;
	private DropboxFileAdapter mAdapter;
	
	public BookAdapterListener(DbxEPubInfo fileInfo, DropboxFileAdapter adapter){
		mFileInfo = fileInfo;
		mAdapter = adapter;
	}

	@Override
	public void OnBookReady(Book book) {
		Log.d("BookAdapterListener", "OnBookReady");
		mFileInfo.setEPubBookTitle(book.getMetadata().getFirstTitle());
		mAdapter.notifyDataSetChanged();
	}
}
