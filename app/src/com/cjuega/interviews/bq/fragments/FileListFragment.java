package com.cjuega.interviews.bq.fragments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.utils.Utils;
import com.cjuega.interviews.bq.widgets.BookAdapterListener;
import com.cjuega.interviews.bq.widgets.DataRequester;
import com.cjuega.interviews.bq.widgets.DoubleClickSupportedListView;
import com.cjuega.interviews.bq.widgets.DoubleClickSupportedListView.OnItemDoubleClickListener;
import com.cjuega.interviews.bq.widgets.DropboxFileAdapter;
import com.cjuega.interviews.dropbox.DbxEPubInfo;
import com.cjuega.interviews.dropbox.DropboxListingBean;
import com.cjuega.interviews.dropbox.DropboxManager;
import com.cjuega.interviews.epub.EPubHelper;
import com.dropbox.sync.android.DbxPath;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FileListFragment extends ListFragmentCustomLayout implements ActionBar.OnNavigationListener,
															  		 	  DropboxManager.SimpleCallback,
															  		 	  DataRequester {
	
	private static final String FILE_EXTENSION = ".epub";
	
	private static final String SORT_BY_KEY = "SORT_BY_KEY";
	private static final int SORT_BY_BOOKTITLE = 1;
	private static final int SORT_BY_FILENAME = 2;
	private static final int SORT_BY_CREATION_DATE = 3;
	private int mSortMethod;
	
	private static final String PATHS_KEY = "PATHS_KEY";
	private static final String SEPARATOR = "|";
	private List<DbxPath> mPathsToExplore;
	
	private static final int MAX_FILES_PER_REQUEST = 20;
	
	private OnFileSelectedListener mCallback;
	private ActionBar mActionBar;
	
	private int mPreviousNavigationMode;
	
	private DropboxFileAdapter mAdapter;
	
	private static final String SHOW_DIALOG_KEY = "SHOW_DIALOG_KEY";
	private long mShowingDialogBytes = 0;
	private AlertDialog mSyncDialog;
	
	// Container Activity must implement this interface
    public interface OnFileSelectedListener {
        public void onFileSelected(DbxPath path);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFileSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFileSelectedListener");
        }
    }
    
    
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
				
		if (savedInstanceState == null){
			mSortMethod = SORT_BY_FILENAME;
			Log.d("FileListFragment", "onCreate -> set sort method to "+mSortMethod);
			mPathsToExplore = new ArrayList<DbxPath>();
			mPathsToExplore.add(DbxPath.ROOT);
			
		}else{
			mSortMethod = savedInstanceState.getInt(SORT_BY_KEY);
			if (mSortMethod == 0)
				mSortMethod = SORT_BY_FILENAME;	
			Log.d("FileListFragment", "onCreate -> set sort method to "+mSortMethod);
			mPathsToExplore = restorePathsListFromString(savedInstanceState.getString(PATHS_KEY));
			mShowingDialogBytes = savedInstanceState.getLong(SHOW_DIALOG_KEY);
			showSyncDialog(mShowingDialogBytes);
		}
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		DoubleClickSupportedListView listView = (DoubleClickSupportedListView) view.findViewById(android.R.id.list);
		listView.setOnItemDoubleClickListener(new OnItemDoubleClickListener() {
			
			@Override
			public void OnItemDoubleClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCallback.onFileSelected(mAdapter.getItem(position).getFileInfo().path);
			}
		});
		
		TextView emptyText = (TextView) view.findViewById(android.R.id.empty);
		emptyText.setText(R.string.library_empty_list);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
											
		if (getActivity() instanceof ActionBarActivity){
			mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
			if (mActionBar != null)
				mPreviousNavigationMode = mActionBar.getNavigationMode();
			
			SpinnerAdapter dropDownAdapter = ArrayAdapter.createFromResource(getActivity(), 
					   														 R.array.action_sortby_list,
					   														 android.R.layout.simple_list_item_1);
			// To enable the drop-down menu within the Activity's ActionBar
			mActionBar.setListNavigationCallbacks(dropDownAdapter, this);
		}else
			mActionBar = null;
		
		if (mAdapter == null){
			setListShown(false);
			Log.d("FileListFragment", "restoring Comparator");
			mAdapter = new DropboxFileAdapter(getActivity(), restoreComparator(mSortMethod), this);
			setListAdapter(mAdapter);
		}
	}

	@Override
	public void onResume() {
		if (mActionBar != null)
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SORT_BY_KEY, mSortMethod);
		outState.putString(PATHS_KEY, convertPathsListToString(mPathsToExplore));
		outState.putLong(SHOW_DIALOG_KEY, mShowingDialogBytes);
	}

	@Override
	public void onPause() {
		if (mActionBar != null){
			mActionBar.setNavigationMode(mPreviousNavigationMode);
			//mActionBar.setListNavigationCallbacks(null, null);
		}
		
		if (mSyncDialog != null)
			mSyncDialog.dismiss();
		
		super.onPause();
	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		if (mAdapter != null){
			switch (position) {
			//Sort by filename
			case 0:
				Log.d("FileListFragment", "onNavigationItemSelected -> sorting by Filename");
				mAdapter.sortby (new FilenameComparator());
				mSortMethod = SORT_BY_FILENAME;
				Log.d("FileListFragment", "onNavigationItemSelected -> set sort method to "+mSortMethod);
				return true;
				
			//Sort by creation date
			case 1:
				Log.d("FileListFragment", "onNavigationItemSelected -> sorting by Date");
				mAdapter.sortby (new CreationDateComparator());
				mSortMethod = SORT_BY_CREATION_DATE;
				Log.d("FileListFragment", "onNavigationItemSelected -> set sort method to "+mSortMethod);
				return true;
				
			//Sort by book's title
			case 2:
				long bytesToDownload = mAdapter.isSync();
				if (bytesToDownload == 0){
					if (mAdapter.areTitlesAvailable()){
						Log.d("FileListFragment", "onNavigationItemSelected -> sorting by Title");
						mAdapter.sortby (new BookTitleComparator());
						mSortMethod = SORT_BY_BOOKTITLE;
						Log.d("FileListFragment", "onNavigationItemSelected -> set sort method to "+mSortMethod);
						
					} else {
						mAdapter.demandTitles();
					}
					
				} else {
					showSyncDialog(bytesToDownload);
				}
				return true;
				
			default:
				return false;
			}
		}
		return false;
	}
	
	private void showSyncDialog(long bytes){
		if (bytes <= 0)
			return;
		
		mShowingDialogBytes = bytes;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		mSyncDialog = builder.setTitle(R.string.dialog_title)
							 .setMessage(String.format(getString(R.string.dialog_message), Utils.humanReadableByteCount(bytes, false)))
							 .setPositiveButton(R.string.dialog_yes, new OnClickListener() {
								 @Override
								 public void onClick(DialogInterface dialog, int which) {
									 mShowingDialogBytes = 0;
									 mAdapter.syncAllFiles();
									 mSyncDialog.dismiss();
								 }
							 }).setNegativeButton(R.string.dialog_no, new OnClickListener() {
								
								 @Override
								 public void onClick(DialogInterface dialog, int which) {
									 mShowingDialogBytes = 0;
									 mSyncDialog.dismiss();
								 }
							 }).create();
		
		mSyncDialog.show();
	}
	
	private String convertPathsListToString (List<DbxPath> paths){
		if (paths == null || paths.size() == 0)
			return null;
		
		StringBuilder pathListString = new StringBuilder();
        
        for(DbxPath i : paths){
        	pathListString.append(i);
        	pathListString.append(SEPARATOR);
        }
        
        return pathListString.toString();
	}
	
	private List<DbxPath> restorePathsListFromString (String str){
		List<DbxPath> paths = new ArrayList<DbxPath>();
		
		if (str != null){
			StringTokenizer tk = new StringTokenizer(str, SEPARATOR);
   	 		
   	 		while (tk.hasMoreElements()){
   	 		paths.add(new DbxPath(tk.nextToken()));
   	 		}
		} else {
			paths.add(DbxPath.ROOT);
		}
		
		return paths;
	}
	
	private Comparator<DbxEPubInfo> restoreComparator(int comparatorId) {
		switch (comparatorId) {
		case SORT_BY_BOOKTITLE:
			return new BookTitleComparator();
			
		case SORT_BY_FILENAME:
			return new FilenameComparator();
			
		case SORT_BY_CREATION_DATE:
			return new CreationDateComparator();

		default:
			return null;
		}
	}
	
	private class BookTitleComparator implements Comparator<DbxEPubInfo>{

		@Override
		public int compare(DbxEPubInfo lhs, DbxEPubInfo rhs) {
			return lhs.getEPubBookTitle().compareTo(rhs.getEPubBookTitle());
		}
	}
	
	private class FilenameComparator implements Comparator<DbxEPubInfo>{

		@Override
		public int compare(DbxEPubInfo lhs, DbxEPubInfo rhs) {
			return lhs.getFileInfo().path.getName().compareTo(rhs.getFileInfo().path.getName());
		}
	}
	
	private class CreationDateComparator implements Comparator<DbxEPubInfo>{

		@Override
		public int compare(DbxEPubInfo lhs, DbxEPubInfo rhs) {
			return lhs.getFileInfo().modifiedTime.compareTo(rhs.getFileInfo().modifiedTime);
		}
	}

	@Override
	public void call(Object listOfPathsAndFiles) {
		if (listOfPathsAndFiles != null && listOfPathsAndFiles instanceof DropboxListingBean) {
			mPathsToExplore = ((DropboxListingBean)listOfPathsAndFiles).getPaths();
			List<DbxEPubInfo> files = ((DropboxListingBean)listOfPathsAndFiles).getFiles();
			
			//mAdapter.addAll(files);
			
			// Probably faster than mAdapter.addAll(files) because elements are inserted in the correct position 
			// and it does not require to sort again. And does not include existing elements!
			for (DbxEPubInfo dbxEPubInfo : files) {
				Log.d("FileListFragment", "inserting element");
				mAdapter.add(dbxEPubInfo);
			}
			mAdapter.setNoMoreDataToLoad();
			
			for (DbxEPubInfo dbxEPubInfo : files) {
				// If dbxEPubInfo is synchronized then we can get the book's title
				if (DropboxManager.getInstance().isSync(dbxEPubInfo.getFileInfo())){
					EPubHelper.getInstance().openBookFromFileInfo(dbxEPubInfo.getFileInfo(), new BookAdapterListener(dbxEPubInfo, mAdapter), true);
				}
			}
			
			mAdapter.setLoading(false);
			getLoadingFooterView().setVisibility(View.GONE);
			setListShown(true);
			
		} else {
			Toast.makeText(getActivity(), getString(R.string.dropbox_connection_error), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void requestData() {
		getLoadingFooterView().setVisibility(View.VISIBLE);
		DropboxManager.getInstance().getFiles(mPathsToExplore, FILE_EXTENSION, MAX_FILES_PER_REQUEST, this);
	}
}
