package com.cjuega.interviews.bq.fragments;

import java.io.IOException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.utils.BitmapHelper;
import com.cjuega.interviews.dropbox.DropboxManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxPath;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BookDetailsFragment extends Fragment implements DropboxManager.SimpleCallback {

	public static final String FILENAME = "FILENAME";
	
	private String mFilename;
	
	private ImageView mBookCoverTextView;
	private TextView mBookTitleTextView;
	private TextView mBookAuthorsTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null){
			mFilename = getArguments().getString(FILENAME);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_book_details, container, false);
		
		mBookCoverTextView = (ImageView) view.findViewById(R.id.book_cover);
		mBookTitleTextView = (TextView) view.findViewById(R.id.book_title);
		mBookAuthorsTextView = (TextView) view.findViewById(R.id.book_author);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (mFilename != null){
			DbxFile file = DropboxManager.getInstance().open(new DbxPath(mFilename));
			DropboxManager.getInstance().forceReading(file, this);
			
		} else {
			if (savedInstanceState == null){
				whenBookDataIsNotAvailable();
			}
		}
	}

	private void whenBookDataIsNotAvailable() {
		BitmapHelper helper = new BitmapHelper();
		helper.loadBitmapFromResources(mBookCoverTextView, R.drawable.epub_logo);
		mBookTitleTextView.setText(String.format(getString(R.string.library_book_title), "UNKNOWN"));
		mBookAuthorsTextView.setText(String.format(getString(R.string.library_book_author), "UNKNOWN"));
	}

	@Override
	public void call(Object dbxFile) {
		if (dbxFile != null && dbxFile instanceof DbxFile){

			DbxFile file = (DbxFile) dbxFile;
			
			try {
				// file.getReadStream() will never block the UI thread because we already guarantee that the files is cached
				Book book = (new EpubReader()).readEpub(file.getReadStream());
				file.close();
				
				BitmapHelper helper = new BitmapHelper();
				if (book.getCoverImage() != null)
					helper.loadBitmapFromInputStream(mBookCoverTextView, book.getCoverImage().getInputStream());
				else
					helper.loadBitmapFromResources(mBookCoverTextView, R.drawable.epub_logo);
					
				if (book.getMetadata() != null){
					mBookTitleTextView.setText(String.format(getString(R.string.library_book_title), book.getMetadata().getFirstTitle()));
					mBookAuthorsTextView.setText(String.format(getString(R.string.library_book_author), book.getMetadata().getAuthors()));
				}
					
			} catch (DbxException e){
				// when file.getReadStream() fails
				whenBookDataIsNotAvailable();
					
			} catch (IOException e) {
				whenBookDataIsNotAvailable();
			}
		} else {
			Toast.makeText(getActivity(), getString(R.string.dropbox_open_file_error), Toast.LENGTH_SHORT).show();
			whenBookDataIsNotAvailable();
		}
	}
}
