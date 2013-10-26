package com.cjuega.interviews.bq.main;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.bq.fragments.BookDetailsFragment;
import com.cjuega.interviews.bq.fragments.FileListFragment;
import com.cjuega.interviews.bq.fragments.FileListFragment.OnFileSelectedListener;
import com.cjuega.interviews.dropbox.DropboxManager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * 
 * @author cjuega
 *
 * This is the main activity in our application.
 *
 */
public class LibraryActivity extends ActionBarActivity implements OnFileSelectedListener {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
    		   .replace(R.id.fragment_container, new FileListFragment(), null)
    		   .commit();

            getSupportFragmentManager().executePendingTransactions();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.library_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		switch (item.getItemId()) {
        case R.id.action_logout:
        	DropboxManager.getInstance().dropboxLogout();
        	finish();
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
		}
    }

	@Override
	public void OnFileSelected(String filename) {
		getSupportFragmentManager().beginTransaction()
		   .replace(R.id.fragment_container, new BookDetailsFragment(), null)
		   .addToBackStack(null)
		   .commit();

		getSupportFragmentManager().executePendingTransactions();
	}
}
