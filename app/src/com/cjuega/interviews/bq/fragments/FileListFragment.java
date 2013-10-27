package com.cjuega.interviews.bq.fragments;

import java.util.Comparator;
import java.util.List;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.widgets.SortedListAdapter;
import com.cjuega.interviews.dropbox.DropboxManager;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxPath;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class FileListFragment extends ListFragment implements ActionBar.OnNavigationListener,
															  DropboxManager.SimpleCallback {
	
	private static final String FILE_EXTENSION = ".epub";
	
	private static final String SORT_BY_KEY = "SORT_BY_KEY";
	private static final int SORT_BY_FILENAME = 1;
	private static final int SORT_BY_CREATION_DATE = 2;
	
	private int mSortMethod;
	
	private OnFileSelectedListener mCallback;
	private ActionBar mActionBar;
	
	private int mPreviousNavigationMode;
	private SpinnerAdapter mDropDownAdapter;
	
	private SortedListAdapter<DbxFileInfo> mAdapter;
	
	// Container Activity must implement this interface
    public interface OnFileSelectedListener {
        public void OnFileSelected(DbxPath path);
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
			mDropDownAdapter = ArrayAdapter.createFromResource(getActivity(), 
															   R.array.action_sortby_list,
															   android.R.layout.simple_list_item_1);
		}else{
			mSortMethod = savedInstanceState.getInt(SORT_BY_KEY);
		}
		
		if (getActivity() instanceof ActionBarActivity){
			mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
			mPreviousNavigationMode = mActionBar.getNavigationMode();
			// To enable the drop-down menu within the Activity's ActionBar
			mActionBar.setListNavigationCallbacks(mDropDownAdapter, this);
		}else
			mActionBar = null;
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Async call to get all files
		DropboxManager.getInstance().getAllFiles(DbxPath.ROOT, FILE_EXTENSION, this);
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
	}

	@Override
	public void onPause() {
		if (mActionBar != null){
			mActionBar.setNavigationMode(mPreviousNavigationMode);
			//mActionBar.setListNavigationCallbacks(null, null);
		}
		super.onPause();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallback.OnFileSelected(mAdapter.getItem(position).path);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		if (mAdapter != null){
			switch (position) {
			case 0:
				mAdapter.sortby (new FilenameComparator());
				mSortMethod = SORT_BY_FILENAME;
				return true;
				
			case 1:
				mAdapter.sortby (new CreationDateComparator());
				mSortMethod = SORT_BY_CREATION_DATE;
				return true;
	
			default:
				return false;
			}
		}
		return false;
	}
	
	private class FilenameComparator implements Comparator<DbxFileInfo>{

		@Override
		public int compare(DbxFileInfo lhs, DbxFileInfo rhs) {
			return lhs.path.getName().compareTo(rhs.path.getName());
		}
	}
	
	private class CreationDateComparator implements Comparator<DbxFileInfo>{

		@Override
		public int compare(DbxFileInfo lhs, DbxFileInfo rhs) {
			return lhs.modifiedTime.compareTo(rhs.modifiedTime);
		}
	}

	@Override
	public void call(Object listOfFiles) {
		if (listOfFiles != null && listOfFiles instanceof List<?>) {
			List<DbxFileInfo> files = (List<DbxFileInfo>)listOfFiles;
			
			mAdapter = new SortedListAdapter<DbxFileInfo>(getActivity(), 
														  android.R.layout.simple_list_item_1,
														  files,
														  new FilenameComparator());
			mSortMethod = SORT_BY_FILENAME;
			setListAdapter(mAdapter);
		} else {
			Toast.makeText(getActivity(), getString(R.string.dropbox_connection_error), Toast.LENGTH_SHORT).show();
		}
	}
}
