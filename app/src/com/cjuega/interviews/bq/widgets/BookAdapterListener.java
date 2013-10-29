package com.cjuega.interviews.bq.widgets;

import nl.siegmann.epublib.domain.Book;

import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.epub.EPubHelper.BookListener;

/**
 * 
 * @author cjuega
 *
 * This class implements the {@link BookListener listener} in our app. It just set the book's title in 
 * the given {@link DbxEPubInfo DbxEPubInfo}.
 *
 */
public class BookAdapterListener implements BookListener {
	private DbxEPubInfo mFileInfo;
	private DropboxFileAdapter mAdapter;
	
	public BookAdapterListener(DbxEPubInfo fileInfo, DropboxFileAdapter adapter){
		mFileInfo = fileInfo;
		mAdapter = adapter;
	}

	@Override
	public void OnBookReady(Book book) {
		mFileInfo.setEPubBookTitle(book.getMetadata().getFirstTitle());
		mAdapter.notifyDataSetChanged();
	}
}
